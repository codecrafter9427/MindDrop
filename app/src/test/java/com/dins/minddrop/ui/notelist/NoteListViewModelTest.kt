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

    private fun note(
        id: String,
        createdAt: Long,
        priority: Priority = Priority.NORMAL,
        content: String = "Note $id",
        type: NoteType = NoteType.GENERAL
    ) = Note(
        id = id,
        content = content,
        type = type,
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

    @Test
    fun `search query narrows the list to matching content`() = runTest {
        val noteRepository = FakeNoteRepository().apply {
            setNotes(
                listOf(
                    note("groceries", createdAt = 1L, content = "Buy milk and eggs"),
                    note("report", createdAt = 2L, content = "Finish the quarterly report")
                )
            )
        }
        val viewModel = createViewModel(noteRepository, FakeUserPreferencesRepository())

        viewModel.onSearchQueryChange("milk")

        assertEquals(listOf("groceries"), viewModel.snapshotIds())
    }

    @Test
    fun `search is case insensitive`() = runTest {
        val noteRepository = FakeNoteRepository().apply {
            setNotes(listOf(note("report", createdAt = 1L, content = "Finish the Quarterly Report")))
        }
        val viewModel = createViewModel(noteRepository, FakeUserPreferencesRepository())

        viewModel.onSearchQueryChange("QUARTERLY")

        assertEquals(listOf("report"), viewModel.snapshotIds())
    }

    @Test
    fun `type filter narrows the list to that type`() = runTest {
        val noteRepository = FakeNoteRepository().apply {
            setNotes(
                listOf(
                    note("task", createdAt = 1L, type = NoteType.TASK),
                    note("idea", createdAt = 2L, type = NoteType.IDEA)
                )
            )
        }
        val viewModel = createViewModel(noteRepository, FakeUserPreferencesRepository())

        viewModel.onTypeSelected(NoteType.IDEA)

        assertEquals(listOf("idea"), viewModel.snapshotIds())
    }

    @Test
    fun `priority filter narrows the list to that priority`() = runTest {
        val noteRepository = FakeNoteRepository().apply {
            setNotes(
                listOf(
                    note("urgent", createdAt = 1L, priority = Priority.URGENT),
                    note("low", createdAt = 2L, priority = Priority.LOW)
                )
            )
        }
        val viewModel = createViewModel(noteRepository, FakeUserPreferencesRepository())

        viewModel.onPrioritySelected(Priority.URGENT)

        assertEquals(listOf("urgent"), viewModel.snapshotIds())
    }

    @Test
    fun `search and type filter apply together`() = runTest {
        val noteRepository = FakeNoteRepository().apply {
            setNotes(
                listOf(
                    note("a", createdAt = 1L, content = "Call the plumber", type = NoteType.TASK),
                    note("b", createdAt = 2L, content = "Call the dentist", type = NoteType.REMINDER),
                    note("c", createdAt = 3L, content = "Email the plumber", type = NoteType.TASK)
                )
            )
        }
        val viewModel = createViewModel(noteRepository, FakeUserPreferencesRepository())

        viewModel.onSearchQueryChange("Call")
        viewModel.onTypeSelected(NoteType.TASK)

        assertEquals(listOf("a"), viewModel.snapshotIds())
    }

    @Test
    fun `reselecting the active type chip clears the filter`() = runTest {
        val viewModel = createViewModel(FakeNoteRepository(), FakeUserPreferencesRepository())

        viewModel.onTypeSelected(NoteType.TASK)
        assertEquals(NoteType.TASK, viewModel.selectedType.value)

        viewModel.onTypeSelected(NoteType.TASK)
        assertEquals(null, viewModel.selectedType.value)

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `clearFilters resets query type and priority`() = runTest {
        val viewModel = createViewModel(FakeNoteRepository(), FakeUserPreferencesRepository())

        viewModel.onSearchQueryChange("milk")
        viewModel.onTypeSelected(NoteType.TASK)
        viewModel.onPrioritySelected(Priority.HIGH)

        viewModel.clearFilters()

        assertEquals("", viewModel.searchQuery.value)
        assertEquals(null, viewModel.selectedType.value)
        assertEquals(null, viewModel.selectedPriority.value)

        viewModel.viewModelScope.cancel()
    }
}
