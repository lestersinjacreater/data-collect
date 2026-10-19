package ke.linda.donate.redactor

import java.security.MessageDigest
import java.time.Instant
import java.time.temporal.ChronoUnit

/** One SMS exactly as read from the phone. Never leaves the device in this form. */
data class RawSms(val address: String?, val body: String, val receivedAtMillis: Long)

/** One CSV row after redaction. This is the only shape that can be exported. */
data class DonationRow(
    val donorId: String,
    val senderId: String,
    val senderType: String,
    val text: String,
    val receivedAt: String,
)

/**
 * Removes the sensitive parts of a message before it is exported.
 *
 * Why these rules: the model needs to learn how scams *read* (wording, links, amounts,
 * urgency), not who the people are. So we hide credentials and identities, and keep
 * everything else as written.
 *
 * - OTP / PIN codes, national IDs and card numbers are replaced by a plain token.
 * - Phone numbers become a stable hash token, so the same number is recognisable across
 *   messages (useful for spotting a scam campaign) without revealing it. The hash uses a
 *   team-wide salt; a 9-digit phone space can still be brute-forced by someone who knows
 *   the salt, so treat the CSV as confidential, not anonymous.
 * - Company sender IDs such as MPESA are kept readable: they are the strongest signal.
 */
class Redactor(private val salt: String, ownNumber: String?, val donorId: String) {

    private val ownNormalized: String? = ownNumber?.let(::normalizePhone)

    fun toRow(sms: RawSms): DonationRow {
        val (senderId, senderType) = describeSender(sms.address)
        return DonationRow(
            donorId = donorId,
            senderId = senderId,
            senderType = senderType,
            text = redactText(sms.body),
            receivedAt = Instant.ofEpochMilli(sms.receivedAtMillis).truncatedTo(ChronoUnit.SECONDS).toString(),
        )
    }

    fun describeSender(address: String?): Pair<String, String> {
        val a = address?.trim().orEmpty()
        if (a.isEmpty()) return "unknown" to "unknown"
        if (a.any { it.isLetter() }) return a to "company"
        val phone = normalizePhone(a) ?: return a to "shortcode"
        return (if (phone == ownNormalized) "<SELF>" else "P_" + hash(phone)) to "personal"
    }

    fun redactText(body: String): String {
        var t = body
        t = cardRegex.replace(t) { m -> if (isLuhnCard(m.value)) "<CARD>" else m.value }
        t = phoneRegex.replace(t) { m ->
            val phone = normalizePhone(m.value) ?: return@replace m.value
            if (phone == ownNormalized) "<SELF>" else "<PHONE_" + hash(phone) + ">"
        }
        t = idRegex.replaceGroup(t, "<ID>")
        t = otpAfterKeyword.replaceGroup(t, "<OTP>", ::looksLikeMoney)
        t = otpBeforeKeyword.replaceGroup(t, "<OTP>")
        return t
    }

    private fun hash(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest("$salt:$value".toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(10)
    }

    companion object {
        private val cardRegex = Regex("(?<!\\d)(?:\\d[ -]?){12,18}\\d(?!\\d)")
        private val phoneRegex =
            Regex("(?<![\\d.,])(?:\\+?254|0)[\\s-]?[17]\\d{2}[\\s-]?\\d{3}[\\s-]?\\d{3}(?!\\d)")
        private val idRegex =
            Regex("(?i)\\b(?:national\\s+)?id(?:\\s*(?:no\\.?|number|namba))?\\s*[:#.]?\\s*(\\d{7,9})(?!\\d)")
        private const val OTP_WORDS =
            "otp|pin|code|passcode|password|verification|verify|one[- ]time|msimbo|nambari ya siri|namba ya siri"
        private val otpAfterKeyword = Regex("(?i)\\b(?:$OTP_WORDS)\\b[^\\d]{0,25}(\\d{4,8})(?!\\d)")
        private val otpBeforeKeyword =
            Regex("(?i)(?<![\\d.,])(\\d{4,8})(?![\\d.,]\\d)(?=\\s+(?:is|ni)\\s+(?:your|yako)\\b)")

        /** `2547XXXXXXXX` form used everywhere in Linda, or null if this is not a phone number. */
        fun normalizePhone(raw: String): String? {
            val d = raw.filter { it.isDigit() }
            return when {
                d.length == 12 && d.startsWith("254") -> d
                d.length == 10 && d.startsWith("0") -> "254" + d.drop(1)
                else -> null
            }
        }

        /** Luhn check: stops us hiding random long numbers that are not really cards. */
        fun isLuhnCard(raw: String): Boolean {
            val digits = raw.filter { it.isDigit() }
            if (digits.length !in 13..19) return false
            var sum = 0
            digits.reversed().forEachIndexed { i, c ->
                var n = c - '0'
                if (i % 2 == 1) {
                    n *= 2
                    if (n > 9) n -= 9
                }
                sum += n
            }
            return sum % 10 == 0
        }

        /** "Ksh 1500" or "1500.00" must survive: amounts are a key scam signal. */
        private fun looksLikeMoney(text: String, group: MatchGroup): Boolean {
            val before = text.substring(maxOf(0, group.range.first - 6), group.range.first)
            val after = text.substring(group.range.last + 1, minOf(text.length, group.range.last + 3))
            return Regex("(?i)(ksh|kes|sh|usd|\\$)\\s*$").containsMatchIn(before) ||
                Regex("^[.,]\\d").containsMatchIn(after)
        }

        private fun Regex.replaceGroup(
            text: String,
            token: String,
            skip: (String, MatchGroup) -> Boolean = { _, _ -> false },
        ): String {
            val out = StringBuilder()
            var last = 0
            for (m in findAll(text)) {
                val g = m.groups[1] ?: continue
                if (skip(text, g)) continue
                out.append(text, last, g.range.first).append(token)
                last = g.range.last + 1
            }
            return out.append(text, last, text.length).toString()
        }
    }
}
