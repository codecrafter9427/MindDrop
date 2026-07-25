package com.dins.minddrop.ml.categorization

import com.dins.minddrop.domain.categorization.NoteCategorizer
import com.dins.minddrop.domain.model.NoteType
import javax.inject.Inject

class CompositeNoteCategorizer @Inject constructor(
    private val tfLiteNoteCategorizer: TfLiteNoteCategorizer,
    private val keywordNoteCategorizer: KeywordNoteCategorizer
) : NoteCategorizer {

    override fun categorize(content: String): NoteType =
        tfLiteNoteCategorizer.categorize(content) ?: keywordNoteCategorizer.categorize(content)
}
