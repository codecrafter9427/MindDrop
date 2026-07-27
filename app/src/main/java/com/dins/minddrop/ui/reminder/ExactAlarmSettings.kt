package com.dins.minddrop.ui.reminder

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * Opens the system screen where the user grants exact-alarm access.
 *
 * SCHEDULE_EXACT_ALARM can't be requested with the normal runtime permission
 * dialog -- it's a special app access, so the only route is this settings intent.
 */
fun Context.openExactAlarmSettings() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    // Some OEM builds don't ship the per-app screen; fall back to app details so
    // the user still lands somewhere they can act, rather than nothing happening.
    runCatching { startActivity(intent) }.onFailure {
        runCatching {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
    }
}
