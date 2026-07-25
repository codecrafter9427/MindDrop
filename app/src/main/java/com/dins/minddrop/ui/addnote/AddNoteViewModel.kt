package com.dins.minddrop.ui.addnote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.usecase.AddNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val addNoteUseCase: AddNoteUseCase
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    fun addNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                addNoteUseCase(note)
                _isSaved.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to save note"
            }
        }
    }

    fun resetState() {
        _isSaved.value = false
        _errorMessage.value = null
    }
}
