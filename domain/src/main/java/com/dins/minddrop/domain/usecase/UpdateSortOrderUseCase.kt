package com.dins.minddrop.domain.usecase

import com.dins.minddrop.domain.model.SortOrder
import com.dins.minddrop.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class UpdateSortOrderUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    suspend operator fun invoke(sortOrder: SortOrder) = repository.updateSortOrder(sortOrder)
}
