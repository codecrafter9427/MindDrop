package com.dins.minddrop.domain.model

import java.util.Calendar

/**
 * The next time this reminder should fire after [now], or null if it doesn't repeat.
 *
 * Advances in whole calendar steps via [Calendar.add] rather than adding fixed
 * millisecond offsets, so a daily 7am reminder stays at 7am across a daylight
 * saving change instead of drifting to 6am or 8am.
 *
 * Loops until the result is in the future: if the device was off for three days,
 * a daily reminder should land on the next upcoming occurrence, not on a time
 * that has already passed.
 */
fun Reminder.nextOccurrenceAfter(now: Long): Long? {
    val field = when (recurrence) {
        ReminderRecurrence.NONE -> return null
        ReminderRecurrence.DAILY -> Calendar.DAY_OF_MONTH
        ReminderRecurrence.WEEKLY -> Calendar.WEEK_OF_YEAR
    }

    val calendar = Calendar.getInstance().apply { timeInMillis = triggerAtMillis }
    do {
        calendar.add(field, 1)
    } while (calendar.timeInMillis <= now)
    return calendar.timeInMillis
}
