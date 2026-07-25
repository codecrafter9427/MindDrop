package com.dins.minddrop.domain.repository

import androidx.paging.PagingData
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.NoteFilter
import com.dins.minddrop.domain.model.SortOrder
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    fun getPaginatedNotes(
        sortOrder: SortOrder,
        filter: NoteFilter = NoteFilter()
    ): Flow<PagingData<Note>>
    suspend fun getNoteById(id: String): Note?
    suspend fun addNote(note: Note)
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(id: String)
    suspend fun updateSurfaceScore(id: String, score: Float)

    /** Applies every score in one transaction; see [updateSurfaceScore] for single updates. */
    suspend fun updateSurfaceScores(scores: Map<String, Float>)
}
