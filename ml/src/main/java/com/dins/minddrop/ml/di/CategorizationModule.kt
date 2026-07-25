package com.dins.minddrop.ml.di

import com.dins.minddrop.domain.categorization.NoteCategorizer
import com.dins.minddrop.ml.categorization.CompositeNoteCategorizer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CategorizationModule {

    @Binds
    @Singleton
    abstract fun bindNoteCategorizer(impl: CompositeNoteCategorizer): NoteCategorizer
}
