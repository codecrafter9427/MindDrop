package com.dins.minddrop.fake

import com.dins.minddrop.domain.model.NoteType
import com.dins.minddrop.domain.model.SortOrder
import com.dins.minddrop.domain.model.SurfaceFrequency
import com.dins.minddrop.domain.model.UserPreferences
import com.dins.minddrop.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserPreferencesRepository(
    initial: UserPreferences = UserPreferences()
) : UserPreferencesRepository {

    private val preferencesFlow = MutableStateFlow(initial)

    override fun getUserPreferences(): Flow<UserPreferences> = preferencesFlow

    override suspend fun updateSortOrder(sortOrder: SortOrder) {
        preferencesFlow.value = preferencesFlow.value.copy(sortOrder = sortOrder)
    }

    override suspend fun updateDefaultNoteType(noteType: NoteType) {
        preferencesFlow.value = preferencesFlow.value.copy(defaultNoteType = noteType)
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        preferencesFlow.value = preferencesFlow.value.copy(notificationsEnabled = enabled)
    }

    override suspend fun updateSurfaceFrequency(surfaceFrequency: SurfaceFrequency) {
        preferencesFlow.value = preferencesFlow.value.copy(surfaceFrequency = surfaceFrequency)
    }
}
