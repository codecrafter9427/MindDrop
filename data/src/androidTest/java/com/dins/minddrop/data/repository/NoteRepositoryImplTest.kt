package com.dins.minddrop.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dins.minddrop.data.local.MindDropDatabase
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.NoteType
import com.dins.minddrop.domain.model.Priority
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Exercises NoteRepositoryImpl against a real (in-memory) Room database, so
// the entity/domain mapping and the DAO queries are both covered -- a fake
// wouldn't catch a broken mapper or a malformed @Query.
@RunWith(AndroidJUnit4::class)
class NoteRepositoryImplTest {

    private lateinit var database: MindDropDatabase
    private lateinit var repository: NoteRepositoryImpl

    private val note = Note(
        id = "note-1",
        content = "Urgent: submit the report",
        type = NoteType.TASK,
        priority = Priority.HIGH,
        createdAt = 1_000L,
        lastViewedAt = 1_000L,
        viewCount = 2,
        surfaceScore = 0f,
        isSurfaced = false,
        tags = listOf("work", "q3")
    )

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, MindDropDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = NoteRepositoryImpl(database.noteDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun addNote_thenGetAllNotes_emitsTheNoteWithFieldsIntact() = runBlocking {
        repository.addNote(note)

        val notes = repository.getAllNotes().first()

        assertEquals(1, notes.size)
        // Asserts on the round-tripped domain object, which also covers the
        // enum-to-String and List<String> tag converters.
        assertEquals(note, notes.first())
    }

    @Test
    fun getNoteById_returnsTheMatchingNote() = runBlocking {
        repository.addNote(note)

        assertEquals(note, repository.getNoteById("note-1"))
    }

    @Test
    fun getNoteById_returnsNullForAnUnknownId() = runBlocking {
        assertNull(repository.getNoteById("missing"))
    }

    @Test
    fun updateNote_persistsTheEditedFields() = runBlocking {
        repository.addNote(note)

        repository.updateNote(note.copy(content = "Edited", priority = Priority.LOW))

        val stored = repository.getNoteById("note-1")
        assertEquals("Edited", stored?.content)
        assertEquals(Priority.LOW, stored?.priority)
    }

    @Test
    fun deleteNote_removesItFromTheFlow() = runBlocking {
        repository.addNote(note)

        repository.deleteNote("note-1")

        assertTrue(repository.getAllNotes().first().isEmpty())
    }

    @Test
    fun updateSurfaceScore_writesOnlyTheScore() = runBlocking {
        repository.addNote(note)

        repository.updateSurfaceScore("note-1", 82.5f)

        val stored = repository.getNoteById("note-1")
        assertEquals(82.5f, stored?.surfaceScore)
        assertEquals(note.content, stored?.content)
    }
}
