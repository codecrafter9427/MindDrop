package com.dins.minddrop.domain

import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.NoteType
import com.dins.minddrop.domain.model.Priority
import com.dins.minddrop.domain.model.Reminder
import com.dins.minddrop.domain.model.ReminderType
import com.dins.minddrop.domain.usecase.AddNoteUseCase
import com.dins.minddrop.domain.usecase.DeleteNoteUseCase
import com.dins.minddrop.domain.usecase.RescheduleRemindersUseCase
import com.dins.minddrop.domain.usecase.UpdateNoteUseCase
import com.dins.minddrop.fake.FakeNoteRepository
import com.dins.minddrop.fake.FakeReminderScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the invariant that persisting a note also arms or clears its reminder --
 * the reason scheduling lives in the use cases rather than the ViewModels.
 */
class ReminderSchedulingTest {

    private val hourFromNow = System.currentTimeMillis() + 60 * 60 * 1000L
    private val hourAgo = System.currentTimeMillis() - 60 * 60 * 1000L

    private fun note(id: String, reminder: Reminder? = null) = Note(
        id = id,
        content = "Note $id",
        type = NoteType.GENERAL,
        priority = Priority.NORMAL,
        createdAt = 1_000L,
        lastViewedAt = 1_000L,
        reminder = reminder
    )

    @Test
    fun `adding a note with a reminder schedules it`() = runTest {
        val repository = FakeNoteRepository()
        val scheduler = FakeReminderScheduler()
        val reminder = Reminder(hourFromNow, ReminderType.ALARM)

        AddNoteUseCase(repository, scheduler)(note("a", reminder))

        assertEquals(reminder, scheduler.scheduled["a"])
    }

    @Test
    fun `adding a note without a reminder schedules nothing`() = runTest {
        val scheduler = FakeReminderScheduler()

        AddNoteUseCase(FakeNoteRepository(), scheduler)(note("a"))

        assertNull(scheduler.scheduled["a"])
    }

    @Test
    fun `editing a note to add a reminder schedules it`() = runTest {
        val repository = FakeNoteRepository().apply { setNotes(listOf(note("a"))) }
        val scheduler = FakeReminderScheduler()
        val reminder = Reminder(hourFromNow, ReminderType.NOTIFICATION)

        UpdateNoteUseCase(repository, scheduler)(note("a", reminder))

        assertEquals(reminder, scheduler.scheduled["a"])
    }

    @Test
    fun `clearing a note's reminder cancels the pending alarm`() = runTest {
        val existing = Reminder(hourFromNow, ReminderType.ALARM)
        val repository = FakeNoteRepository().apply { setNotes(listOf(note("a", existing))) }
        val scheduler = FakeReminderScheduler()
        scheduler.schedule(note("a", existing))

        UpdateNoteUseCase(repository, scheduler)(note("a", reminder = null))

        assertNull(scheduler.scheduled["a"])
        assertTrue(scheduler.cancelled.contains("a"))
    }

    @Test
    fun `changing the reminder type replaces the schedule rather than stacking`() = runTest {
        val repository = FakeNoteRepository().apply { setNotes(listOf(note("a"))) }
        val scheduler = FakeReminderScheduler()
        val useCase = UpdateNoteUseCase(repository, scheduler)

        useCase(note("a", Reminder(hourFromNow, ReminderType.NOTIFICATION)))
        useCase(note("a", Reminder(hourFromNow, ReminderType.ALARM)))

        assertEquals(1, scheduler.scheduled.size)
        assertEquals(ReminderType.ALARM, scheduler.scheduled["a"]?.type)
    }

    @Test
    fun `deleting a note cancels its reminder`() = runTest {
        val reminder = Reminder(hourFromNow, ReminderType.ALARM)
        val repository = FakeNoteRepository().apply { setNotes(listOf(note("a", reminder))) }
        val scheduler = FakeReminderScheduler()
        scheduler.schedule(note("a", reminder))

        DeleteNoteUseCase(repository, scheduler)("a")

        assertNull(scheduler.scheduled["a"])
        assertTrue(scheduler.cancelled.contains("a"))
    }

    @Test
    fun `reschedule re-arms future reminders after a reboot`() = runTest {
        val future = Reminder(hourFromNow, ReminderType.ALARM)
        val repository = FakeNoteRepository().apply {
            setNotes(listOf(note("future", future), note("plain")))
        }
        val scheduler = FakeReminderScheduler()

        RescheduleRemindersUseCase(repository, scheduler)()

        assertEquals(future, scheduler.scheduled["future"])
        assertNull(scheduler.scheduled["plain"])
    }

    @Test
    fun `reschedule skips reminders whose time already passed`() = runTest {
        val repository = FakeNoteRepository().apply {
            setNotes(listOf(note("stale", Reminder(hourAgo, ReminderType.ALARM))))
        }
        val scheduler = FakeReminderScheduler()

        RescheduleRemindersUseCase(repository, scheduler)()

        // Firing a reminder for a time long past is noise, not a notification.
        assertFalse(scheduler.scheduled.containsKey("stale"))
    }
}
