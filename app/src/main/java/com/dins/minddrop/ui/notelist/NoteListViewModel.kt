package com.dins.minddrop.ui.notelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.SortOrder
import com.dins.minddrop.domain.usecase.GetAllNotesUseCase
import com.dins.minddrop.domain.usecase.GetUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val getAllNotesUseCase: GetAllNotesUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<NoteListState>(NoteListState.Loading)
    val uiState: StateFlow<NoteListState> = _uiState.asStateFlow()

    init {
        observeNotes()
    }

    fun retry() {
        observeNotes()
    }

    private fun observeNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                getAllNotesUseCase(),
                getUserPreferencesUseCase()
            ) { notes, preferences -> sortNotes(notes, preferences.sortOrder) }
                .catch { e -> _uiState.value = NoteListState.Error(e.message ?: "Unknown error") }
                .collect { notes -> _uiState.value = NoteListState.Success(notes) }
        }
    }

    private fun sortNotes(notes: List<Note>, sortOrder: SortOrder): List<Note> =
        when (sortOrder) {
            SortOrder.NEWEST_FIRST -> notes.sortedByDescending { it.createdAt }
            SortOrder.OLDEST_FIRST -> notes.sortedBy { it.createdAt }
            SortOrder.PRIORITY_FIRST -> notes.sortedByDescending { it.priority.ordinal }
        }
}
