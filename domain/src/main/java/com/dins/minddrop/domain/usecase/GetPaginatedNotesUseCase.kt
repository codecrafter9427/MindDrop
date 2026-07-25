package com.dins.minddrop.domain.usecase

import androidx.paging.PagingData
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.SortOrder
import com.dins.minddrop.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPaginatedNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(sortOrder: SortOrder): Flow<PagingData<Note>> =
        repository.getPaginatedNotes(sortOrder)
}
