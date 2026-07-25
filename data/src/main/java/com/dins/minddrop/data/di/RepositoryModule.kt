package com.dins.minddrop.data.di

import com.dins.minddrop.data.repository.NoteRepositoryImpl
import com.dins.minddrop.data.repository.UserPreferencesRepositoryImpl
import com.dins.minddrop.domain.repository.NoteRepository
import com.dins.minddrop.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}
