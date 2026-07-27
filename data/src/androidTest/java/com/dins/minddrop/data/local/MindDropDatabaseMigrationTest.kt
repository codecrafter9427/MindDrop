package com.dins.minddrop.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies the 1 -> 2 migration adds the reminder columns without losing notes.
 *
 * This is the risky part of the reminder feature: devices already have notes, so
 * a botched migration is data loss.
 *
 * Deliberately not using Room's MigrationTestHelper. That requires an exported
 * schema JSON for version 1, but the database was created with
 * exportSchema = false, so v1's schema was never recorded and can't be
 * regenerated now the entity has moved on. Driving the migration against a
 * hand-built v1 table tests the actual SQL rather than Room's bookkeeping.
 */
@RunWith(AndroidJUnit4::class)
class MindDropDatabaseMigrationTest {

    private lateinit var helper: SupportSQLiteOpenHelper

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(TEST_DB)

        val configuration = SupportSQLiteOpenHelper.Configuration
            .builder(context)
            .name(TEST_DB)
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(V1_CREATE_NOTES)
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, old: Int, new: Int) = Unit
            })
            .build()
        helper = FrameworkSQLiteOpenHelperFactory().create(configuration)
    }

    @After
    fun tearDown() {
        helper.close()
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(TEST_DB)
    }

    @Test
    fun migrate1To2_preservesExistingNoteAndAddsNullReminderColumns() {
        val db = helper.writableDatabase
        db.insert(
            "notes",
            SQLiteDatabase.CONFLICT_REPLACE,
            ContentValues().apply {
                put("id", "existing-note")
                put("content", "Written before reminders existed")
                put("type", "TASK")
                put("priority", "HIGH")
                put("createdAt", 1_000L)
                put("lastViewedAt", 2_000L)
                put("viewCount", 3)
                put("surfaceScore", 55.5f)
                put("isSurfaced", 0)
                put("tags", "work,q3")
            }
        )

        MindDropDatabase.MIGRATION_1_2.migrate(db)

        db.query("SELECT * FROM notes WHERE id = 'existing-note'").use { cursor ->
            assertTrue("the pre-existing note should survive the migration", cursor.moveToFirst())

            assertEquals(
                "Written before reminders existed",
                cursor.getString(cursor.getColumnIndexOrThrow("content"))
            )
            assertEquals("TASK", cursor.getString(cursor.getColumnIndexOrThrow("type")))
            assertEquals("HIGH", cursor.getString(cursor.getColumnIndexOrThrow("priority")))
            assertEquals(3, cursor.getInt(cursor.getColumnIndexOrThrow("viewCount")))
            assertEquals("work,q3", cursor.getString(cursor.getColumnIndexOrThrow("tags")))

            // The new columns must exist and read as null: an existing note has no
            // reminder, and a non-null default would invent one.
            assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("reminderAtMillis")))
            assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("reminderType")))
        }
    }

    @Test
    fun migrate1To2_allowsWritingAReminderAfterwards() {
        val db = helper.writableDatabase
        db.insert(
            "notes",
            SQLiteDatabase.CONFLICT_REPLACE,
            ContentValues().apply {
                put("id", "note-1")
                put("content", "Call the plumber")
                put("type", "TASK")
                put("priority", "NORMAL")
                put("createdAt", 1_000L)
                put("lastViewedAt", 1_000L)
                put("viewCount", 0)
                put("surfaceScore", 0f)
                put("isSurfaced", 0)
                put("tags", "")
            }
        )

        MindDropDatabase.MIGRATION_1_2.migrate(db)

        db.execSQL(
            "UPDATE notes SET reminderAtMillis = 1893456000000, reminderType = 'ALARM' " +
                "WHERE id = 'note-1'"
        )

        db.query("SELECT reminderAtMillis, reminderType FROM notes WHERE id = 'note-1'")
            .use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(1893456000000L, cursor.getLong(0))
                assertEquals("ALARM", cursor.getString(1))
            }
    }

    @Test
    fun migrate2To3_addsRecurrenceDefaultingToNone() {
        val db = helper.writableDatabase
        db.insert(
            "notes",
            SQLiteDatabase.CONFLICT_REPLACE,
            ContentValues().apply {
                put("id", "note-1")
                put("content", "Existing one-time reminder")
                put("type", "REMINDER")
                put("priority", "HIGH")
                put("createdAt", 1_000L)
                put("lastViewedAt", 1_000L)
                put("viewCount", 0)
                put("surfaceScore", 0f)
                put("isSurfaced", 0)
                put("tags", "")
            }
        )

        MindDropDatabase.MIGRATION_1_2.migrate(db)
        db.execSQL(
            "UPDATE notes SET reminderAtMillis = 1893456000000, reminderType = 'ALARM' " +
                "WHERE id = 'note-1'"
        )
        MindDropDatabase.MIGRATION_2_3.migrate(db)

        db.query("SELECT reminderRecurrence FROM notes WHERE id = 'note-1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            // Existing reminders must stay one-time rather than silently becoming
            // repeating, and the column is NOT NULL to match the entity.
            assertEquals("NONE", cursor.getString(0))
        }
    }

    private companion object {
        const val TEST_DB = "migration-test.db"

        // The schema Room generated at version 1, before reminders were added.
        const val V1_CREATE_NOTES = """
            CREATE TABLE IF NOT EXISTS `notes` (
                `id` TEXT NOT NULL,
                `content` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `priority` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `lastViewedAt` INTEGER NOT NULL,
                `viewCount` INTEGER NOT NULL,
                `surfaceScore` REAL NOT NULL,
                `isSurfaced` INTEGER NOT NULL,
                `tags` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
        """
    }
}
