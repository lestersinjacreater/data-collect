# How it works (plain language)

**Why this app exists.** A scam detector learns from examples. Real Kenyan SMS are the best examples,
so teammates donate theirs. Because inboxes also hold private things, the app is built to share the minimum.

**Reading.** `SmsReader` asks Android for the inbox (received messages only), after the donor agrees and grants permission.

**Redacting** (`redactor/Redactor.kt`). Each message goes through simple text rules, in this order:
1. Card numbers: long digit strings that pass the Luhn check become `<CARD>`.
2. Phone numbers (`07..`, `+2547..`, with spaces or dashes) become `<PHONE_xxxxxxxxxx>`, a short salted hash. The same number
   always gives the same token, so the team can see "this scam number appeared in 40 messages" without seeing the number.
3. ID numbers after the word "ID" become `<ID>`.
4. One-time codes next to words like code/OTP/PIN become `<OTP>`. Amounts such as `Ksh 1500` are kept, because amounts are a scam clue.

**Choosing.** The donor sees every sender with a count, can untick any, and sees three redacted examples before anything is written.

**Exporting** (`Csv.kt`). One CSV with a byte-order mark so Excel reads Swahili correctly. A leading `'` stops spreadsheet formula injection.
The file is opened in the share sheet; the app has no internet permission, so it cannot send anything itself.

**One time.** After "Finish" the file is deleted and a flag locks the screen. If the app is closed before finishing, the file can be shared again until then.

**Limits we are honest about.** Regex rules cannot catch everything (e.g. a name written in a message). Hashes with a shared salt can be
brute-forced for phone numbers. So the file must stay private to the team.
