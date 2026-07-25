package com.dins.minddrop.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dins.minddrop.domain.usecase.SurfaceTopNotesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SurfacingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val surfaceTopNotesUseCase: SurfaceTopNotesUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            surfaceTopNotesUseCase()
            Log.d(TAG, "Surfacing pass complete")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to surface top notes", e)
            Result.retry()
        }
    }

    private companion object {
        const val TAG = "SurfacingWorker"
    }
}
