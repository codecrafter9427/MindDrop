package com.dins.minddrop.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dins.minddrop.domain.model.Note
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationBuilder: NoteNotificationBuilder
) {

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        val reminders = NotificationChannel(
            REMINDERS_CHANNEL_ID,
            "MindDrop Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Urgent and high-priority note reminders" }

        val dailySurface = NotificationChannel(
            DAILY_SURFACE_CHANNEL_ID,
            "Daily Surface",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Notes MindDrop resurfaces based on staleness and usage" }

        manager.createNotificationChannels(listOf(reminders, dailySurface))
    }

    fun showNotification(note: Note) {
        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            Log.d(TAG, "Skipping notification for ${note.id}: POST_NOTIFICATIONS not granted")
            return
        }

        NotificationManagerCompat.from(context)
            .notify(note.id.hashCode(), notificationBuilder.build(note))
        Log.d(TAG, "Posted notification for note ${note.id} (score=${note.surfaceScore})")
    }

    companion object {
        const val REMINDERS_CHANNEL_ID = "reminders"
        const val DAILY_SURFACE_CHANNEL_ID = "daily_surface"
        private const val TAG = "NotificationHelper"
    }
}
