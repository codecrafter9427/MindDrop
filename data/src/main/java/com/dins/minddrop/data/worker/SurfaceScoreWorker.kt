package com.dins.minddrop.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dins.minddrop.domain.usecase.UpdateSurfaceScoresUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SurfaceScoreWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val updateSurfaceScoresUseCase: UpdateSurfaceScoresUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            updateSurfaceScoresUseCase()
            Log.d(TAG, "Surface scores updated for all notes")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update surface scores", e)
            Result.retry()
        }
    }

    private companion object {
        const val TAG = "SurfaceScoreWorker"
    }
}
