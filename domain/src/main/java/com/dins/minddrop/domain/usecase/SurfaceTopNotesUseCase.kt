package com.dins.minddrop.domain.usecase

import com.dins.minddrop.domain.notification.NoteSurfacer
import com.dins.minddrop.domain.repository.NoteRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SurfaceTopNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val noteSurfacer: NoteSurfacer
) {
    suspend operator fun invoke() {
        val topNotes = noteRepository.getAllNotes().first()
            .filter { it.surfaceScore > SCORE_THRESHOLD }
            .sortedByDescending { it.surfaceScore }
            .take(MAX_SURFACED_NOTES)

        if (topNotes.isNotEmpty()) {
            noteSurfacer.surface(topNotes)
        }
    }

    private companion object {
        const val SCORE_THRESHOLD = 70f
        const val MAX_SURFACED_NOTES = 3
    }
}
