package com.dins.minddrop.data.di

import android.content.Context
import androidx.room.Room
import com.dins.minddrop.data.local.MindDropDatabase
import com.dins.minddrop.data.local.NoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMindDropDatabase(@ApplicationContext context: Context): MindDropDatabase =
        Room.databaseBuilder(
            context,
            MindDropDatabase::class.java,
            "minddrop.db"
        )
            .addMigrations(*MindDropDatabase.ALL_MIGRATIONS)
            .build()

    @Provides
    @Singleton
    fun provideNoteDao(database: MindDropDatabase): NoteDao = database.noteDao()
}
