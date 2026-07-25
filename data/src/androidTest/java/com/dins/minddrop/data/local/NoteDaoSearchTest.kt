package com.dins.minddrop.data.local

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// The search/filter/sort logic added on Day 14 lives entirely in SQL, so it can
// only be verified against a real database -- a fake repository would just be
// re-testing a Kotlin reimplementation of the same rules.
@RunWith(AndroidJUnit4::class)
class NoteDaoSearchTest {

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

    private fun entity(
        id: String,
        content: String,
        type: String = "GENERAL",
        priority: String = "NORMAL",
        createdAt: Long
    ) = NoteEntity(
        id = id,
        content = content,
        type = type,
        priority = priority,
        createdAt = createdAt,
        lastViewedAt = createdAt,
        viewCount = 0,
        surfaceScore = 0f,
        isSurfaced = false,
        tags = emptyList()
    )

    private suspend fun idsFor(
        query: String = "",
        type: String? = null,
        priority: String? = null,
        sortOrder: String = "NEWEST_FIRST"
    ): List<String> {
        val pagingSource = dao.getPagedNotes(query, type, priority, sortOrder)
        val page = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 50,
                placeholdersEnabled = false
            )
        ) as PagingSource.LoadResult.Page
        return page.data.map { it.id }
    }

    private fun seed() = runBlocking {
        dao.insertNote(
            entity("milk", "Buy milk and eggs", type = "TASK", priority = "LOW", createdAt = 1L)
        )
        dao.insertNote(
            entity("report", "Finish the Quarterly Report", type = "TASK", priority = "URGENT", createdAt = 2L)
        )
        dao.insertNote(
            entity("idea", "Idea: milk frother review", type = "IDEA", priority = "NORMAL", createdAt = 3L)
        )
    }

    @Test
    fun blankQuery_returnsEveryNote() = runBlocking {
        seed()

        assertEquals(setOf("milk", "report", "idea"), idsFor().toSet())
    }

    @Test
    fun query_matchesContentSubstring() = runBlocking {
        seed()

        assertEquals(setOf("milk", "idea"), idsFor(query = "milk").toSet())
    }

    @Test
    fun query_isCaseInsensitive() = runBlocking {
        seed()

        assertEquals(listOf("report"), idsFor(query = "quarterly"))
    }

    @Test
    fun typeFilter_returnsOnlyThatType() = runBlocking {
        seed()

        assertEquals(setOf("milk", "report"), idsFor(type = "TASK").toSet())
    }

    @Test
    fun priorityFilter_returnsOnlyThatPriority() = runBlocking {
        seed()

        assertEquals(listOf("report"), idsFor(priority = "URGENT"))
    }

    @Test
    fun queryAndTypeFilter_applyTogether() = runBlocking {
        seed()

        assertEquals(listOf("milk"), idsFor(query = "milk", type = "TASK"))
    }

    @Test
    fun newestFirst_ordersByCreatedAtDescending() = runBlocking {
        seed()

        assertEquals(listOf("idea", "report", "milk"), idsFor(sortOrder = "NEWEST_FIRST"))
    }

    @Test
    fun oldestFirst_ordersByCreatedAtAscending() = runBlocking {
        seed()

        assertEquals(listOf("milk", "report", "idea"), idsFor(sortOrder = "OLDEST_FIRST"))
    }

    @Test
    fun priorityFirst_ordersUrgentAboveNormalAboveLow() = runBlocking {
        seed()

        assertEquals(listOf("report", "idea", "milk"), idsFor(sortOrder = "PRIORITY_FIRST"))
    }

    @Test
    fun filtersAndSortCombine() = runBlocking {
        seed()

        assertEquals(
            listOf("report", "milk"),
            idsFor(type = "TASK", sortOrder = "PRIORITY_FIRST")
        )
    }
}
