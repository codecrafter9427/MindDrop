package com.dins.minddrop.fake

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.NoteFilter
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

    override fun getPaginatedNotes(
        sortOrder: SortOrder,
        filter: NoteFilter
    ): Flow<PagingData<Note>> =
        notesFlow.map { notes ->
            // Terminal load states are required: without them refresh stays
            // Loading forever and asSnapshot() in tests never returns.
            PagingData.from(
                data = sortNotes(applyFilter(notes, filter), sortOrder),
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

    override suspend fun updateSurfaceScores(scores: Map<String, Float>) {
        // Single assignment, mirroring the real implementation's single
        // transaction -- collectors see one emission, not one per note.
        notesFlow.value = notesFlow.value.map { note ->
            scores[note.id]?.let { note.copy(surfaceScore = it) } ?: note
        }
    }

    // Mirrors the WHERE clause in NoteDao.getPagedNotes: substring match on
    // content (case-insensitive, as SQLite's LIKE is for ASCII) plus exact
    // type/priority matches, with blank/null meaning "no restriction".
    private fun applyFilter(notes: List<Note>, filter: NoteFilter): List<Note> =
        notes.filter { note ->
            val matchesQuery = filter.query.isBlank() ||
                note.content.contains(filter.query.trim(), ignoreCase = true)
            val matchesType = filter.type == null || note.type == filter.type
            val matchesPriority = filter.priority == null || note.priority == filter.priority
            matchesQuery && matchesType && matchesPriority
        }

    private fun sortNotes(notes: List<Note>, sortOrder: SortOrder): List<Note> =
        when (sortOrder) {
            SortOrder.NEWEST_FIRST -> notes.sortedByDescending { it.createdAt }
            SortOrder.OLDEST_FIRST -> notes.sortedBy { it.createdAt }
            SortOrder.PRIORITY_FIRST -> notes.sortedByDescending { it.priority.ordinal }
        }
}
