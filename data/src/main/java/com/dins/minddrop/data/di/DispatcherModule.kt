package com.dins.minddrop.data.di

import com.dins.minddrop.domain.di.DefaultDispatcher
import com.dins.minddrop.domain.di.IoDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

// Injected rather than referenced directly as Dispatchers.IO/Default so tests
// can substitute a TestDispatcher and drive coroutines deterministically.
// The qualifiers themselves live in :domain -- see DispatcherQualifiers.
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
