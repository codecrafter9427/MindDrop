package com.dins.minddrop.fake

import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.Reminder
import com.dins.minddrop.domain.reminder.ReminderScheduler

/**
 * Records scheduling calls so tests can assert that persisting a note also armed
 * (or cancelled) its reminder.
 */
class FakeReminderScheduler(
    private var exactAllowed: Boolean = true
) : ReminderScheduler {

    val scheduled = mutableMapOf<String, Reminder>()
    val cancelled = mutableListOf<String>()

    override fun schedule(note: Note) {
        val reminder = note.reminder
        if (reminder == null) {
            cancel(note.id)
        } else {
            scheduled[note.id] = reminder
        }
    }

    override fun cancel(noteId: String) {
        scheduled.remove(noteId)
        cancelled += noteId
    }

    override fun canScheduleExactReminders(): Boolean = exactAllowed
}
