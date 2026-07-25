package com.dins.minddrop.domain.usecase

import com.dins.minddrop.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class UpdateNotificationsEnabledUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    suspend operator fun invoke(enabled: Boolean) = repository.updateNotificationsEnabled(enabled)
}
