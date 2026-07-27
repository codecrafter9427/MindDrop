package com.dins.minddrop.ui.notedetail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.NoteType
import com.dins.minddrop.domain.model.Priority
import com.dins.minddrop.domain.model.Reminder
import com.dins.minddrop.domain.model.ReminderRecurrence
import com.dins.minddrop.domain.model.ReminderType
import com.dins.minddrop.ui.reminder.ReminderSection
import com.dins.minddrop.ui.reminder.formatReminderLabel
import com.dins.minddrop.ui.reminder.openExactAlarmSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String,
    modifier: Modifier = Modifier,
    onNoteDeleted: () -> Unit = {},
    onBackClick: () -> Unit = {},
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDeleted by viewModel.isDeleted.collectAsState()
    val context = LocalContext.current
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(noteId) { viewModel.loadNote(noteId) }
    LaunchedEffect(isDeleted) { if (isDeleted) onNoteDeleted() }
    DisposableEffect(noteId) { onDispose { isEditing = false } }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit note" else "Note") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is NoteDetailState.Success && !isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
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
                is NoteDetailState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is NoteDetailState.Success -> {
                    if (isEditing) {
                        EditNoteContent(
                            note = state.note,
                            canScheduleExact = viewModel.canScheduleExactReminders(),
                            onRequestExactAlarmPermission = { context.openExactAlarmSettings() },
                            onSave = { updated ->
                                viewModel.updateNote(updated)
                                isEditing = false
                            },
                            onCancel = { isEditing = false }
                        )
                    } else {
                        NoteDetailContent(note = state.note)
                    }
                }

                is NoteDetailState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete note?") },
            text = { Text("This action can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteNote(noteId)
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun NoteDetailContent(note: Note, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = note.type.name, style = MaterialTheme.typography.labelLarge)
        Text(text = note.priority.name, style = MaterialTheme.typography.labelLarge)
        Text(text = note.content, style = MaterialTheme.typography.bodyLarge)

        note.reminder?.let { reminder ->
            val label = if (reminder.type == ReminderType.ALARM) "Alarm" else "Reminder"
            val repeat = when (reminder.recurrence) {
                ReminderRecurrence.NONE -> ""
                ReminderRecurrence.DAILY -> " (daily)"
                ReminderRecurrence.WEEKLY -> " (weekly)"
            }
            Text(
                text = "$label: ${formatReminderLabel(context, reminder.triggerAtMillis)}$repeat",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun EditNoteContent(
    note: Note,
    canScheduleExact: Boolean,
    onRequestExactAlarmPermission: () -> Unit,
    onSave: (Note) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var content by rememberSaveable(note.id) { mutableStateOf(note.content) }
    var type by rememberSaveable(note.id) { mutableStateOf(note.type) }
    var priority by rememberSaveable(note.id) { mutableStateOf(note.priority) }
    var reminderAtMillis by rememberSaveable(note.id) {
        mutableStateOf(note.reminder?.triggerAtMillis)
    }
    var reminderType by rememberSaveable(note.id) {
        mutableStateOf(note.reminder?.type ?: ReminderType.NOTIFICATION)
    }
    var reminderRecurrence by rememberSaveable(note.id) {
        mutableStateOf(note.reminder?.recurrence ?: ReminderRecurrence.NONE)
    }
    val reminder = reminderAtMillis?.let { Reminder(it, reminderType, reminderRecurrence) }

    Column(
        modifier = modifier
            .fillMaxSize()
            // Scrollable because the reminder section grows substantially once a
            // reminder is set; without this the Save button is pushed past the
            // bottom edge and becomes unreachable.
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Content") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Type")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NoteType.entries.forEach { option ->
                FilterChip(
                    selected = type == option,
                    onClick = { type = option },
                    label = { Text(option.name) }
                )
            }
        }

        Text("Priority")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Priority.entries.forEach { option ->
                FilterChip(
                    selected = priority == option,
                    onClick = { priority = option },
                    label = { Text(option.name) }
                )
            }
        }

        ReminderSection(
            reminder = reminder,
            canScheduleExact = canScheduleExact,
            onReminderChange = { updated ->
                reminderAtMillis = updated?.triggerAtMillis
                updated?.let {
                    reminderType = it.type
                    reminderRecurrence = it.recurrence
                }
            },
            onRequestExactAlarmPermission = onRequestExactAlarmPermission
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    onSave(
                        note.copy(
                            content = content,
                            type = type,
                            priority = priority,
                            reminder = reminder
                        )
                    )
                },
                enabled = content.isNotBlank()
            ) { Text("Save") }
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}
