package com.dins.minddrop.domain.usecase

import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.reminder.ReminderScheduler
import com.dins.minddrop.domain.repository.NoteRepository
import javax.inject.Inject

class AddNoteUseCase @Inject constructor(
    private val repository: NoteRepository,
    private val reminderScheduler: ReminderScheduler
) {
    // Scheduling lives here rather than in the ViewModel so a note's stored
    // reminder and its pending alarm can't drift apart -- there's no code path
    // that saves a note and forgets to arm it.
    suspend operator fun invoke(note: Note) {
        repository.addNote(note)
        reminderScheduler.schedule(note)
    }
}
