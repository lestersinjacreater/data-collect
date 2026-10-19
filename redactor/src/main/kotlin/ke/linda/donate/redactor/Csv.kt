package ke.linda.donate.redactor

/** Writes the donation file. CSV (not .xlsx) because Excel, Sheets and pandas all open it. */
object Csv {
    val HEADER = listOf("donor_id", "sender_id", "sender_type", "text", "received_at", "label")

    // Excel needs a byte-order mark to read Swahili and emoji correctly.
    private const val BOM = "﻿"

    fun render(rows: List<DonationRow>): String {
        val sb = StringBuilder(BOM)
        sb.append(HEADER.joinToString(",")).append("\r\n")
        for (r in rows) {
            // "label" is empty on purpose: the team fills scam / safe later.
            sb.append(listOf(r.donorId, r.senderId, r.senderType, r.text, r.receivedAt, "").joinToString(",") { cell(it) })
                .append("\r\n")
        }
        return sb.toString()
    }

    fun cell(value: String): String {
        // A message starting with = + - @ would run as a formula when opened in Excel.
        val safe = if (value.isNotEmpty() && value[0] in "=+-@\t\r") "'$value" else value
        return if (safe.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + safe.replace("\"", "\"\"") + "\""
        } else {
            safe
        }
    }
}
