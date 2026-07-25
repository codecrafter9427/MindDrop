package com.dins.minddrop.notifications

import android.util.Log
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.notification.NoteSurfacer
import javax.inject.Inject

class LoggingNoteSurfacer @Inject constructor() : NoteSurfacer {

    override suspend fun surface(notes: List<Note>) {
        notes.forEach { note ->
            Log.d(TAG, "Surfacing note ${note.id} (score=${note.surfaceScore}): ${note.content}")
        }
    }

    private companion object {
        const val TAG = "NoteSurfacer"
    }
}
