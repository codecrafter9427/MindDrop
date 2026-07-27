package com.dins.minddrop.notifications.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dins.minddrop.domain.model.ReminderType
import com.dins.minddrop.domain.usecase.HandleFiredReminderUseCase
import com.dins.minddrop.notifications.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receives a fired reminder alarm, posts its notification, then settles the
 * note's reminder state.
 *
 * The notification is built from intent extras rather than a database read so it
 * appears immediately -- onReceive runs on the main thread with a short window.
 * The follow-up work (advancing a repeat, or clearing a spent one-time reminder)
 * does touch Room, so it runs under goAsync on the IO dispatcher.
 */
@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var handleFiredReminder: HandleFiredReminderUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getStringExtra(EXTRA_NOTE_ID) ?: return
        val content = intent.getStringExtra(EXTRA_CONTENT).orEmpty()
        val type = intent.getStringExtra(EXTRA_REMINDER_TYPE)
            ?.let { runCatching { ReminderType.valueOf(it) }.getOrNull() }
            ?: ReminderType.NOTIFICATION

        Log.d(TAG, "Reminder fired for note $noteId (type=$type)")
        notificationHelper.showReminder(noteId = noteId, content = content, type = type)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                handleFiredReminder(noteId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to settle reminder for note $noteId", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_REMINDER = "com.dins.minddrop.action.REMINDER"
        const val EXTRA_NOTE_ID = "note_id"
        const val EXTRA_CONTENT = "content"
        const val EXTRA_REMINDER_TYPE = "reminder_type"
        private const val TAG = "ReminderReceiver"
    }
}
