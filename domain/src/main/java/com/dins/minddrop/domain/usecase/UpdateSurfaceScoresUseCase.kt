package com.dins.minddrop.domain.usecase

import com.dins.minddrop.domain.di.DefaultDispatcher
import com.dins.minddrop.domain.repository.NoteRepository
import com.dins.minddrop.domain.scoring.SurfaceScorer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateSurfaceScoresUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val surfaceScorer: SurfaceScorer,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke() {
        val notes = noteRepository.getAllNotes().first()
        if (notes.isEmpty()) return

        // Scoring is pure CPU work, so it belongs on Default rather than the IO
        // pool; Room's own suspend functions handle their dispatching for the
        // surrounding reads and writes.
        val scores = withContext(defaultDispatcher) {
            val now = System.currentTimeMillis()
            notes.associate { note -> note.id to surfaceScorer.score(note, now) }
        }

        // One transaction instead of one UPDATE per note -- previously an N-note
        // pass meant N commits and N Room invalidations, each rebuilding the
        // paged list while the pass was still running.
        noteRepository.updateSurfaceScores(scores)
    }
}
