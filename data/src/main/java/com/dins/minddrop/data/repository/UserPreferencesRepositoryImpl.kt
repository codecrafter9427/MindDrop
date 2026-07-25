package com.dins.minddrop.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.dins.minddrop.domain.model.NoteType
import com.dins.minddrop.domain.model.SortOrder
import com.dins.minddrop.domain.model.SurfaceFrequency
import com.dins.minddrop.domain.model.UserPreferences
import com.dins.minddrop.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private object PreferencesKeys {
    val SORT_ORDER = stringPreferencesKey("sort_order")
    val DEFAULT_NOTE_TYPE = stringPreferencesKey("default_note_type")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val SURFACE_FREQUENCY = stringPreferencesKey("surface_frequency")
}

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    override fun getUserPreferences(): Flow<UserPreferences> =
        dataStore.data.map { preferences ->
            UserPreferences(
                sortOrder = preferences[PreferencesKeys.SORT_ORDER]
                    ?.let { SortOrder.valueOf(it) } ?: SortOrder.NEWEST_FIRST,
                defaultNoteType = preferences[PreferencesKeys.DEFAULT_NOTE_TYPE]
                    ?.let { NoteType.valueOf(it) } ?: NoteType.GENERAL,
                notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
                surfaceFrequency = preferences[PreferencesKeys.SURFACE_FREQUENCY]
                    ?.let { SurfaceFrequency.valueOf(it) } ?: SurfaceFrequency.MEDIUM
            )
        }

    override suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit { it[PreferencesKeys.SORT_ORDER] = sortOrder.name }
    }

    override suspend fun updateDefaultNoteType(noteType: NoteType) {
        dataStore.edit { it[PreferencesKeys.DEFAULT_NOTE_TYPE] = noteType.name }
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled }
    }

    override suspend fun updateSurfaceFrequency(surfaceFrequency: SurfaceFrequency) {
        dataStore.edit { it[PreferencesKeys.SURFACE_FREQUENCY] = surfaceFrequency.name }
    }
}
