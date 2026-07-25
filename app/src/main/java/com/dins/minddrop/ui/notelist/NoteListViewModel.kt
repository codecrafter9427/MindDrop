package com.dins.minddrop.ui.notelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dins.minddrop.domain.usecase.GetAllNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val getAllNotesUseCase: GetAllNotesUseCase
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
            getAllNotesUseCase()
                .catch { e -> _uiState.value = NoteListState.Error(e.message ?: "Unknown error") }
                .collect { notes -> _uiState.value = NoteListState.Success(notes) }
        }
    }
}
