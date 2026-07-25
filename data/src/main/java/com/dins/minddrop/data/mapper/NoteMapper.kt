package com.dins.minddrop.data.mapper

import com.dins.minddrop.data.local.NoteEntity
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.NoteType
import com.dins.minddrop.domain.model.Priority

fun NoteEntity.toDomain(): Note = Note(
    id = id,
    content = content,
    type = NoteType.valueOf(type),
    priority = Priority.valueOf(priority),
    createdAt = createdAt,
    lastViewedAt = lastViewedAt,
    viewCount = viewCount,
    surfaceScore = surfaceScore,
    isSurfaced = isSurfaced,
    tags = tags
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    content = content,
    type = type.name,
    priority = priority.name,
    createdAt = createdAt,
    lastViewedAt = lastViewedAt,
    viewCount = viewCount,
    surfaceScore = surfaceScore,
    isSurfaced = isSurfaced,
    tags = tags
)
