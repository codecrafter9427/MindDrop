package com.dins.minddrop.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val content: String,
    val type: String,
    val priority: String,
    val createdAt: Long,
    val lastViewedAt: Long,
    val viewCount: Int,
    val surfaceScore: Float,
    val isSurfaced: Boolean,
    val tags: List<String>
)
