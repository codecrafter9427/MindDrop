package com.dins.minddrop.domain.usecase

import com.dins.minddrop.domain.categorization.NoteCategorizer
import com.dins.minddrop.domain.model.NoteType
import javax.inject.Inject

class CategorizeNoteUseCase @Inject constructor(
    private val noteCategorizer: NoteCategorizer
) {
    operator fun invoke(content: String): NoteType = noteCategorizer.categorize(content)
}
