package ke.linda.donate

import android.content.ContentResolver
import android.provider.Telephony
import ke.linda.donate.redactor.RawSms

/** Reads the inbox only (received messages). Needs the READ_SMS permission. */
object SmsReader {
    fun readInbox(resolver: ContentResolver): List<RawSms> {
        val out = ArrayList<RawSms>()
        val columns = arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE)
        resolver.query(Telephony.Sms.Inbox.CONTENT_URI, columns, null, null, "${Telephony.Sms.DATE} ASC")?.use { c ->
            val a = c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val b = c.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val d = c.getColumnIndexOrThrow(Telephony.Sms.DATE)
            while (c.moveToNext()) {
                out.add(RawSms(c.getString(a), c.getString(b).orEmpty(), c.getLong(d)))
            }
        }
        return out
    }
}
