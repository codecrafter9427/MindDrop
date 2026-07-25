package com.dins.minddrop.ui.notedetail

import com.dins.minddrop.domain.model.Note

sealed class NoteDetailState {
    data object Loading : NoteDetailState()
    data class Success(val note: Note) : NoteDetailState()
    data class Error(val message: String) : NoteDetailState()
}
