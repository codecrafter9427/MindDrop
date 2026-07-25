package com.dins.minddrop.ui.notelist

import androidx.lifecycle.viewModelScope
import androidx.paging.testing.asSnapshot
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.NoteType
import com.dins.minddrop.domain.model.Priority
import com.dins.minddrop.domain.model.SortOrder
import com.dins.minddrop.domain.model.UserPreferences
import com.dins.minddrop.domain.usecase.GetPaginatedNotesUseCase
import com.dins.minddrop.domain.usecase.GetUserPreferencesUseCase
import com.dins.minddrop.fake.FakeNoteRepository
import com.dins.minddrop.fake.FakeUserPreferencesRepository
import com.dins.minddrop.fake.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

// NoteListViewModel moved from a Loading/Success/Error sealed state to
// Flow<PagingData<Note>> in Day 12 (Paging 3), so "verify Loading -> Success"
// style assertions don't map onto this ViewModel anymore -- that pattern is
// covered instead by NoteDetailViewModelTest, which still has that shape.
// What's meaningful to test here is the actual current behavior: the
// paginated flow reflects repository data, and reactively resorts when the
// sort-order preference changes.
@OptIn(ExperimentalCoroutinesApi::class)
class NoteListViewModelTest {

    // Unconfined so cachedIn()'s sharing coroutine starts eagerly rather than
    // waiting on an explicit scheduler advance; asSnapshot() drives collection
    // itself, so there's nothing here that needs Standard's step-by-step control.
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private fun note(id: String, createdAt: Long, priority: Priority = Priority.NORMAL) = Note(
        id = id,
        content = "Note $id",
        type = NoteType.GENERAL,
        priority = priority,
        createdAt = createdAt,
        lastViewedAt = createdAt
    )

    private fun createViewModel(
        noteRepository: FakeNoteRepository,
        preferencesRepository: FakeUserPreferencesRepository
    ) = NoteListViewModel(
        GetPaginatedNotesUseCase(noteRepository),
        GetUserPreferencesUseCase(preferencesRepository)
    )

    // cachedIn(viewModelScope) keeps a sharing coroutine alive for the
    // ViewModel's lifetime, which runTest would otherwise fail on as an
    // unfinished coroutine -- cancelling the scope stands in for onCleared().
    private suspend fun NoteListViewModel.snapshotIds(): List<String> =
        try {
            notes.asSnapshot().map { it.id }
        } finally {
            viewModelScope.cancel()
        }

    @Test
    fun `emits notes from the repository newest first by default`() = runTest {
        val noteRepository = FakeNoteRepository().apply {
            setNotes(listOf(note("1", createdAt = 1L), note("2", createdAt = 2L)))
        }
        val viewModel = createViewModel(noteRepository, FakeUserPreferencesRepository())

        assertEquals(listOf("2", "1"), viewModel.snapshotIds())
    }

    @Test
    fun `priority sort preference orders urgent notes first`() = runTest {
        val noteRepository = FakeNoteRepository().apply {
            setNotes(
                listOf(
                    note("low", createdAt = 1L, priority = Priority.LOW),
                    note("urgent", createdAt = 2L, priority = Priority.URGENT)
                )
            )
        }
        val preferencesRepository = FakeUserPreferencesRepository(
            initial = UserPreferences(sortOrder = SortOrder.PRIORITY_FIRST)
        )
        val viewModel = createViewModel(noteRepository, preferencesRepository)

        assertEquals(listOf("urgent", "low"), viewModel.snapshotIds())
    }

    @Test
    fun `empty repository emits no items`() = runTest {
        val viewModel = createViewModel(FakeNoteRepository(), FakeUserPreferencesRepository())

        assertEquals(emptyList<String>(), viewModel.snapshotIds())
    }
}
