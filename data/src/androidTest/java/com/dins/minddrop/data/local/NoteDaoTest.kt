package com.dins.minddrop.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteDaoTest {

    private lateinit var database: MindDropDatabase
    private lateinit var dao: NoteDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, MindDropDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.noteDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertNote_andRetrieveViaFlow_returnsInsertedNote() = runBlocking {
        val note = NoteEntity(
            id = "1",
            content = "Buy milk",
            type = "TASK",
            priority = "NORMAL",
            createdAt = 1000L,
            lastViewedAt = 1000L,
            viewCount = 0,
            surfaceScore = 0f,
            isSurfaced = false,
            tags = listOf("errand")
        )

        dao.insertNote(note)

        val notes = dao.getAllNotes().first()

        assertEquals(1, notes.size)
        assertEquals(note, notes.first())
    }
}
