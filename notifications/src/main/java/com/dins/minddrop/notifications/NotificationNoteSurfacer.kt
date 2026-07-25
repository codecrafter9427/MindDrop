package com.dins.minddrop.notifications

import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.notification.NoteSurfacer
import javax.inject.Inject

class NotificationNoteSurfacer @Inject constructor(
    private val notificationHelper: NotificationHelper
) : NoteSurfacer {

    override suspend fun surface(notes: List<Note>) {
        notes.forEach { note -> notificationHelper.showNotification(note) }
    }
}
