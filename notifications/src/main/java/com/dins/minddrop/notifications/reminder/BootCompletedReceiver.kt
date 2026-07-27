package com.dins.minddrop.notifications.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dins.minddrop.domain.usecase.RescheduleRemindersUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Re-arms reminders after a reboot, since the platform clears pending alarms.
 *
 * Also handles the package-replaced broadcast: an app update likewise drops
 * scheduled alarms, so without it reminders would vanish on every install.
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var rescheduleReminders: RescheduleRemindersUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in HANDLED_ACTIONS) return

        // goAsync keeps the receiver alive past onReceive so the database read
        // can finish; without it the process may be killed mid-reschedule.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                rescheduleReminders()
                Log.d(TAG, "Rescheduled reminders after ${intent.action}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reschedule reminders", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val TAG = "BootCompletedReceiver"
        val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED
        )
    }
}
