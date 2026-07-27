package com.dins.minddrop.domain.usecase

import com.dins.minddrop.domain.model.nextOccurrenceAfter
import com.dins.minddrop.domain.reminder.ReminderScheduler
import com.dins.minddrop.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * Settles a note's reminder state once it has fired.
 *
 * A repeating reminder is advanced to its next occurrence and re-armed, since
 * exact alarms can't be registered as repeating. A one-time reminder is cleared,
 * so a spent reminder stops showing on the note as though it were still pending.
 */
class HandleFiredReminderUseCase @Inject constructor(
    private val repository: NoteRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(noteId: String) {
        val note = repository.getNoteById(noteId) ?: return
        val reminder = note.reminder ?: return

        val nextTrigger = reminder.nextOccurrenceAfter(System.currentTimeMillis())
        val settled = if (nextTrigger == null) {
            note.copy(reminder = null)
        } else {
            note.copy(reminder = reminder.copy(triggerAtMillis = nextTrigger))
        }

        // Persist before scheduling so a reboot between the two still recovers the
        // right time -- BootCompletedReceiver replays whatever Room holds.
        repository.updateNote(settled)
        reminderScheduler.schedule(settled)
    }
}
