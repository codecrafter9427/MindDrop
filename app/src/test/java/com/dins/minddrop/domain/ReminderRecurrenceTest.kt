package com.dins.minddrop.domain

import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.NoteType
import com.dins.minddrop.domain.model.Priority
import com.dins.minddrop.domain.model.Reminder
import com.dins.minddrop.domain.model.ReminderRecurrence
import com.dins.minddrop.domain.model.ReminderType
import com.dins.minddrop.domain.model.nextOccurrenceAfter
import com.dins.minddrop.domain.usecase.HandleFiredReminderUseCase
import com.dins.minddrop.domain.usecase.RescheduleRemindersUseCase
import com.dins.minddrop.fake.FakeNoteRepository
import com.dins.minddrop.fake.FakeReminderScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ReminderRecurrenceTest {

    private val dayMillis = 24 * 60 * 60 * 1000L

    private fun note(id: String, reminder: Reminder?) = Note(
        id = id,
        content = "Note $id",
        type = NoteType.GENERAL,
        priority = Priority.NORMAL,
        createdAt = 1_000L,
        lastViewedAt = 1_000L,
        reminder = reminder
    )

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            set(year, month, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    @Test
    fun `a one-time reminder has no next occurrence`() {
        val reminder = Reminder(at(2026, 6, 25, 21, 0), ReminderType.ALARM)

        assertNull(reminder.nextOccurrenceAfter(now = at(2026, 6, 25, 21, 1)))
    }

    @Test
    fun `a daily reminder advances by one day`() {
        val trigger = at(2026, 6, 25, 21, 0)
        val reminder = Reminder(trigger, ReminderType.ALARM, ReminderRecurrence.DAILY)

        val next = reminder.nextOccurrenceAfter(now = trigger + 1)

        assertEquals(at(2026, 6, 26, 21, 0), next)
    }

    @Test
    fun `a weekly reminder advances by seven days`() {
        val trigger = at(2026, 6, 25, 9, 30)
        val reminder = Reminder(trigger, ReminderType.NOTIFICATION, ReminderRecurrence.WEEKLY)

        val next = reminder.nextOccurrenceAfter(now = trigger + 1)

        assertEquals(at(2026, 7, 1, 9, 30), next)
    }

    @Test
    fun `a daily reminder keeps its time of day when catching up several days`() {
        val trigger = at(2026, 6, 20, 7, 15)
        val reminder = Reminder(trigger, ReminderType.ALARM, ReminderRecurrence.DAILY)

        // Device was off for days: the next occurrence must be in the future, not
        // one day after a long-past trigger.
        val now = at(2026, 6, 24, 12, 0)
        val next = reminder.nextOccurrenceAfter(now)

        assertTrue("next occurrence must be in the future", next!! > now)
        assertEquals(at(2026, 6, 25, 7, 15), next)
    }

    @Test
    fun `firing a daily reminder re-arms it for the next day`() = runTest {
        val trigger = System.currentTimeMillis() - 1_000L
        val reminder = Reminder(trigger, ReminderType.ALARM, ReminderRecurrence.DAILY)
        val repository = FakeNoteRepository().apply { setNotes(listOf(note("a", reminder))) }
        val scheduler = FakeReminderScheduler()

        HandleFiredReminderUseCase(repository, scheduler)("a")

        val stored = repository.getNoteById("a")?.reminder
        assertNotNull("a daily reminder should still exist after firing", stored)
        assertTrue("it should be rescheduled into the future", stored!!.triggerAtMillis > trigger)
        assertEquals(ReminderRecurrence.DAILY, stored.recurrence)
        assertEquals(stored, scheduler.scheduled["a"])
    }

    @Test
    fun `firing a one-time reminder clears it`() = runTest {
        val reminder = Reminder(System.currentTimeMillis() - 1_000L, ReminderType.ALARM)
        val repository = FakeNoteRepository().apply { setNotes(listOf(note("a", reminder))) }
        val scheduler = FakeReminderScheduler()

        HandleFiredReminderUseCase(repository, scheduler)("a")

        // A spent reminder shouldn't keep showing on the note as if still pending.
        assertNull(repository.getNoteById("a")?.reminder)
        assertTrue(scheduler.cancelled.contains("a"))
    }

    @Test
    fun `reboot rolls an overdue daily reminder forward instead of dropping it`() = runTest {
        val staleTrigger = System.currentTimeMillis() - 3 * dayMillis
        val reminder = Reminder(staleTrigger, ReminderType.ALARM, ReminderRecurrence.DAILY)
        val repository = FakeNoteRepository().apply { setNotes(listOf(note("a", reminder))) }
        val scheduler = FakeReminderScheduler()

        RescheduleRemindersUseCase(repository, scheduler)()

        val scheduled = scheduler.scheduled["a"]
        assertNotNull("a daily reminder must survive being missed", scheduled)
        assertTrue(scheduled!!.triggerAtMillis > System.currentTimeMillis())
        // Room must agree with what was scheduled, or the next reboot regresses.
        assertEquals(scheduled.triggerAtMillis, repository.getNoteById("a")?.reminder?.triggerAtMillis)
    }

    @Test
    fun `reboot clears an overdue one-time reminder`() = runTest {
        val reminder = Reminder(System.currentTimeMillis() - dayMillis, ReminderType.NOTIFICATION)
        val repository = FakeNoteRepository().apply { setNotes(listOf(note("a", reminder))) }
        val scheduler = FakeReminderScheduler()

        RescheduleRemindersUseCase(repository, scheduler)()

        assertNull(repository.getAllNotes().first().single().reminder)
        assertNull(scheduler.scheduled["a"])
    }
}
