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

    // Search, filters and sort all live in SQL so they compose with Paging 3 --
    // filtering a PagingData client-side would mean loading the whole table.
    // Sort is parameterized via CASE rather than split across three near-identical
    // queries, so the WHERE clause exists in exactly one place. Each CASE yields
    // NULL for the non-selected sort orders, contributing nothing to the ordering,
    // so the trailing createdAt DESC both drives NEWEST_FIRST and acts as the
    // tiebreak for PRIORITY_FIRST.
    @Query(
        """
        SELECT * FROM notes
        WHERE (:query = '' OR content LIKE '%' || :query || '%')
          AND (:type IS NULL OR type = :type)
          AND (:priority IS NULL OR priority = :priority)
        ORDER BY
            CASE WHEN :sortOrder = 'OLDEST_FIRST' THEN createdAt END ASC,
            CASE WHEN :sortOrder = 'PRIORITY_FIRST' THEN
                CASE priority
                    WHEN 'URGENT' THEN 4
                    WHEN 'HIGH' THEN 3
                    WHEN 'NORMAL' THEN 2
                    WHEN 'LOW' THEN 1
                    ELSE 0
                END
            END DESC,
            createdAt DESC
        """
    )
    fun getPagedNotes(
        query: String,
        type: String?,
        priority: String?,
        sortOrder: String
    ): PagingSource<Int, NoteEntity>

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
