package com.dins.minddrop.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [NoteEntity::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MindDropDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        /**
         * Adds the user-set reminder columns.
         *
         * A real migration rather than destructive fallback: notes already exist
         * on installed devices, and wiping them to add a feature would be a data
         * loss bug. Both columns are nullable, so existing rows simply have no
         * reminder.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN reminderAtMillis INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE notes ADD COLUMN reminderType TEXT DEFAULT NULL")
            }
        }

        /**
         * Adds reminder recurrence.
         *
         * NOT NULL with a 'NONE' default so existing reminders stay one-time
         * rather than silently becoming repeating, and so the column matches the
         * entity's non-null declaration -- a nullable column here would fail
         * Room's schema validation on open.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE notes ADD COLUMN reminderRecurrence TEXT NOT NULL DEFAULT 'NONE'"
                )
            }
        }

        val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
    }
}
