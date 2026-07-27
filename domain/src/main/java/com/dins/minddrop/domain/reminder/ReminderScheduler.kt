package com.dins.minddrop.domain.reminder

import com.dins.minddrop.domain.model.Note

/**
 * Schedules and cancels a note's reminder with the platform.
 *
 * Implemented in :notifications, which owns scheduling and intent handling, so
 * :domain stays free of AlarmManager and other framework types.
 */
interface ReminderScheduler {

    /**
     * Schedules [note]'s reminder, replacing any already scheduled for it.
     * A note with no reminder, or one whose time has already passed, is cancelled instead.
     */
    fun schedule(note: Note)

    fun cancel(noteId: String)

    /**
     * Whether the platform will currently honour exact alarm times.
     *
     * False on Android 12+ when the user has revoked SCHEDULE_EXACT_ALARM; the
     * caller should route them to settings, since reminders would otherwise be
     * delivered late enough to be useless.
     */
    fun canScheduleExactReminders(): Boolean
}
