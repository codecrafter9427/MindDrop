package com.dins.minddrop.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getPagedNotesNewestFirst(): PagingSource<Int, NoteEntity>

    @Query("SELECT * FROM notes ORDER BY createdAt ASC")
    fun getPagedNotesOldestFirst(): PagingSource<Int, NoteEntity>

    @Query(
        """
        SELECT * FROM notes
        ORDER BY
            CASE priority
                WHEN 'URGENT' THEN 4
                WHEN 'HIGH' THEN 3
                WHEN 'NORMAL' THEN 2
                WHEN 'LOW' THEN 1
                ELSE 0
            END DESC
        """
    )
    fun getPagedNotesByPriority(): PagingSource<Int, NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)

    @Query("UPDATE notes SET surfaceScore = :score WHERE id = :id")
    suspend fun updateSurfaceScore(id: String, score: Float)
}
