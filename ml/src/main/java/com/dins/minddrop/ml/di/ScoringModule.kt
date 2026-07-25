package com.dins.minddrop.ml.di

import com.dins.minddrop.domain.scoring.SurfaceScorer
import com.dins.minddrop.ml.scoring.SurfaceScoreCalculator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ScoringModule {

    @Binds
    @Singleton
    abstract fun bindSurfaceScorer(impl: SurfaceScoreCalculator): SurfaceScorer
}
