package com.dins.minddrop.domain.usecase

import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.repository.NoteRepository
import javax.inject.Inject

class UpdateNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) = repository.updateNote(note)
}
