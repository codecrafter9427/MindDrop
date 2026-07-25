package com.dins.minddrop.data.worker

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MindDropWorkScheduler @Inject constructor(
    private val workManager: WorkManager
) {

    fun scheduleAll() {
        scheduleSurfaceScoreWorker()
        scheduleSurfacingWorker()
    }

    private fun scheduleSurfaceScoreWorker() {
        val request = PeriodicWorkRequestBuilder<SurfaceScoreWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            SURFACE_SCORE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun scheduleSurfacingWorker() {
        val request = PeriodicWorkRequestBuilder<SurfacingWorker>(12, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            SURFACING_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    // No network requirement: scoring/surfacing only reads Room, matching the
    // app's offline-first design. Battery constraint still applies since these
    // are best-effort background jobs, not user-triggered actions.
    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
        .setRequiresBatteryNotLow(true)
        .build()

    private companion object {
        const val SURFACE_SCORE_WORK_NAME = "surface_score_work"
        const val SURFACING_WORK_NAME = "surfacing_work"
    }
}
