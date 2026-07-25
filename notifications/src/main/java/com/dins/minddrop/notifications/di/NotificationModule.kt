package com.dins.minddrop.notifications.di

import com.dins.minddrop.domain.notification.NoteSurfacer
import com.dins.minddrop.notifications.NotificationNoteSurfacer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun bindNoteSurfacer(impl: NotificationNoteSurfacer): NoteSurfacer
}
