package com.dins.minddrop.domain.usecase

import com.dins.minddrop.domain.reminder.ReminderScheduler
import com.dins.minddrop.domain.repository.NoteRepository
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
    private val repository: NoteRepository,
    private val reminderScheduler: ReminderScheduler
) {
    // Cancel before deleting: if the delete then failed we'd be left with a note
    // whose reminder is gone, which the user can simply set again. The reverse
    // order risks firing a reminder for a note that no longer exists.
    suspend operator fun invoke(id: String) {
        reminderScheduler.cancel(id)
        repository.deleteNote(id)
    }
}
