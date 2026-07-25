package com.dins.minddrop.domain.di

import javax.inject.Qualifier

/**
 * Dispatcher qualifiers live in :domain so use cases can state which kind of
 * dispatcher they need, while the concrete bindings stay in :data. These are
 * plain javax.inject annotations, so this keeps :domain free of any DI
 * framework or Android dependency.
 */

/** Disk and database work. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/** CPU-bound work, such as the surface-scoring pass. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher
