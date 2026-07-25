package com.dins.minddrop.domain.notification

import com.dins.minddrop.domain.model.Note

interface NoteSurfacer {
    suspend fun surface(notes: List<Note>)
}
