package com.dins.minddrop.ml.scoring

import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.NoteType
import com.dins.minddrop.domain.model.Priority
import org.junit.Assert.assertEquals
import org.junit.Test

class SurfaceScoreCalculatorTest {

    private val calculator = SurfaceScoreCalculator()
    private val now = 1_700_000_000_000L
    private val oneDayMs = 24 * 60 * 60 * 1000L
    private val delta = 0.001f

    // Isolates each factor: no urgency keyword, viewed just now (0 staleness),
    // created long ago (no freshness bonus), viewed 3 times (no frequency bonus),
    // REFERENCE type (lowest type weight, 5 points) -- so a test overriding one
    // field can assert on a clean delta from this 5-point baseline.
    private fun baselineNote(
        content: String = "A quiet reference note",
        type: NoteType = NoteType.REFERENCE,
        createdAt: Long = now - 30 * oneDayMs,
        lastViewedAt: Long = now,
        viewCount: Int = 3
    ) = Note(
        content = content,
        type = type,
        priority = Priority.NORMAL,
        createdAt = createdAt,
        lastViewedAt = lastViewedAt,
        viewCount = viewCount
    )

    @Test
    fun `baseline note scores just its type weight`() {
        assertEquals(5f, calculator.score(baselineNote(), now), delta)
    }

    @Test
    fun `urgency keyword adds 40 points`() {
        val score = calculator.score(baselineNote(content = "This is urgent"), now)
        assertEquals(45f, score, delta)
    }

    @Test
    fun `by day pattern is detected as urgency`() {
        val score = calculator.score(baselineNote(content = "Submit report by Friday"), now)
        assertEquals(45f, score, delta)
    }

    @Test
    fun `before event pattern is detected as urgency`() {
        val score = calculator.score(baselineNote(content = "Finish this before the meeting"), now)
        assertEquals(45f, score, delta)
    }

    @Test
    fun `ordinary content has no urgency bonus`() {
        val score = calculator.score(baselineNote(content = "Buy groceries later"), now)
        assertEquals(5f, score, delta)
    }

    @Test
    fun `staleness score follows the tiered curve`() {
        assertEquals(5f, calculator.score(baselineNote(lastViewedAt = now), now), delta)
        assertEquals(
            25f,
            calculator.score(baselineNote(lastViewedAt = now - 2 * oneDayMs), now),
            delta
        )
        assertEquals(
            45f,
            calculator.score(baselineNote(lastViewedAt = now - 5 * oneDayMs), now),
            delta
        )
        assertEquals(
            65f,
            calculator.score(baselineNote(lastViewedAt = now - 10 * oneDayMs), now),
            delta
        )
    }

    @Test
    fun `never-viewed notes get the largest frequency bonus`() {
        assertEquals(20f, calculator.score(baselineNote(viewCount = 0), now), delta)
    }

    @Test
    fun `lightly viewed notes get a smaller frequency bonus`() {
        assertEquals(13f, calculator.score(baselineNote(viewCount = 1), now), delta)
    }

    @Test
    fun `frequently viewed notes get no frequency bonus`() {
        assertEquals(5f, calculator.score(baselineNote(viewCount = 5), now), delta)
    }

    @Test
    fun `freshly created notes get an age bonus`() {
        val score = calculator.score(baselineNote(createdAt = now), now)
        assertEquals(15f, score, delta)
    }

    @Test
    fun `older notes get no age bonus`() {
        val score = calculator.score(baselineNote(createdAt = now - 5 * oneDayMs), now)
        assertEquals(5f, score, delta)
    }

    @Test
    fun `type weight ranks task above reminder above idea above general above reference`() {
        assertEquals(20f, calculator.score(baselineNote(type = NoteType.TASK), now), delta)
        assertEquals(15f, calculator.score(baselineNote(type = NoteType.REMINDER), now), delta)
        assertEquals(10f, calculator.score(baselineNote(type = NoteType.IDEA), now), delta)
        assertEquals(8f, calculator.score(baselineNote(type = NoteType.GENERAL), now), delta)
        assertEquals(5f, calculator.score(baselineNote(type = NoteType.REFERENCE), now), delta)
    }

    @Test
    fun `score is clamped to 100 when many factors stack`() {
        val overloaded = Note(
            content = "URGENT deadline asap",
            type = NoteType.TASK,
            priority = Priority.URGENT,
            createdAt = now,
            lastViewedAt = now - 10 * oneDayMs,
            viewCount = 0
        )

        assertEquals(100f, calculator.score(overloaded, now), delta)
    }
}
