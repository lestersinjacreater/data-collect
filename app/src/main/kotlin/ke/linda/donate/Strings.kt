package ke.linda.donate

/** All screen text in English and Swahili. The donor can flip between them on every screen. */
data class Strings(
    val title: String,
    val consentIntro: String,
    val consentPoints: List<String>,
    val agree: String,
    val permissionDenied: String,
    val restrictedHelp: String,
    val openSettings: String,
    val chooseTitle: String,
    val chooseHelp: String,
    val ownNumberLabel: String,
    val previewTitle: String,
    val messagesCount: (Int, Int) -> String,
    val export: String,
    val doneTitle: String,
    val doneBody: String,
    val shareAgain: String,
    val finish: String,
    val alreadyDone: String,
    val noMessages: String,
    val language: String,
) {
    companion object {
        val en = Strings(
            title = "Donate my SMS (one time)",
            consentIntro = "Help train Linda to spot M-Pesa scams. Please read this first:",
            consentPoints = listOf(
                "This is a one-time donation for the hackathon team only.",
                "The app reads your received messages once, on this phone.",
                "Before anything is saved, codes (OTP/PIN), ID numbers, card numbers and phone numbers are hidden. Your own number becomes <SELF>.",
                "You can leave out any sender you do not want to share.",
                "The app cannot upload anything. It makes one spreadsheet file and you choose who to send it to.",
                "When you finish, the file is deleted from this phone.",
            ),
            agree = "I understand and agree",
            permissionDenied = "Permission to read SMS was not granted, so nothing was read.",
            restrictedHelp = "Android blocks SMS access for apps installed from a downloaded file. To allow it:\n" +
                "1. Tap \"Open app settings\".\n" +
                "2. Tap the three dots (top right) and choose \"Allow restricted settings\". Confirm with your PIN or fingerprint.\n" +
                "3. Tap Permissions, then SMS, then Allow.\n" +
                "4. Come back here and tap the button again.",
            openSettings = "Open app settings",
            chooseTitle = "Choose what to share",
            chooseHelp = "Everyone is included by default. Untick senders you want to keep out.",
            ownNumberLabel = "Your phone number (optional, so we can hide it)",
            previewTitle = "What will be shared (examples):",
            messagesCount = { n, total -> "$n of $total messages will be shared" },
            export = "Create file and share",
            doneTitle = "File created",
            doneBody = "Send the file to the team. Tap \"Finish\" afterwards to delete it from this phone.",
            shareAgain = "Share again",
            finish = "Finish and delete the file",
            alreadyDone = "Thank you! Your donation is done. Nothing more to do.",
            noMessages = "No messages found in the inbox.",
            language = "Kiswahili",
        )

        val sw = Strings(
            title = "Changia SMS zangu (mara moja)",
            consentIntro = "Saidia kufundisha Linda kutambua ulaghai wa M-Pesa. Tafadhali soma kwanza:",
            consentPoints = listOf(
                "Hii ni mchango wa mara moja kwa timu ya hackathon pekee.",
                "Programu inasoma jumbe ulizopokea mara moja, kwenye simu hii.",
                "Kabla ya kuhifadhi, misimbo (OTP/PIN), nambari za kitambulisho, kadi na simu hufichwa. Nambari yako inakuwa <SELF>.",
                "Unaweza kuacha nje mtumaji yeyote usiyetaka kushiriki.",
                "Programu haiwezi kutuma chochote mtandaoni. Inatengeneza faili moja na wewe unachagua utamtumia nani.",
                "Ukimaliza, faili inafutwa kutoka kwenye simu hii.",
            ),
            agree = "Nimeelewa na nakubali",
            permissionDenied = "Ruhusa ya kusoma SMS haikutolewa, kwa hivyo hakuna kilichosomwa.",
            restrictedHelp = "Android huzuia SMS kwa programu zilizosakinishwa kutoka faili iliyopakuliwa. Ili kuruhusu:\n" +
                "1. Bonyeza \"Fungua mipangilio ya programu\".\n" +
                "2. Bonyeza nukta tatu (juu kulia) kisha uchague \"Allow restricted settings\". Thibitisha kwa PIN au alama ya kidole.\n" +
                "3. Bonyeza Permissions, kisha SMS, kisha Allow.\n" +
                "4. Rudi hapa na ubonyeze kitufe tena.",
            openSettings = "Fungua mipangilio ya programu",
            chooseTitle = "Chagua cha kushiriki",
            chooseHelp = "Kila mtu ameingizwa kwa chaguo-msingi. Ondoa tiki kwa watumaji usiotaka.",
            ownNumberLabel = "Nambari yako ya simu (hiari, ili tuifiche)",
            previewTitle = "Kitakachoshirikiwa (mifano):",
            messagesCount = { n, total -> "Jumbe $n kati ya $total zitashirikiwa" },
            export = "Tengeneza faili na ushiriki",
            doneTitle = "Faili imetengenezwa",
            doneBody = "Tuma faili kwa timu. Kisha bonyeza \"Maliza\" kuifuta kwenye simu hii.",
            shareAgain = "Shiriki tena",
            finish = "Maliza na ufute faili",
            alreadyDone = "Asante! Mchango wako umekamilika. Hakuna kingine cha kufanya.",
            noMessages = "Hakuna jumbe kwenye kikasha.",
            language = "English",
        )
    }
}
