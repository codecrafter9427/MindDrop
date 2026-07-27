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
    val tags: List<String>,
    // Stored as two nullable columns rather than an @Embedded Reminder, because
    // a nullable @Embedded would need every field nullable anyway. The mapper
    // recombines them into the domain's Reminder value object.
    val reminderAtMillis: Long? = null,
    val reminderType: String? = null,
    // Non-null with a NONE default: unlike the two columns above, "doesn't repeat"
    // is a real value rather than the absence of a reminder.
    val reminderRecurrence: String = "NONE"
)
