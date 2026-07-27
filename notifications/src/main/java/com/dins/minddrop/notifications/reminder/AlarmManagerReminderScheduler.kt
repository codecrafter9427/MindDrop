package com.dins.minddrop.notifications.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.reminder.ReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmManagerReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : ReminderScheduler {

    private val alarmManager: AlarmManager =
        context.getSystemService(AlarmManager::class.java)

    override fun schedule(note: Note) {
        val reminder = note.reminder
        if (reminder == null) {
            cancel(note.id)
            return
        }

        // Re-arming a past time would fire immediately, which reads as a bug to
        // the user. Reaching here usually means an edit left the time behind.
        if (reminder.triggerAtMillis <= System.currentTimeMillis()) {
            Log.d(TAG, "Not scheduling note ${note.id}: reminder time already passed")
            cancel(note.id)
            return
        }

        val pendingIntent = reminderPendingIntent(note)

        // setAlarmClock rather than setExactAndAllowWhileIdle: it survives Doze
        // and app standby, which the user-visible promise of "remind me at 7am"
        // requires. It also surfaces in the system's next-alarm slot, so the
        // pending reminder is discoverable outside the app.
        val canBeExact = canScheduleExactReminders()
        if (canBeExact) {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(reminder.triggerAtMillis, pendingIntent),
                pendingIntent
            )
        } else {
            // Degrade rather than crash or silently drop: the reminder still
            // arrives, just batched by the system. The UI separately prompts the
            // user to grant exact-alarm permission.
            alarmManager.setWindow(
                AlarmManager.RTC_WAKEUP,
                reminder.triggerAtMillis,
                INEXACT_WINDOW_MILLIS,
                pendingIntent
            )
        }
        Log.d(
            TAG,
            "Scheduled ${reminder.type} for note ${note.id} at ${reminder.triggerAtMillis} " +
                "(repeat=${reminder.recurrence}, exact=$canBeExact)"
        )
    }

    override fun cancel(noteId: String) {
        // FLAG_NO_CREATE: if no PendingIntent exists there's nothing to cancel,
        // and creating one just to cancel it would be pointless work.
        val existing = PendingIntent.getBroadcast(
            context,
            requestCode(noteId),
            baseIntent(),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (existing != null) {
            alarmManager.cancel(existing)
            existing.cancel()
            Log.d(TAG, "Cancelled reminder for note $noteId")
        }
    }

    override fun canScheduleExactReminders(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

    private fun reminderPendingIntent(note: Note): PendingIntent {
        val intent = baseIntent().apply {
            putExtra(ReminderReceiver.EXTRA_NOTE_ID, note.id)
            putExtra(ReminderReceiver.EXTRA_CONTENT, note.content)
            putExtra(ReminderReceiver.EXTRA_REMINDER_TYPE, note.reminder?.type?.name)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode(noteId = note.id),
            intent,
            // UPDATE_CURRENT so rescheduling the same note replaces its alarm
            // rather than stacking a second one.
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun baseIntent() = Intent(context, ReminderReceiver::class.java).apply {
        action = ReminderReceiver.ACTION_REMINDER
    }

    // A note id is a UUID string but PendingIntent request codes are ints, so the
    // hash is the mapping. Collisions are theoretically possible; the cost would
    // be one note's reminder replacing another's, which is why cancel() and
    // schedule() must derive the code identically.
    private fun requestCode(noteId: String): Int = noteId.hashCode()

    private companion object {
        const val TAG = "ReminderScheduler"
        const val INEXACT_WINDOW_MILLIS = 10 * 60 * 1000L
    }
}
