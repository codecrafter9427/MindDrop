package com.dins.minddrop.ui.notedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.usecase.DeleteNoteUseCase
import com.dins.minddrop.domain.usecase.GetNoteByIdUseCase
import com.dins.minddrop.domain.usecase.UpdateNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<NoteDetailState>(NoteDetailState.Loading)
    val uiState: StateFlow<NoteDetailState> = _uiState.asStateFlow()

    private val _isDeleted = MutableStateFlow(false)
    val isDeleted: StateFlow<Boolean> = _isDeleted.asStateFlow()

    fun loadNote(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val note = getNoteByIdUseCase(id)
                _uiState.value = if (note != null) {
                    NoteDetailState.Success(note)
                } else {
                    NoteDetailState.Error("Note not found")
                }
            } catch (e: Exception) {
                _uiState.value = NoteDetailState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                updateNoteUseCase(note)
                _uiState.value = NoteDetailState.Success(note)
            } catch (e: Exception) {
                _uiState.value = NoteDetailState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                deleteNoteUseCase(id)
                _isDeleted.value = true
            } catch (e: Exception) {
                _uiState.value = NoteDetailState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
