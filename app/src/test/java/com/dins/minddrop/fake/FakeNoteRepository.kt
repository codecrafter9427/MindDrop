package com.dins.minddrop.fake

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.SortOrder
import com.dins.minddrop.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeNoteRepository : NoteRepository {

    private val notesFlow = MutableStateFlow<List<Note>>(emptyList())
    var getNoteByIdError: Throwable? = null

    fun setNotes(notes: List<Note>) {
        notesFlow.value = notes
    }

    override fun getAllNotes(): Flow<List<Note>> = notesFlow

    override fun getPaginatedNotes(sortOrder: SortOrder): Flow<PagingData<Note>> =
        notesFlow.map { notes ->
            // Terminal load states are required: without them refresh stays
            // Loading forever and asSnapshot() in tests never returns.
            PagingData.from(
                data = sortNotes(notes, sortOrder),
                sourceLoadStates = LoadStates(
                    refresh = LoadState.NotLoading(endOfPaginationReached = true),
                    prepend = LoadState.NotLoading(endOfPaginationReached = true),
                    append = LoadState.NotLoading(endOfPaginationReached = true)
                )
            )
        }

    override suspend fun getNoteById(id: String): Note? {
        getNoteByIdError?.let { throw it }
        return notesFlow.value.find { it.id == id }
    }

    override suspend fun addNote(note: Note) {
        notesFlow.value = notesFlow.value + note
    }

    override suspend fun updateNote(note: Note) {
        notesFlow.value = notesFlow.value.map { if (it.id == note.id) note else it }
    }

    override suspend fun deleteNote(id: String) {
        notesFlow.value = notesFlow.value.filterNot { it.id == id }
    }

    override suspend fun updateSurfaceScore(id: String, score: Float) {
        notesFlow.value = notesFlow.value.map {
            if (it.id == id) it.copy(surfaceScore = score) else it
        }
    }

    private fun sortNotes(notes: List<Note>, sortOrder: SortOrder): List<Note> =
        when (sortOrder) {
            SortOrder.NEWEST_FIRST -> notes.sortedByDescending { it.createdAt }
            SortOrder.OLDEST_FIRST -> notes.sortedBy { it.createdAt }
            SortOrder.PRIORITY_FIRST -> notes.sortedByDescending { it.priority.ordinal }
        }
}
