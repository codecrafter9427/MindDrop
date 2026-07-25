package com.dins.minddrop.domain.usecase

import com.dins.minddrop.domain.model.UserPreferences
import com.dins.minddrop.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserPreferencesUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<UserPreferences> = repository.getUserPreferences()
}
