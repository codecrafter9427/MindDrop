package com.dins.minddrop.domain

import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.NoteType
import com.dins.minddrop.domain.model.Priority
import com.dins.minddrop.domain.scoring.SurfaceScorer
import com.dins.minddrop.domain.usecase.UpdateSurfaceScoresUseCase
import com.dins.minddrop.fake.FakeNoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateSurfaceScoresUseCaseTest {

    private fun note(id: String) = Note(
        id = id,
        content = "Note $id",
        type = NoteType.GENERAL,
        priority = Priority.NORMAL,
        createdAt = 1_000L,
        lastViewedAt = 1_000L
    )

    private fun useCase(repository: FakeNoteRepository, scorer: SurfaceScorer) =
        UpdateSurfaceScoresUseCase(repository, scorer, UnconfinedTestDispatcher())

    @Test
    fun `writes a score for every note`() = runTest {
        val repository = FakeNoteRepository().apply {
            setNotes(listOf(note("a"), note("b"), note("c")))
        }
        val scorer = SurfaceScorer { _, _ -> 42f }

        useCase(repository, scorer)()

        assertEquals(
            listOf(42f, 42f, 42f),
            repository.getAllNotes().first().map { it.surfaceScore }
        )
    }

    @Test
    fun `writes the score each note actually earned`() = runTest {
        val repository = FakeNoteRepository().apply {
            setNotes(listOf(note("low"), note("high")))
        }
        val scorer = SurfaceScorer { note, _ -> if (note.id == "high") 90f else 10f }

        useCase(repository, scorer)()

        val scores = repository.getAllNotes().first().associate { it.id to it.surfaceScore }
        assertEquals(10f, scores["low"])
        assertEquals(90f, scores["high"])
    }

    @Test
    fun `an empty repository is a no-op`() = runTest {
        val repository = FakeNoteRepository()
        var scorerCalls = 0
        val scorer = SurfaceScorer { _, _ -> scorerCalls++; 1f }

        useCase(repository, scorer)()

        assertEquals(0, scorerCalls)
        assertEquals(emptyList<Note>(), repository.getAllNotes().first())
    }
}
