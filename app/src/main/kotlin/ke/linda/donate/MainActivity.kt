package ke.linda.donate

import android.Manifest
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import ke.linda.donate.redactor.Csv
import ke.linda.donate.redactor.RawSms
import ke.linda.donate.redactor.Redactor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.util.UUID

private enum class Stage { CONSENT, CHOOSE, DONE, FINISHED }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) { DonateApp() }
            }
        }
    }
}

@Composable
private fun DonateApp() {
    val context = LocalContext.current
    val store = remember { DonationStore(context) }
    val scope = rememberCoroutineScope()

    var swahili by remember { mutableStateOf(false) }
    val s = if (swahili) Strings.sw else Strings.en

    var stage by remember {
        mutableStateOf(
            when {
                store.done -> Stage.FINISHED
                store.pendingFile?.let { File(it).exists() } == true -> Stage.DONE
                else -> Stage.CONSENT
            },
        )
    }
    var messages by remember { mutableStateOf<List<RawSms>>(emptyList()) }
    var excluded by remember { mutableStateOf<Set<String>>(emptySet()) }
    var ownNumber by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    fun loadInbox() {
        scope.launch {
            busy = true
            messages = withContext(Dispatchers.IO) { SmsReader.readInbox(context.contentResolver) }
            busy = false
            message = if (messages.isEmpty()) s.noMessages else null
            if (messages.isNotEmpty()) stage = Stage.CHOOSE
        }
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) loadInbox() else message = s.permissionDenied
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(s.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = { swahili = !swahili }) { Text(s.language) }
        }

        when (stage) {
            Stage.CONSENT -> {
                Text(s.consentIntro)
                s.consentPoints.forEach { Text("• $it") }
                Button(
                    enabled = !busy,
                    onClick = {
                        val has = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) ==
                            PackageManager.PERMISSION_GRANTED
                        if (has) loadInbox() else permission.launch(Manifest.permission.READ_SMS)
                    },
                ) { Text(s.agree) }
            }

            Stage.CHOOSE -> {
                val senders = remember(messages) {
                    messages.groupingBy { it.address.orEmpty() }.eachCount().entries.sortedByDescending { it.value }
                }
                val included = messages.filter { it.address.orEmpty() !in excluded }
                val redactor = remember(ownNumber) { newRedactor(ownNumber) }

                Text(s.chooseHelp)
                OutlinedTextField(
                    value = ownNumber,
                    onValueChange = { ownNumber = it },
                    label = { Text(s.ownNumberLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(s.messagesCount(included.size, messages.size), style = MaterialTheme.typography.titleSmall)

                // Preview shows exactly what the CSV will contain, so the donor can trust it.
                Text(s.previewTitle, style = MaterialTheme.typography.titleSmall)
                included.take(3).forEach {
                    val row = redactor.toRow(it)
                    Text("${row.senderId}: ${row.text.take(140)}", style = MaterialTheme.typography.bodySmall)
                }

                LazyColumn(Modifier.weight(1f)) {
                    items(senders.toList(), key = { it.key }) { (address, count) ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row {
                                Checkbox(
                                    checked = address !in excluded,
                                    onCheckedChange = { on -> excluded = if (on) excluded - address else excluded + address },
                                )
                                Text(address.ifEmpty { "?" }, Modifier.padding(top = 12.dp))
                            }
                            Text("$count", Modifier.padding(top = 12.dp))
                        }
                    }
                }
                Button(
                    enabled = included.isNotEmpty() && !busy,
                    onClick = {
                        scope.launch {
                            busy = true
                            val file = withContext(Dispatchers.IO) { writeCsv(context, redactor, included) }
                            store.pendingFile = file.absolutePath
                            busy = false
                            share(context, file)
                            // Raw messages are no longer needed; drop them from memory.
                            messages = emptyList()
                            stage = Stage.DONE
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(s.export) }
            }

            Stage.DONE -> {
                Text(s.doneTitle, style = MaterialTheme.typography.titleMedium)
                Text(s.doneBody)
                OutlinedButton(onClick = { store.pendingFile?.let { share(context, File(it)) } }) { Text(s.shareAgain) }
                Button(onClick = {
                    store.pendingFile?.let { File(it).delete() }
                    store.pendingFile = null
                    store.done = true
                    stage = Stage.FINISHED
                }) { Text(s.finish) }
            }

            Stage.FINISHED -> Text(s.alreadyDone)
        }

        message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

/** Builds the redactor for this donation. Donor id is a hash, never the phone number itself. */
private fun newRedactor(ownNumber: String): Redactor {
    val own = ownNumber.takeIf { it.isNotBlank() }
    val seed = own?.let { Redactor.normalizePhone(it) } ?: UUID.randomUUID().toString()
    val donorId = MessageDigest.getInstance("SHA-256")
        .digest("${BuildConfig.DONATION_SALT}:donor:$seed".toByteArray())
        .joinToString("") { "%02x".format(it) }.take(10)
    return Redactor(BuildConfig.DONATION_SALT, own, donorId)
}

private fun writeCsv(context: Context, redactor: Redactor, messages: List<RawSms>): File {
    val dir = File(context.cacheDir, "donations").apply { mkdirs() }
    val file = File(dir, "sms-donation-${redactor.donorId}.csv")
    file.writeText(Csv.render(messages.map(redactor::toRow)), Charsets.UTF_8)
    return file
}

private fun share(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newRawUri("", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
