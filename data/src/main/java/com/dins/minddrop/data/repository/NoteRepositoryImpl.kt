package com.dins.minddrop.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.dins.minddrop.data.local.NoteDao
import com.dins.minddrop.data.mapper.toDomain
import com.dins.minddrop.data.mapper.toEntity
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.NoteFilter
import com.dins.minddrop.domain.model.SortOrder
import com.dins.minddrop.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> =
        noteDao.getAllNotes().map { entities -> entities.map { it.toDomain() } }

    override fun getPaginatedNotes(
        sortOrder: SortOrder,
        filter: NoteFilter
    ): Flow<PagingData<Note>> {
        val pagingSourceFactory = {
            noteDao.getPagedNotes(
                query = filter.query.trim(),
                type = filter.type?.name,
                priority = filter.priority?.name,
                sortOrder = sortOrder.name
            )
        }
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
    }

    override suspend fun getNoteById(id: String): Note? =
        noteDao.getNoteById(id)?.toDomain()

    override suspend fun addNote(note: Note) {
        noteDao.insertNote(note.toEntity())
        Log.d(TAG, "Saved note ${note.id} (type=${note.type}, priority=${note.priority})")
    }

    override suspend fun updateNote(note: Note) =
        noteDao.updateNote(note.toEntity())

    override suspend fun deleteNote(id: String) =
        noteDao.deleteNoteById(id)

    override suspend fun updateSurfaceScore(id: String, score: Float) =
        noteDao.updateSurfaceScore(id, score)

    override suspend fun updateSurfaceScores(scores: Map<String, Float>) {
        noteDao.updateSurfaceScores(scores)
        Log.d(TAG, "Updated surface scores for ${scores.size} notes")
    }

    private companion object {
        const val PAGE_SIZE = 20
        const val TAG = "NoteRepository"
    }
}
