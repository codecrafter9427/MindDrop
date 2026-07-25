package com.dins.minddrop.ml.categorization

import com.dins.minddrop.domain.model.NoteType
import org.junit.Assert.assertEquals
import org.junit.Test

class KeywordNoteCategorizerTest {

    private val categorizer = KeywordNoteCategorizer()

    @Test
    fun `todo keyword categorizes as task`() {
        assertEquals(NoteType.TASK, categorizer.categorize("Todo: finish the report"))
    }

    @Test
    fun `deadline keyword categorizes as task`() {
        assertEquals(NoteType.TASK, categorizer.categorize("Submit by the deadline Friday"))
    }

    @Test
    fun `remind keyword categorizes as reminder`() {
        assertEquals(NoteType.REMINDER, categorizer.categorize("Remind me to call mom"))
    }

    @Test
    fun `tomorrow keyword categorizes as reminder`() {
        assertEquals(NoteType.REMINDER, categorizer.categorize("Pick up dry cleaning tomorrow"))
    }

    @Test
    fun `idea keyword categorizes as idea`() {
        assertEquals(NoteType.IDEA, categorizer.categorize("Idea: a notes app that resurfaces itself"))
    }

    @Test
    fun `what if phrase categorizes as idea`() {
        assertEquals(NoteType.IDEA, categorizer.categorize("What if we added dark mode"))
    }

    @Test
    fun `url categorizes as reference`() {
        assertEquals(NoteType.REFERENCE, categorizer.categorize("Check out https://example.com/article"))
    }

    @Test
    fun `fyi keyword categorizes as reference`() {
        assertEquals(NoteType.REFERENCE, categorizer.categorize("FYI the office moved buildings"))
    }

    @Test
    fun `content with no keyword matches falls back to general`() {
        assertEquals(NoteType.GENERAL, categorizer.categorize("Just a plain thought"))
    }

    @Test
    fun `matching is case insensitive`() {
        assertEquals(NoteType.TASK, categorizer.categorize("TODO buy groceries"))
    }

    @Test
    fun `task keywords are checked before reminder keywords`() {
        // Contains both a task and a reminder signal -- task wins by priority order.
        assertEquals(NoteType.TASK, categorizer.categorize("Remind me to finish the todo list"))
    }
}
