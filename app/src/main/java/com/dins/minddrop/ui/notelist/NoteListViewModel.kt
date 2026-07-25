package com.dins.minddrop.ui.notelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.NoteFilter
import com.dins.minddrop.domain.model.NoteType
import com.dins.minddrop.domain.model.Priority
import com.dins.minddrop.domain.usecase.GetPaginatedNotesUseCase
import com.dins.minddrop.domain.usecase.GetUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    getPaginatedNotesUseCase: GetPaginatedNotesUseCase,
    getUserPreferencesUseCase: GetUserPreferencesUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow<NoteType?>(null)
    val selectedType: StateFlow<NoteType?> = _selectedType.asStateFlow()

    private val _selectedPriority = MutableStateFlow<Priority?>(null)
    val selectedPriority: StateFlow<Priority?> = _selectedPriority.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val notes: Flow<PagingData<Note>> = combine(
        getUserPreferencesUseCase(),
        // Debounce only non-empty input: a cleared or initial query should
        // restore the full list immediately rather than after a dead pause.
        _searchQuery.debounce { query -> if (query.isEmpty()) 0L else SEARCH_DEBOUNCE_MS },
        _selectedType,
        _selectedPriority
    ) { preferences, query, type, priority ->
        preferences.sortOrder to NoteFilter(query = query, type = type, priority = priority)
    }
        // flatMapLatest cancels the in-flight query when the criteria change again,
        // so a fast typist never pays for intermediate searches.
        .flatMapLatest { (sortOrder, filter) -> getPaginatedNotesUseCase(sortOrder, filter) }
        .cachedIn(viewModelScope)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onTypeSelected(type: NoteType?) {
        // Tapping the active chip clears it, so the row doubles as its own "all" option.
        _selectedType.value = if (_selectedType.value == type) null else type
    }

    fun onPrioritySelected(priority: Priority?) {
        _selectedPriority.value = if (_selectedPriority.value == priority) null else priority
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedType.value = null
        _selectedPriority.value = null
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
