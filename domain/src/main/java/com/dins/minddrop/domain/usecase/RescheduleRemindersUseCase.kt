package com.dins.minddrop.domain.usecase

import com.dins.minddrop.domain.model.nextOccurrenceAfter
import com.dins.minddrop.domain.reminder.ReminderScheduler
import com.dins.minddrop.domain.repository.NoteRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Re-arms stored reminders with the platform.
 *
 * The platform drops all pending alarms on reboot and on app update, so without
 * this every reminder a user had set would silently never fire again. Room is the
 * source of truth, so recovery is a matter of replaying it.
 */
class RescheduleRemindersUseCase @Inject constructor(
    private val repository: NoteRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke() {
        val now = System.currentTimeMillis()

        repository.getAllNotes().first().forEach { note ->
            val reminder = note.reminder ?: return@forEach

            when {
                // Still upcoming: re-arm as-is.
                reminder.triggerAtMillis > now -> reminderScheduler.schedule(note)

                // Overdue but repeating: roll forward to the next occurrence instead
                // of dropping it, otherwise a daily alarm dies whenever the phone
                // happens to be off at its usual time.
                else -> {
                    val next = reminder.nextOccurrenceAfter(now)
                    if (next != null) {
                        val rolled = note.copy(reminder = reminder.copy(triggerAtMillis = next))
                        repository.updateNote(rolled)
                        reminderScheduler.schedule(rolled)
                    } else {
                        // Overdue and one-time: it fired (or was missed) while the
                        // device was off. Clear it rather than firing it late.
                        repository.updateNote(note.copy(reminder = null))
                    }
                }
            }
        }
    }
}
