package com.dins.minddrop.ui.notelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.usecase.GetPaginatedNotesUseCase
import com.dins.minddrop.domain.usecase.GetUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    getPaginatedNotesUseCase: GetPaginatedNotesUseCase,
    getUserPreferencesUseCase: GetUserPreferencesUseCase
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: Flow<PagingData<Note>> =
        getUserPreferencesUseCase()
            .flatMapLatest { preferences -> getPaginatedNotesUseCase(preferences.sortOrder) }
            .cachedIn(viewModelScope)
}
