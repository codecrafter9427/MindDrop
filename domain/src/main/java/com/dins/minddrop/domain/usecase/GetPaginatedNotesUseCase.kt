package com.dins.minddrop.domain.usecase

import androidx.paging.PagingData
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.NoteFilter
import com.dins.minddrop.domain.model.SortOrder
import com.dins.minddrop.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Single entry point for the note list: search, type/priority filtering and
 * sorting are all resolved in one query.
 *
 * The curriculum splits this into SearchNoteUseCase / FilterNotesByTypeUseCase /
 * FilterNotesByPriorityUseCase each returning Flow<List<Note>>, but composing
 * those client-side would require loading every note into memory to filter it,
 * undoing the Paging 3 work from Day 12. Pushing all three concerns into SQL
 * keeps the list paginated no matter how the user narrows it.
 */
class GetPaginatedNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(
        sortOrder: SortOrder,
        filter: NoteFilter = NoteFilter()
    ): Flow<PagingData<Note>> = repository.getPaginatedNotes(sortOrder, filter)
}
