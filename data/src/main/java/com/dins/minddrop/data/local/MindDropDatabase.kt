package com.dins.minddrop.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [NoteEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MindDropDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
