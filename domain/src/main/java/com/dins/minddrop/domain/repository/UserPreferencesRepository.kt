package com.dins.minddrop.domain.repository

import com.dins.minddrop.domain.model.NoteType
import com.dins.minddrop.domain.model.SortOrder
import com.dins.minddrop.domain.model.SurfaceFrequency
import com.dins.minddrop.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun updateSortOrder(sortOrder: SortOrder)
    suspend fun updateDefaultNoteType(noteType: NoteType)
    suspend fun updateNotificationsEnabled(enabled: Boolean)
    suspend fun updateSurfaceFrequency(surfaceFrequency: SurfaceFrequency)
}
