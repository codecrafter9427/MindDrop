package com.dins.minddrop.ml.scoring

import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.NoteType
import com.dins.minddrop.domain.scoring.SurfaceScorer
import javax.inject.Inject

class SurfaceScoreCalculator @Inject constructor() : SurfaceScorer {

    override fun score(note: Note, now: Long): Float {
        val raw = urgencyScore(note.content) +
            stalenessScore(note.lastViewedAt, now) +
            viewFrequencyScore(note.viewCount) +
            ageScore(note.createdAt, now) +
            typeWeightScore(note.type)
        return raw.coerceIn(0f, 100f)
    }

    private fun urgencyScore(content: String): Float =
        if (hasUrgencyKeyword(content)) URGENCY_POINTS else 0f

    private fun hasUrgencyKeyword(content: String): Boolean {
        val lower = content.lowercase()
        if (URGENCY_KEYWORDS.any { lower.contains(it) }) return true
        if (DAYS_OF_WEEK.any { lower.contains("by $it") }) return true
        return BEFORE_EVENT_REGEX.containsMatchIn(lower)
    }

    private fun stalenessScore(lastViewedAt: Long, now: Long): Float {
        val daysSinceViewed = (now - lastViewedAt).coerceAtLeast(0L) / MILLIS_PER_DAY
        return when {
            daysSinceViewed <= 1 -> 0f
            daysSinceViewed <= 3 -> 20f
            daysSinceViewed <= 7 -> 40f
            else -> 60f
        }
    }

    private fun viewFrequencyScore(viewCount: Int): Float = when {
        viewCount == 0 -> 15f
        viewCount <= 2 -> 8f
        else -> 0f
    }

    // Freshly captured notes get a brief visibility bump so quick-capture
    // items aren't immediately buried under older, higher-scoring notes.
    private fun ageScore(createdAt: Long, now: Long): Float {
        val daysSinceCreated = (now - createdAt).coerceAtLeast(0L) / MILLIS_PER_DAY
        return if (daysSinceCreated < 1) 10f else 0f
    }

    private fun typeWeightScore(type: NoteType): Float = when (type) {
        NoteType.TASK -> 20f
        NoteType.REMINDER -> 15f
        NoteType.IDEA -> 10f
        NoteType.GENERAL -> 8f
        NoteType.REFERENCE -> 5f
    }

    private companion object {
        const val URGENCY_POINTS = 40f
        const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
        val URGENCY_KEYWORDS = listOf("urgent", "deadline", "asap", "important")
        val DAYS_OF_WEEK = listOf(
            "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"
        )
        val BEFORE_EVENT_REGEX = Regex("\\bbefore\\s+\\w+")
    }
}
