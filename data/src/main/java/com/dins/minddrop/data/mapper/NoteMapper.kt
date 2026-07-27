package com.dins.minddrop.data.mapper

import com.dins.minddrop.data.local.NoteEntity
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.NoteType
import com.dins.minddrop.domain.model.Priority
import com.dins.minddrop.domain.model.Reminder
import com.dins.minddrop.domain.model.ReminderRecurrence
import com.dins.minddrop.domain.model.ReminderType

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
    tags = tags,
    // Both columns must be present to form a reminder. A half-written row (only
    // one column set) is treated as no reminder rather than crashing on a null.
    reminder = reminderAtMillis?.let { at ->
        reminderType?.let { type ->
            Reminder(
                triggerAtMillis = at,
                type = ReminderType.valueOf(type),
                // Unknown values fall back to NONE rather than throwing: a
                // downgrade or hand-edited row shouldn't make the note unreadable.
                recurrence = runCatching { ReminderRecurrence.valueOf(reminderRecurrence) }
                    .getOrDefault(ReminderRecurrence.NONE)
            )
        }
    }
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
    tags = tags,
    reminderAtMillis = reminder?.triggerAtMillis,
    reminderType = reminder?.type?.name,
    reminderRecurrence = (reminder?.recurrence ?: ReminderRecurrence.NONE).name
)
