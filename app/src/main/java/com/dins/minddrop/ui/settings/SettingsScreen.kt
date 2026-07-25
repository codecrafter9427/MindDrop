package com.dins.minddrop.ui.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dins.minddrop.domain.model.NoteType
import com.dins.minddrop.domain.model.SortOrder
import com.dins.minddrop.domain.model.SurfaceFrequency
import com.dins.minddrop.domain.model.UserPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is SettingsState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is SettingsState.Success -> {
                    SettingsContent(
                        preferences = state.preferences,
                        onNotificationsEnabledChange = viewModel::updateNotificationsEnabled,
                        onSortOrderChange = viewModel::updateSortOrder,
                        onDefaultNoteTypeChange = viewModel::updateDefaultNoteType,
                        onSurfaceFrequencyChange = viewModel::updateSurfaceFrequency
                    )
                }

                is SettingsState.Error -> {
                    Text(text = state.message, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
private fun SettingsContent(
    preferences: UserPreferences,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onDefaultNoteTypeChange: (NoteType) -> Unit,
    onSurfaceFrequencyChange: (SurfaceFrequency) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Notifications")
            Switch(
                checked = preferences.notificationsEnabled,
                onCheckedChange = onNotificationsEnabledChange
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Sort order")
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortOrder.entries.forEach { option ->
                    FilterChip(
                        selected = preferences.sortOrder == option,
                        onClick = { onSortOrderChange(option) },
                        label = { Text(option.name) }
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Default note type")
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NoteType.entries.forEach { option ->
                    FilterChip(
                        selected = preferences.defaultNoteType == option,
                        onClick = { onDefaultNoteTypeChange(option) },
                        label = { Text(option.name) }
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Surface frequency")
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SurfaceFrequency.entries.forEach { option ->
                    FilterChip(
                        selected = preferences.surfaceFrequency == option,
                        onClick = { onSurfaceFrequencyChange(option) },
                        label = { Text(option.name) }
                    )
                }
            }
        }
    }
}
