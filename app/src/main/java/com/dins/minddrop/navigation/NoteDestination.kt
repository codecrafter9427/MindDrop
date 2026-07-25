package com.dins.minddrop.navigation

import kotlinx.serialization.Serializable

sealed interface NoteDestination {
    @Serializable
    data object NoteList : NoteDestination

    @Serializable
    data object AddNote : NoteDestination

    @Serializable
    data class NoteDetail(val noteId: String) : NoteDestination

    @Serializable
    data object Settings : NoteDestination
}
