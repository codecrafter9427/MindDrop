package com.dins.minddrop.domain.usecase

import com.dins.minddrop.domain.repository.NoteRepository
import javax.inject.Inject

class UpdateSurfaceScoreUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: String, score: Float) = repository.updateSurfaceScore(id, score)
}
