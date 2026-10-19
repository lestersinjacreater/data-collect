package ke.linda.donate

import android.content.Context

/**
 * Remembers the one-time rule on the device.
 * `pendingFile` is set while a CSV exists and has not been deleted yet, so a donor who
 * closes the app after exporting can still share the file again.
 */
class DonationStore(context: Context) {
    private val prefs = context.getSharedPreferences("donation", Context.MODE_PRIVATE)

    var done: Boolean
        get() = prefs.getBoolean("done", false)
        set(v) = prefs.edit().putBoolean("done", v).apply()

    var pendingFile: String?
        get() = prefs.getString("pending_file", null)
        set(v) = prefs.edit().putString("pending_file", v).apply()
}
