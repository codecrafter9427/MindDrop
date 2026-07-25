package com.dins.minddrop.domain.categorization

import com.dins.minddrop.domain.model.NoteType

/** Single-purpose categorization contract; `fun interface` so tests can supply a lambda. */
fun interface NoteCategorizer {
    fun categorize(content: String): NoteType
}
