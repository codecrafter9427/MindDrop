package com.dins.minddrop.domain.model

import java.util.UUID

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val type: NoteType = NoteType.GENERAL,
    val priority: Priority = Priority.NORMAL,
    val createdAt: Long = System.currentTimeMillis(),
    val lastViewedAt: Long = createdAt,
    val viewCount: Int = 0,
    val surfaceScore: Float = 0f,
    val isSurfaced: Boolean = false,
    val tags: List<String> = emptyList(),
    /** User-set reminder, or null if this note has none. */
    val reminder: Reminder? = null
)

enum class NoteType { TASK, IDEA, REMINDER, REFERENCE, GENERAL }

enum class Priority { LOW, NORMAL, HIGH, URGENT }
