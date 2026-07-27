package com.dins.minddrop.ui.reminder

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dins.minddrop.domain.model.Reminder
import com.dins.minddrop.domain.model.ReminderRecurrence
import com.dins.minddrop.domain.model.ReminderType
import java.util.Calendar

/**
 * Lets the user attach a one-time reminder to a note, choosing whether it
 * arrives as a notification or rings as an alarm.
 *
 * Uses Calendar rather than java.time: minSdk is 24 and core library
 * desugaring isn't enabled, so java.time would crash on API 24-25.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSection(
    reminder: Reminder?,
    canScheduleExact: Boolean,
    onReminderChange: (Reminder?) -> Unit,
    onRequestExactAlarmPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    // Held between the two dialogs: the date is chosen first, then the time is
    // applied to it before a Reminder is emitted.
    var pendingDateMillis by remember { mutableStateOf<Long?>(null) }
    var showPastTimeWarning by remember { mutableStateOf(false) }

    // Default a new reminder to a short way ahead rather than "now". The time is
    // stored with seconds zeroed, so pre-filling the current time would make the
    // default selection always a few seconds in the past -- accepting it would be
    // rejected as a past time every single time. Computed once so the date and
    // time pickers agree, including when the lead time crosses midnight.
    val defaultTriggerMillis = remember {
        System.currentTimeMillis() + DEFAULT_LEAD_MINUTES * 60 * 1000L
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Reminder", style = MaterialTheme.typography.titleMedium)

        if (reminder == null) {
            OutlinedButton(onClick = { showDatePicker = true }) {
                Text("Add reminder")
            }
        } else {
            Text(
                text = formatReminderLabel(context, reminder.triggerAtMillis),
                style = MaterialTheme.typography.bodyLarge
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReminderType.entries.forEach { type ->
                    FilterChip(
                        selected = reminder.type == type,
                        onClick = { onReminderChange(reminder.copy(type = type)) },
                        label = {
                            Text(if (type == ReminderType.ALARM) "Alarm" else "Notification")
                        }
                    )
                }
            }

            Text(
                text = if (reminder.type == ReminderType.ALARM) {
                    "Rings on the alarm volume, even if your ringer is silent."
                } else {
                    "Arrives as a normal notification."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text("Repeat", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReminderRecurrence.entries.forEach { option ->
                    FilterChip(
                        selected = reminder.recurrence == option,
                        onClick = { onReminderChange(reminder.copy(recurrence = option)) },
                        label = { Text(recurrenceLabel(option)) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { showDatePicker = true }) { Text("Change") }
                TextButton(onClick = { onReminderChange(null) }) { Text("Remove") }
            }

            if (!canScheduleExact) {
                Text(
                    text = "Exact alarms are turned off, so this may arrive late.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                TextButton(onClick = onRequestExactAlarmPermission) {
                    Text("Allow exact alarms")
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = reminder?.triggerAtMillis ?: defaultTriggerMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                        showTimePicker = true
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val initial = Calendar.getInstance().apply {
            timeInMillis = reminder?.triggerAtMillis ?: defaultTriggerMillis
        }
        val timePickerState = rememberTimePickerState(
            initialHour = initial.get(Calendar.HOUR_OF_DAY),
            initialMinute = initial.get(Calendar.MINUTE),
            is24Hour = DateFormat.is24HourFormat(context)
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val triggerAt = combine(
                        dateMillis = pendingDateMillis ?: defaultTriggerMillis,
                        hour = timePickerState.hour,
                        minute = timePickerState.minute
                    )
                    showTimePicker = false
                    // Refuse rather than silently accept: a past reminder would
                    // fire instantly, which reads as a bug.
                    if (triggerAt <= System.currentTimeMillis()) {
                        showPastTimeWarning = true
                    } else {
                        onReminderChange(
                            Reminder(
                                triggerAtMillis = triggerAt,
                                type = reminder?.type ?: ReminderType.NOTIFICATION,
                                // Preserved so changing the time doesn't silently
                                // drop a repeat the user already chose.
                                recurrence = reminder?.recurrence ?: ReminderRecurrence.NONE
                            )
                        )
                    }
                }) { Text("Set") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            title = { Text("Reminder time") },
            text = { TimePicker(state = timePickerState) }
        )
    }

    if (showPastTimeWarning) {
        AlertDialog(
            onDismissRequest = { showPastTimeWarning = false },
            confirmButton = {
                // Straight back to the time picker: the chosen date is retained in
                // pendingDateMillis and the usual mistake is the time of day, so
                // re-picking the date would be needless friction.
                TextButton(onClick = {
                    showPastTimeWarning = false
                    showTimePicker = true
                }) { Text("Change time") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPastTimeWarning = false
                    showDatePicker = true
                }) { Text("Change date") }
            },
            title = { Text("That time has passed") },
            text = { Text("Choose a date and time in the future.") }
        )
    }
}

/** How far ahead a brand-new reminder is pre-filled. */
private const val DEFAULT_LEAD_MINUTES = 10L

private fun recurrenceLabel(recurrence: ReminderRecurrence): String = when (recurrence) {
    ReminderRecurrence.NONE -> "Once"
    ReminderRecurrence.DAILY -> "Daily"
    ReminderRecurrence.WEEKLY -> "Weekly"
}

/** Merges the date from the date picker with the hour and minute from the time picker. */
private fun combine(dateMillis: Long, hour: Int, minute: Int): Long {
    // The date picker reports UTC midnight, so the calendar day is read in UTC
    // and then rebuilt in the device's zone -- otherwise a user east or west of
    // UTC can land a day off.
    val utc = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = dateMillis
    }
    return Calendar.getInstance().apply {
        set(Calendar.YEAR, utc.get(Calendar.YEAR))
        set(Calendar.MONTH, utc.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, utc.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

/**
 * Formats a reminder timestamp using the device's date and time preferences,
 * so a 24-hour-clock user doesn't get AM/PM.
 */
fun formatReminderLabel(context: android.content.Context, triggerAtMillis: Long): String {
    val date = java.util.Date(triggerAtMillis)
    val dateText = DateFormat.getMediumDateFormat(context).format(date)
    val timeText = DateFormat.getTimeFormat(context).format(date)
    return "$dateText at $timeText"
}
