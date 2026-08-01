package com.dins.minddrop.ui.common

import com.dins.minddrop.domain.model.NoteType
import com.dins.minddrop.domain.model.Priority
import com.dins.minddrop.domain.model.ReminderRecurrence
import com.dins.minddrop.domain.model.ReminderType
import com.dins.minddrop.domain.model.SortOrder
import com.dins.minddrop.domain.model.SurfaceFrequency

/**
 * Human-readable labels for the domain enums.
 *
 * The UI previously rendered `enum.name` directly, which surfaced values like
 * "NEWEST_FIRST" and "PRIORITY_FIRST" to users. Mapping lives here in the UI
 * layer rather than on the enums themselves, so :domain stays free of
 * presentation concerns.
 */

val NoteType.displayName: String
    get() = when (this) {
        NoteType.TASK -> "Task"
        NoteType.IDEA -> "Idea"
        NoteType.REMINDER -> "Reminder"
        NoteType.REFERENCE -> "Reference"
        NoteType.GENERAL -> "General"
    }

val Priority.displayName: String
    get() = when (this) {
        Priority.LOW -> "Low"
        Priority.NORMAL -> "Normal"
        Priority.HIGH -> "High"
        Priority.URGENT -> "Urgent"
    }

val SortOrder.displayName: String
    get() = when (this) {
        SortOrder.NEWEST_FIRST -> "Newest first"
        SortOrder.OLDEST_FIRST -> "Oldest first"
        SortOrder.PRIORITY_FIRST -> "By priority"
    }

val SurfaceFrequency.displayName: String
    get() = when (this) {
        SurfaceFrequency.LOW -> "Rarely"
        SurfaceFrequency.MEDIUM -> "Sometimes"
        SurfaceFrequency.HIGH -> "Often"
    }

val ReminderType.displayName: String
    get() = when (this) {
        ReminderType.NOTIFICATION -> "Notification"
        ReminderType.ALARM -> "Alarm"
    }

val ReminderRecurrence.displayName: String
    get() = when (this) {
        ReminderRecurrence.NONE -> "Once"
        ReminderRecurrence.DAILY -> "Daily"
        ReminderRecurrence.WEEKLY -> "Weekly"
    }
