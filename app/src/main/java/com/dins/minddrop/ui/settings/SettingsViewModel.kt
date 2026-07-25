package com.dins.minddrop.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dins.minddrop.data.di.IoDispatcher
import com.dins.minddrop.domain.model.NoteType
import com.dins.minddrop.domain.model.SortOrder
import com.dins.minddrop.domain.model.SurfaceFrequency
import com.dins.minddrop.domain.usecase.GetUserPreferencesUseCase
import com.dins.minddrop.domain.usecase.UpdateDefaultNoteTypeUseCase
import com.dins.minddrop.domain.usecase.UpdateNotificationsEnabledUseCase
import com.dins.minddrop.domain.usecase.UpdateSortOrderUseCase
import com.dins.minddrop.domain.usecase.UpdateSurfaceFrequencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val updateSortOrderUseCase: UpdateSortOrderUseCase,
    private val updateDefaultNoteTypeUseCase: UpdateDefaultNoteTypeUseCase,
    private val updateNotificationsEnabledUseCase: UpdateNotificationsEnabledUseCase,
    private val updateSurfaceFrequencyUseCase: UpdateSurfaceFrequencyUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsState>(SettingsState.Loading)
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            getUserPreferencesUseCase()
                .catch { e -> _uiState.value = SettingsState.Error(e.message ?: "Unknown error") }
                .collect { preferences -> _uiState.value = SettingsState.Success(preferences) }
        }
    }

    fun updateSortOrder(sortOrder: SortOrder) {
        viewModelScope.launch(ioDispatcher) { updateSortOrderUseCase(sortOrder) }
    }

    fun updateDefaultNoteType(noteType: NoteType) {
        viewModelScope.launch(ioDispatcher) { updateDefaultNoteTypeUseCase(noteType) }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch(ioDispatcher) { updateNotificationsEnabledUseCase(enabled) }
    }

    fun updateSurfaceFrequency(surfaceFrequency: SurfaceFrequency) {
        viewModelScope.launch(ioDispatcher) { updateSurfaceFrequencyUseCase(surfaceFrequency) }
    }
}
