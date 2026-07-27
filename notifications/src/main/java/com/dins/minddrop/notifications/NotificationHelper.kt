package com.dins.minddrop.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.ReminderType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationBuilder: NoteNotificationBuilder
) {

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        val reminders = NotificationChannel(
            REMINDERS_CHANNEL_ID,
            "MindDrop Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Urgent and high-priority note reminders" }

        val dailySurface = NotificationChannel(
            DAILY_SURFACE_CHANNEL_ID,
            "Daily Surface",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Notes MindDrop resurfaces based on staleness and usage" }

        // Separate channel for alarm-style reminders. USAGE_ALARM routes it to
        // the alarm volume stream, so it stays audible when the ringer is
        // silenced -- the whole point of choosing "alarm" over "notification".
        // A distinct channel also lets the user tune or mute alarms without
        // losing ordinary reminders.
        val noteAlarms = NotificationChannel(
            NOTE_ALARMS_CHANNEL_ID,
            "Note Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarm-style reminders you set on a note"
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            )
            enableVibration(true)
            vibrationPattern = ALARM_VIBRATION_PATTERN
        }

        val noteReminders = NotificationChannel(
            NOTE_REMINDERS_CHANNEL_ID,
            "Note Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notification-style reminders you set on a note"
            enableVibration(true)
        }

        manager.createNotificationChannels(
            listOf(reminders, dailySurface, noteAlarms, noteReminders)
        )
    }

    fun showNotification(note: Note) {
        if (!hasPostPermission()) {
            Log.d(TAG, "Skipping notification for ${note.id}: POST_NOTIFICATIONS not granted")
            return
        }

        NotificationManagerCompat.from(context)
            .notify(note.id.hashCode(), notificationBuilder.build(note))
        Log.d(TAG, "Posted notification for note ${note.id} (score=${note.surfaceScore})")
    }

    /**
     * Posts a user-set reminder. Takes the id and content directly rather than a
     * [Note] because the caller is a BroadcastReceiver that must not hit the
     * database on the main thread.
     */
    fun showReminder(noteId: String, content: String, type: ReminderType) {
        if (!hasPostPermission()) {
            Log.d(TAG, "Skipping reminder for $noteId: POST_NOTIFICATIONS not granted")
            return
        }

        NotificationManagerCompat.from(context).notify(
            reminderNotificationId(noteId),
            notificationBuilder.buildReminder(noteId = noteId, content = content, type = type)
        )
        Log.d(TAG, "Posted $type reminder for note $noteId")
    }

    private fun hasPostPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

    companion object {
        const val REMINDERS_CHANNEL_ID = "reminders"
        const val DAILY_SURFACE_CHANNEL_ID = "daily_surface"
        const val NOTE_ALARMS_CHANNEL_ID = "note_alarms"
        const val NOTE_REMINDERS_CHANNEL_ID = "note_reminders"

        private const val TAG = "NotificationHelper"
        private val ALARM_VIBRATION_PATTERN = longArrayOf(0, 500, 500, 500, 500)

        // Offset from the surfacing notification id so a note's reminder and its
        // surfaced notification don't overwrite each other.
        fun reminderNotificationId(noteId: String): Int = noteId.hashCode() xor REMINDER_ID_MASK
        private const val REMINDER_ID_MASK = 0x5EED
    }
}
