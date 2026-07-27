package com.dins.minddrop.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.Priority
import com.dins.minddrop.domain.model.ReminderType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NoteNotificationBuilder @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun build(note: Note): Notification {
        val channelId = when (note.priority) {
            Priority.URGENT, Priority.HIGH -> NotificationHelper.REMINDERS_CHANNEL_ID
            Priority.NORMAL, Priority.LOW -> NotificationHelper.DAILY_SURFACE_CHANNEL_ID
        }

        return NotificationCompat.Builder(context, channelId)
            // Placeholder system icon until Day 15 adds a real app/notification icon.
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(note.type.name)
            .setContentText(note.content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(note.content))
            .setPriority(notificationPriority(note.priority))
            .setContentIntent(deepLinkPendingIntent(note.id))
            .setAutoCancel(true)
            .build()
    }

    /**
     * Builds a user-set reminder notification. Alarm-type reminders go to the
     * alarm channel (alarm audio stream) and are marked as alarm category so the
     * system treats them accordingly under Do Not Disturb.
     */
    fun buildReminder(noteId: String, content: String, type: ReminderType): Notification {
        val channelId = when (type) {
            ReminderType.ALARM -> NotificationHelper.NOTE_ALARMS_CHANNEL_ID
            ReminderType.NOTIFICATION -> NotificationHelper.NOTE_REMINDERS_CHANNEL_ID
        }
        val title = when (type) {
            ReminderType.ALARM -> "Note alarm"
            ReminderType.NOTIFICATION -> "Note reminder"
        }

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(
                if (type == ReminderType.ALARM) {
                    NotificationCompat.CATEGORY_ALARM
                } else {
                    NotificationCompat.CATEGORY_REMINDER
                }
            )
            // Alarms keep vibrating/ringing until acknowledged; a plain reminder
            // shouldn't nag, so only the alarm variant is insistent.
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(deepLinkPendingIntent(noteId))
            .build()
    }

    private fun notificationPriority(priority: Priority): Int = when (priority) {
        Priority.URGENT, Priority.HIGH -> NotificationCompat.PRIORITY_HIGH
        Priority.NORMAL, Priority.LOW -> NotificationCompat.PRIORITY_DEFAULT
    }

    // Implicit intent, not an explicit reference to :app's MainActivity, so
    // :notifications stays decoupled from the UI module -- the manifest's
    // intent-filter for the minddrop://note scheme resolves this at runtime.
    private fun deepLinkPendingIntent(noteId: String): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("$DEEP_LINK_BASE/$noteId")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context,
            noteId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private companion object {
        const val DEEP_LINK_BASE = "minddrop://note"
    }
}
