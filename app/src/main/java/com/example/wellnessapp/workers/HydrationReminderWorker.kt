package com.example.wellnessapp.workers // Or com.example.wellnessapp.utils if combined

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wellnessapp.R // Ensure R class is resolved

class HydrationReminderWorker(
    private val appContext: Context, // Renamed for clarity within this class
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "HydrationReminderWorker"
        const val NOTIFICATION_ID = 101 // Unique ID for this notification type
        const val CHANNEL_ID = "hydration_channel" // Consistent Channel ID
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork: Triggering hydration reminder notification.")
        showNotification(appContext) // Use the appContext passed to the constructor
        return Result.success()
    }

    private fun showNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Hydration Reminders" // Visible name in app settings
            val descriptionText = "Channel for hydration reminder notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                // You can set other channel properties here (e.g., sound, vibration pattern)
            }
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel '${CHANNEL_ID}' created or ensured.")
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications) // Ensure this drawable exists
            .setContentTitle("Stay Hydrated!")
            .setContentText("Time to drink some water and stay refreshed.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // Notification disappears when tapped

        // Show the notification
        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build())
            Log.d(TAG, "Notification successfully posted with ID: $NOTIFICATION_ID")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: Failed to post notification. " +
                    "Ensure POST_NOTIFICATIONS permission is granted on API 33+.", e)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: Failed to post notification.", e)
        }
    }
}
