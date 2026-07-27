package com.dins.minddrop.domain.model

/**
 * A user-set reminder on a note.
 *
 * Modelled as a value object rather than two nullable fields on [Note] so the
 * time and the delivery style can't disagree: either a note has a reminder with
 * both, or it has none at all.
 */
data class Reminder(
    val triggerAtMillis: Long,
    val type: ReminderType,
    val recurrence: ReminderRecurrence = ReminderRecurrence.NONE
)

enum class ReminderType {
    /** Standard notification: respects Do Not Disturb and the user's silent mode. */
    NOTIFICATION,

    /** Rings on the alarm audio stream, so it's audible even when the ringer is silenced. */
    ALARM
}

/**
 * How often a reminder repeats.
 *
 * Repeats are re-armed one occurrence at a time rather than handed to
 * AlarmManager as a repeating alarm: `setRepeating` has been inexact since API
 * 19, which would defeat the point of an exact reminder.
 */
enum class ReminderRecurrence { NONE, DAILY, WEEKLY }
