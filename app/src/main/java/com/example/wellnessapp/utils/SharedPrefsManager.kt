package com.example.wellnessapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log // Added for potential future logging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.wellnessapp.models.Habit
import com.example.wellnessapp.models.MoodEntry

class SharedPrefsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("WellnessAppPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val TAG = "SharedPrefsManager"
        const val KEY_HABITS = "habits"
        const val KEY_MOODS = "mood_entries"
        const val KEY_HYDRATION_INTERVAL = "hydration_interval"
        const val KEY_HYDRATION_ENABLED = "hydration_enabled"
        const val KEY_DAILY_WATER = "daily_water_count"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        const val KEY_SECURITY_PIN = "security_pin"
    }

    // Habits Management
    fun saveHabits(habits: List<Habit>) {
        Log.d(TAG, "Saving ${habits.size} habits")
        val json = gson.toJson(habits)
        prefs.edit().putString(KEY_HABITS, json).apply()
    }

    fun getHabits(): List<Habit> {
        val json = prefs.getString(KEY_HABITS, null)
        Log.d(TAG, "Getting habits, JSON: $json")
        if (json == null) return emptyList()
        val type = object : TypeToken<List<Habit>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    // Mood Entries Management
    fun saveMoodEntries(entries: List<MoodEntry>) {
        Log.d(TAG, "Saving ${entries.size} mood entries")
        val json = gson.toJson(entries)
        prefs.edit().putString(KEY_MOODS, json).apply()
    }

    fun getMoodEntries(): List<MoodEntry> {
        val json = prefs.getString(KEY_MOODS, null)
        Log.d(TAG, "Getting mood entries, JSON: $json")
        if (json == null) return emptyList()
        val type = object : TypeToken<List<MoodEntry>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    // Hydration Settings
    fun setHydrationInterval(minutes: Int) {
        Log.d(TAG, "Setting hydration interval to: $minutes minutes")
        prefs.edit().putInt(KEY_HYDRATION_INTERVAL, minutes).apply()
    }

    fun getHydrationInterval(): Int {
        val interval = prefs.getInt(KEY_HYDRATION_INTERVAL, 60) // Default 60 minutes
        Log.d(TAG, "Getting hydration interval: $interval minutes")
        return interval
    }

    fun setHydrationEnabled(enabled: Boolean) {
        Log.d(TAG, "Setting hydration enabled to: $enabled")
        prefs.edit().putBoolean(KEY_HYDRATION_ENABLED, enabled).apply()
    }

    fun isHydrationEnabled(): Boolean {
        val isEnabled = prefs.getBoolean(KEY_HYDRATION_ENABLED, true) // Default true
        Log.d(TAG, "Is hydration enabled: $isEnabled")
        return isEnabled
    }

    // Daily Water Count
    fun getDailyWaterCount(): Int {
        val count = prefs.getInt(KEY_DAILY_WATER, 0) // Default 0
        Log.d(TAG, "Getting daily water count: $count")
        return count
    }

    fun incrementWaterCount() {
        val current = getDailyWaterCount()
        val newCount = current + 1
        Log.d(TAG, "Incrementing water count from $current to $newCount")
        prefs.edit().putInt(KEY_DAILY_WATER, newCount).apply()
    }

    fun resetDailyWaterCount() {
        Log.d(TAG, "Resetting daily water count to 0")
        prefs.edit().putInt(KEY_DAILY_WATER, 0).apply()
    }

    // Onboarding Management
    fun setOnboardingCompleted(completed: Boolean) {
        Log.d(TAG, "Setting onboarding completed to: $completed")
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    fun isOnboardingCompleted(): Boolean {
        val isCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        Log.d(TAG, "Is onboarding completed: $isCompleted")
        return isCompleted
    }

    // Security Management
    fun setSecurityPin(pin: String) {
        Log.d(TAG, "Setting security PIN")
        prefs.edit().putString(KEY_SECURITY_PIN, pin).apply()
    }

    fun getSecurityPin(): String {
        val pin = prefs.getString(KEY_SECURITY_PIN, "") ?: ""
        Log.d(TAG, "Getting security PIN: ${if (pin.isEmpty()) "Not set" else "Set"}")
        return pin
    }
}
