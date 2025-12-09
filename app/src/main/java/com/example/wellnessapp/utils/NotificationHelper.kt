package com.example.wellnessapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.*
import com.example.wellnessapp.workers.HydrationReminderWorker // Import the worker from its package
import java.util.concurrent.TimeUnit

class NotificationHelper(private val context: Context) {

    companion object {
        private const val TAG = "NotificationHelper"
        private const val HYDRATION_WORK_TAG = "hydration_reminder_unique_work"
        private const val WORKER_CHANNEL_ID = HydrationReminderWorker.CHANNEL_ID
    }

    init {
        // Create the notification channel when NotificationHelper is instantiated.

        createNotificationChannel()
    }

    /**
     * Creates the notification channel for hydration reminders.
     * This method is private as it's an internal setup task for this helper.
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Hydration Reminders"
            val descriptionText = "Channel for hydration reminder notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(WORKER_CHANNEL_ID, name, importance).apply {
                description = descriptionText

            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel '$WORKER_CHANNEL_ID' created or already exists via NotificationHelper.")
        }
    }

    /**
     * Schedules a periodic hydration reminder using WorkManager.
     * @param intervalMinutes The interval in minutes between reminders.
     */
    fun scheduleHydrationReminder(intervalMinutes: Long) {
        if (intervalMinutes <= 0) {
            Log.w(TAG, "Invalid interval: $intervalMinutes minutes. Cannot schedule reminder.")
            return
        }
        Log.d(TAG, "Attempting to schedule hydration reminder for every $intervalMinutes minutes.")

        val hydrationWorkRequest = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
            repeatInterval = intervalMinutes,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )

            .addTag(HYDRATION_WORK_TAG) // Tag for managing this work

            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            HYDRATION_WORK_TAG, // Unique name for this periodic work
            ExistingPeriodicWorkPolicy.REPLACE, // Replace existing work if interval changes
            hydrationWorkRequest
        )
        Log.i(TAG, "Hydration reminder work enqueued with tag: '$HYDRATION_WORK_TAG' for $intervalMinutes minutes.")
    }

    /**
     * Cancels any scheduled hydration reminders.
     */
    fun cancelHydrationReminder() {
        Log.d(TAG, "Attempting to cancel hydration reminders with tag: '$HYDRATION_WORK_TAG'")
        WorkManager.getInstance(context).cancelUniqueWork(HYDRATION_WORK_TAG)
        Log.i(TAG, "Hydration reminder work canceled with tag: '$HYDRATION_WORK_TAG'.")
    }
}
