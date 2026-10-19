package ke.linda.donate.redactor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RedactorTest {
    private val r = Redactor(salt = "test", ownNumber = "0711222333", donorId = "d1")

    @Test fun otpAfterKeywordIsMasked() {
        assertEquals("Your M-PESA code is <OTP>. Do not share.", r.redactText("Your M-PESA code is 482913. Do not share."))
        assertEquals("OTP: <OTP>", r.redactText("OTP: 4829"))
    }

    @Test fun otpBeforeKeywordIsMasked() {
        assertEquals("<OTP> is your verification code", r.redactText("123456 is your verification code"))
    }

    @Test fun amountsNearKeywordsSurvive() {
        val text = "Confirm PIN to send Ksh1500 to John"
        assertEquals(text, r.redactText(text))
        val dec = "Enter code to receive KES 12000.00 now"
        assertEquals(dec, r.redactText(dec))
    }

    @Test fun nationalIdIsMasked() {
        assertEquals("Verify ID <ID> now", r.redactText("Verify ID 12345678 now"))
        assertEquals("National ID No: <ID>", r.redactText("National ID No: 3456789"))
    }

    @Test fun luhnValidCardIsMaskedButRandomLongNumberIsNot() {
        assertEquals("Card <CARD> charged", r.redactText("Card 4111 1111 1111 1111 charged"))
        val notCard = "Ref 1234567890123 ok"
        assertEquals(notCard, r.redactText(notCard))
    }

    @Test fun phoneNumbersBecomeStableTokens() {
        val a = r.redactText("Call 0712345678 now")
        val b = r.redactText("or +254 712 345 678")
        val token = Regex("<PHONE_[0-9a-f]{10}>").find(a)!!.value
        assertTrue(b.contains(token))
        assertFalse(a.contains("0712345678"))
    }

    @Test fun ownNumberBecomesSelfInBodyAndSender() {
        assertEquals("Hi <SELF>", r.redactText("Hi 0711222333"))
        assertEquals("<SELF>" to "personal", r.describeSender("+254711222333"))
    }

    @Test fun senderTypes() {
        assertEquals("MPESA" to "company", r.describeSender("MPESA"))
        assertEquals("456" to "shortcode", r.describeSender("456"))
        assertEquals("unknown" to "unknown", r.describeSender(null))
        val (id, type) = r.describeSender("0722000111")
        assertEquals("personal", type)
        assertTrue(id.startsWith("P_"))
        assertFalse(id.contains("722000111"))
    }

    @Test fun differentSaltGivesDifferentHash() {
        val other = Redactor("other", null, "d2")
        assertFalse(r.describeSender("0722000111").first == other.describeSender("0722000111").first)
    }

    @Test fun normalNonSensitiveTextIsUntouched() {
        val text = "Congratulations! You won Ksh 50,000. Send Ksh 200 to claim at bit.ly/xyz"
        assertEquals(text, r.redactText(text))
    }

    @Test fun rowTimeIsIsoUtcSeconds() {
        val row = r.toRow(RawSms("MPESA", "hi", 1_760_968_931_123L))
        assertEquals("2025-10-20T14:02:11Z", row.receivedAt)
    }
}
