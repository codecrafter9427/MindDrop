package com.dins.minddrop.domain.usecase

import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.reminder.ReminderScheduler
import com.dins.minddrop.domain.repository.NoteRepository
import javax.inject.Inject

class UpdateNoteUseCase @Inject constructor(
    private val repository: NoteRepository,
    private val reminderScheduler: ReminderScheduler
) {
    // schedule() replaces any alarm already pending for this note, and cancels
    // outright when the reminder was cleared -- so editing covers add, change
    // and remove without three separate paths.
    suspend operator fun invoke(note: Note) {
        repository.updateNote(note)
        reminderScheduler.schedule(note)
    }
}
