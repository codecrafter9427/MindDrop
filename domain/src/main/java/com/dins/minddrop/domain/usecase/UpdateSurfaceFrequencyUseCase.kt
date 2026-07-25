package com.dins.minddrop.domain.usecase

import com.dins.minddrop.domain.model.SurfaceFrequency
import com.dins.minddrop.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class UpdateSurfaceFrequencyUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    suspend operator fun invoke(surfaceFrequency: SurfaceFrequency) =
        repository.updateSurfaceFrequency(surfaceFrequency)
}
