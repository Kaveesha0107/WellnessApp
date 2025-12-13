package com.example.wellnessapp.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.wellnessapp.MainActivity
import com.example.wellnessapp.R
import com.example.wellnessapp.utils.SharedPrefsManager

class HabitWidgetProvider : AppWidgetProvider() {

    // Constants for the broadcast action and the class name
    companion object {
        private const val ACTION_UPDATE_WIDGET = "com.example.wellnessapp.UPDATE_WIDGET"
        private const val WIDGET_CLASS_NAME = "com.example.wellnessapp.widgets.HabitWidgetProvider"


        fun triggerWidgetUpdate(context: Context) {
            val intent = Intent(context, HabitWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            context.sendBroadcast(intent)
        }

        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // 1. Get the latest data
            val prefsManager = SharedPrefsManager(context)
            val habits = prefsManager.getHabits()
            val completed = habits.count { it.isCompleted }
            val total = habits.size

            // Calculate percentage using floating point for accuracy
            val percentage = if (total > 0) (completed.toFloat() / total * 100).toInt() else 0

            // 2. Setup Pending Intent to open MainActivity on click
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                // FLAG_UPDATE_CURRENT is important for updating existing intent

                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 3. Update RemoteViews
            val views = RemoteViews(context.packageName, R.layout.widget_habit)
            views.setTextViewText(R.id.widget_percentage, "$percentage%")
            views.setTextViewText(R.id.widget_subtitle, "$completed/$total habits completed")
            // Set max to 100, current progress to percentage
            views.setProgressBar(R.id.widget_progress, 100, percentage, false)
            views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent) // Attach click listener

            // 4. Instruct the AppWidgetManager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }


    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Standard update cycle (when widget is added or on scheduled intervals)
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // This is the critical part that listens for the custom broadcast
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            // Get all widget IDs currently placed on the home screen
            val componentName = ComponentName(context, HabitWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}