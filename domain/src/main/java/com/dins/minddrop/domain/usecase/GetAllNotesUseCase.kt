package com.dins.minddrop.domain.usecase

import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(): Flow<List<Note>> = repository.getAllNotes()
}
