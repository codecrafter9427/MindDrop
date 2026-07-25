package com.dins.minddrop.ui.notedetail

import app.cash.turbine.test
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.NoteType
import com.dins.minddrop.domain.model.Priority
import com.dins.minddrop.domain.usecase.DeleteNoteUseCase
import com.dins.minddrop.domain.usecase.GetNoteByIdUseCase
import com.dins.minddrop.domain.usecase.UpdateNoteUseCase
import com.dins.minddrop.fake.FakeNoteRepository
import com.dins.minddrop.fake.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val existingNote = Note(
        id = "note-1",
        content = "Existing note",
        type = NoteType.TASK,
        priority = Priority.HIGH,
        createdAt = 1_000L,
        lastViewedAt = 1_000L
    )

    private fun createViewModel(repository: FakeNoteRepository) = NoteDetailViewModel(
        GetNoteByIdUseCase(repository),
        UpdateNoteUseCase(repository),
        DeleteNoteUseCase(repository),
        testDispatcher
    )

    @Test
    fun `uiState transitions from Loading to Success when the note exists`() = runTest {
        val repository = FakeNoteRepository().apply { setNotes(listOf(existingNote)) }
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(NoteDetailState.Loading, awaitItem())

            viewModel.loadNote(existingNote.id)

            assertEquals(NoteDetailState.Success(existingNote), awaitItem())
        }
    }

    @Test
    fun `uiState transitions from Loading to Error when the note is missing`() = runTest {
        val viewModel = createViewModel(FakeNoteRepository())

        viewModel.uiState.test {
            assertEquals(NoteDetailState.Loading, awaitItem())

            viewModel.loadNote("does-not-exist")

            assertEquals(NoteDetailState.Error("Note not found"), awaitItem())
        }
    }

    @Test
    fun `uiState transitions from Loading to Error when the repository throws`() = runTest {
        val repository = FakeNoteRepository().apply {
            getNoteByIdError = IllegalStateException("Database unavailable")
        }
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            assertEquals(NoteDetailState.Loading, awaitItem())

            viewModel.loadNote(existingNote.id)

            assertEquals(NoteDetailState.Error("Database unavailable"), awaitItem())
        }
    }

    @Test
    fun `updateNote persists the change and reflects it in uiState`() = runTest {
        val repository = FakeNoteRepository().apply { setNotes(listOf(existingNote)) }
        val viewModel = createViewModel(repository)
        val edited = existingNote.copy(content = "Edited content")

        viewModel.updateNote(edited)
        testScheduler.advanceUntilIdle()

        assertEquals(NoteDetailState.Success(edited), viewModel.uiState.value)
        assertEquals("Edited content", repository.getNoteById(existingNote.id)?.content)
    }

    @Test
    fun `deleteNote removes the note and flags isDeleted`() = runTest {
        val repository = FakeNoteRepository().apply { setNotes(listOf(existingNote)) }
        val viewModel = createViewModel(repository)

        viewModel.deleteNote(existingNote.id)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.isDeleted.value)
        assertEquals(null, repository.getNoteById(existingNote.id))
    }
}
