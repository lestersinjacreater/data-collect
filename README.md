# SMS Donate

A one-time, consent-based Android app. A teammate installs it, reads a short notice,
and creates **one spreadsheet (CSV) of their received SMS** to hand to the hackathon team.
The messages are used to train the scam-detection model (Linda).

## What it does
1. Shows a consent screen (English / Kiswahili).
2. Asks for the SMS permission and reads the **inbox only**.
3. Lets the donor untick any sender, and shows a live preview of the redacted rows.
4. Hides sensitive parts on the device: OTP/PIN codes, national IDs, card numbers (Luhn-checked)
   and phone numbers (stable hash tokens). The donor's own number becomes `<SELF>`.
5. Writes `sms-donation-<donor>.csv` and opens the Android share sheet.
6. After "Finish" the file is deleted and the app locks itself (one donation per install).

## Privacy design
- **No INTERNET permission.** The app cannot upload anything; the donor chooses how to send the file.
- Redaction runs before the file exists. Rules and tests live in `redactor/` (plain Kotlin).
- Company sender IDs (`MPESA`, `KPLC`...) stay readable; personal numbers become `P_<hash>`.
- Phone hashes use a team-wide salt (`DONATION_SALT`, build-time env var / repo secret). A 9-digit
  phone space can be brute-forced by anyone who has the salt, so **treat the CSV as confidential**.
- Messages with `=`, `+`, `-`, `@` at the start get a leading `'` so Excel never runs them as formulas.

## CSV columns
`donor_id, sender_id, sender_type (company|personal|shortcode|unknown), text, received_at (UTC ISO), label (empty: fill scam/safe later)`

## Download page
`docs/index.html` is a bilingual download page served by GitHub Pages (Settings → Pages → Deploy from branch → `main` / `/docs`).
Its button points to the stable URL `releases/download/latest/sms-donate.apk`, so it never needs editing when the APK is rebuilt.

## Build
- Push to `main`: GitHub Actions runs the redaction tests, builds the debug APK and
  publishes it under **Releases → Latest build**.
- Local: `./gradlew :redactor:test` and `./gradlew :app:assembleDebug` (needs the Android SDK).
- Set the repo secret `DONATION_SALT` once (any long random string) before sharing the APK, and keep it unchanged
  so hashes match across donors.

## Handling the donated files
Never commit CSVs (they are git-ignored). Keep them off public places. Delete them when the hackathon ends.
See `docs/DONOR_NOTICE.md` for the text to send to donors and `docs/EXPLAINED.md` for a plain-language walkthrough.
