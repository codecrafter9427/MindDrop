package com.dins.minddrop.domain.usecase

import com.dins.minddrop.domain.repository.NoteRepository
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteNote(id)
}
