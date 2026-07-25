package com.dins.minddrop.ui.notelist

import com.dins.minddrop.domain.model.Note

sealed class NoteListState {
    data object Loading : NoteListState()
    data class Success(val notes: List<Note>) : NoteListState()
    data class Error(val message: String) : NoteListState()
}
