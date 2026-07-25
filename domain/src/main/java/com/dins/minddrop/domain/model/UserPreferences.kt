package com.dins.minddrop.domain.model

data class UserPreferences(
    val sortOrder: SortOrder = SortOrder.NEWEST_FIRST,
    val defaultNoteType: NoteType = NoteType.GENERAL,
    val notificationsEnabled: Boolean = true,
    val surfaceFrequency: SurfaceFrequency = SurfaceFrequency.MEDIUM
)

enum class SortOrder { NEWEST_FIRST, OLDEST_FIRST, PRIORITY_FIRST }

enum class SurfaceFrequency { LOW, MEDIUM, HIGH }
