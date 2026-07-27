package com.dins.minddrop.ui.addnote

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.dins.minddrop.ui.reminder.openExactAlarmSettings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    modifier: Modifier = Modifier,
    onNoteSaved: () -> Unit = {},
    onCancel: () -> Unit = {},
    viewModel: AddNoteViewModel = hiltViewModel()
) {
    var content by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(NoteType.GENERAL) }
    var priority by rememberSaveable { mutableStateOf(Priority.NORMAL) }
    // Reminder isn't Parcelable, so its two primitives are saved separately to
    // survive process death, then recombined.
    var reminderAtMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var reminderType by rememberSaveable { mutableStateOf(ReminderType.NOTIFICATION) }
    var reminderRecurrence by rememberSaveable { mutableStateOf(ReminderRecurrence.NONE) }
    val reminder = reminderAtMillis?.let { Reminder(it, reminderType, reminderRecurrence) }

    val isSaved by viewModel.isSaved.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(isSaved) {
        if (isSaved) onNoteSaved()
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            coroutineScope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.resetState() }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Add note") },
                navigationIcon = {
                    TextButton(onClick = onCancel) { Text("Cancel") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                label = { Text("What's on your mind?") },
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
                canScheduleExact = viewModel.canScheduleExactReminders(),
                onReminderChange = { updated ->
                    reminderAtMillis = updated?.triggerAtMillis
                    updated?.let {
                        reminderType = it.type
                        reminderRecurrence = it.recurrence
                    }
                },
                onRequestExactAlarmPermission = { context.openExactAlarmSettings() }
            )

            Button(
                onClick = {
                    viewModel.addNote(
                        Note(
                            content = content,
                            type = type,
                            priority = priority,
                            reminder = reminder
                        )
                    )
                },
                enabled = content.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
