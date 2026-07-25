package com.dins.minddrop.domain.usecase

import com.dins.minddrop.domain.repository.NoteRepository
import com.dins.minddrop.domain.scoring.SurfaceScorer
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateSurfaceScoresUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val surfaceScorer: SurfaceScorer
) {
    suspend operator fun invoke() {
        val now = System.currentTimeMillis()
        noteRepository.getAllNotes().first().forEach { note ->
            noteRepository.updateSurfaceScore(note.id, surfaceScorer.score(note, now))
        }
    }
}
