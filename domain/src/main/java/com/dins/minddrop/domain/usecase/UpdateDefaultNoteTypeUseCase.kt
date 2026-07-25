package com.dins.minddrop.domain.usecase

import com.dins.minddrop.domain.model.NoteType
import com.dins.minddrop.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class UpdateDefaultNoteTypeUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    suspend operator fun invoke(noteType: NoteType) = repository.updateDefaultNoteType(noteType)
}
