package com.dins.minddrop.domain.categorization

import com.dins.minddrop.domain.model.NoteType

interface NoteCategorizer {
    fun categorize(content: String): NoteType
}
