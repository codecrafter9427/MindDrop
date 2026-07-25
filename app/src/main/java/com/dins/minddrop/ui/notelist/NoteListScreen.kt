package com.dins.minddrop.ui.notelist

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    modifier: Modifier = Modifier,
    onAddNoteClick: () -> Unit = {},
    onNoteClick: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: NoteListViewModel = hiltViewModel()
) {
    val lazyPagingItems = viewModel.notes.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedPriority by viewModel.selectedPriority.collectAsState()

    val filtersActive = searchQuery.isNotBlank() ||
        selectedType != null ||
        selectedPriority != null

    RequestNotificationPermission()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("MindDrop") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNoteClick) {
                Icon(Icons.Default.Add, contentDescription = "Add note")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NoteListFilters(
                searchQuery = searchQuery,
                selectedType = selectedType,
                selectedPriority = selectedPriority,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onTypeSelected = viewModel::onTypeSelected,
                onPrioritySelected = viewModel::onPrioritySelected,
                modifier = Modifier.padding(top = 8.dp)
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when (val refreshState = lazyPagingItems.loadState.refresh) {
                    is LoadState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    is LoadState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = refreshState.error.message ?: "Something went wrong")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { lazyPagingItems.retry() }) {
                                Text("Retry")
                            }
                        }
                    }

                    is LoadState.NotLoading -> {
                        if (lazyPagingItems.itemCount == 0) {
                            EmptyState(
                                filtersActive = filtersActive,
                                onClearFilters = viewModel::clearFilters,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    count = lazyPagingItems.itemCount,
                                    key = lazyPagingItems.itemKey { it.id }
                                ) { index ->
                                    val note = lazyPagingItems[index]
                                    if (note != null) {
                                        NoteCard(note = note, onClick = { onNoteClick(note.id) })
                                    }
                                }

                                if (lazyPagingItems.loadState.append is LoadState.Loading) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }

                                if (lazyPagingItems.loadState.append is LoadState.Error) {
                                    item {
                                        Text(
                                            text = "Couldn't load more notes",
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    filtersActive: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (filtersActive) {
            Text(text = "No notes match your search.", textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onClearFilters) { Text("Clear filters") }
        } else {
            Text(text = "No notes yet. Tap + to add one.", textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Respect the user's choice either way -- surfacing still works without it. */ }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
