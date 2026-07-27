package com.dins.minddrop.ui.addnote

import com.dins.minddrop.domain.categorization.NoteCategorizer
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.NoteType
import com.dins.minddrop.domain.usecase.AddNoteUseCase
import com.dins.minddrop.domain.usecase.CategorizeNoteUseCase
import com.dins.minddrop.fake.FakeNoteRepository
import com.dins.minddrop.fake.FakeReminderScheduler
import com.dins.minddrop.fake.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddNoteViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val reminderScheduler = FakeReminderScheduler()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private fun createViewModel(
        repository: FakeNoteRepository,
        categorizedAs: NoteType = NoteType.TASK
    ) = AddNoteViewModel(
        AddNoteUseCase(repository, reminderScheduler),
        CategorizeNoteUseCase { categorizedAs },
        reminderScheduler,
        testDispatcher
    )

    @Test
    fun `a GENERAL note is auto-categorized before being saved`() = runTest {
        val repository = FakeNoteRepository()
        val viewModel = createViewModel(repository, categorizedAs = NoteType.REMINDER)

        viewModel.addNote(Note(content = "Remind me to call back", type = NoteType.GENERAL))
        testScheduler.advanceUntilIdle()

        assertEquals(NoteType.REMINDER, repository.getAllNotes().first().single().type)
        assertTrue(viewModel.isSaved.value)
    }

    @Test
    fun `an explicitly typed note keeps the user's choice`() = runTest {
        val repository = FakeNoteRepository()
        val viewModel = createViewModel(repository, categorizedAs = NoteType.TASK)

        viewModel.addNote(Note(content = "Some article to read", type = NoteType.REFERENCE))
        testScheduler.advanceUntilIdle()

        assertEquals(NoteType.REFERENCE, repository.getAllNotes().first().single().type)
    }

    @Test
    fun `resetState clears the saved flag so the screen can be reused`() = runTest {
        val viewModel = createViewModel(FakeNoteRepository())

        viewModel.addNote(Note(content = "Anything", type = NoteType.GENERAL))
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.isSaved.value)

        viewModel.resetState()

        assertEquals(false, viewModel.isSaved.value)
        assertEquals(null, viewModel.errorMessage.value)
    }
}
