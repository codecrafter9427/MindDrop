package com.dins.minddrop.ml.categorization

import com.dins.minddrop.domain.model.NoteType
import javax.inject.Inject

class KeywordNoteCategorizer @Inject constructor() {

    fun categorize(content: String): NoteType {
        val lower = content.lowercase()
        return when {
            TASK_KEYWORDS.any { lower.contains(it) } -> NoteType.TASK
            REMINDER_KEYWORDS.any { lower.contains(it) } -> NoteType.REMINDER
            IDEA_KEYWORDS.any { lower.contains(it) } -> NoteType.IDEA
            REFERENCE_KEYWORDS.any { lower.contains(it) } -> NoteType.REFERENCE
            else -> NoteType.GENERAL
        }
    }

    private companion object {
        val TASK_KEYWORDS = listOf(
            "todo", "to-do", "task", "need to", "finish", "complete", "due", "deadline"
        )
        val REMINDER_KEYWORDS = listOf(
            "remind", "remember", "don't forget", "tomorrow", "tonight", "later today"
        )
        val IDEA_KEYWORDS = listOf(
            "idea", "what if", "maybe we", "concept", "brainstorm"
        )
        val REFERENCE_KEYWORDS = listOf(
            "http://", "https://", "www.", "reference", "article", "link", "fyi"
        )
    }
}
