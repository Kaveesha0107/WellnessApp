package com.example.wellnessapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnessapp.utils.SharedPrefsManager
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var prefsManager: SharedPrefsManager
    private lateinit var tvWelcome: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvHabitProgress: TextView
    private lateinit var tvMoodCount: TextView
    private lateinit var tvWaterCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        prefsManager = SharedPrefsManager(this)
        setupViews()
        updateDashboard()
    }

    private fun setupViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvDate = findViewById(R.id.tvDate)
        tvHabitProgress = findViewById(R.id.tvHabitProgress)
        tvMoodCount = findViewById(R.id.tvMoodCount)
        tvWaterCount = findViewById(R.id.tvWaterCount)
    }

    private fun updateDashboard() {
        // Welcome message
        tvWelcome.text = "Welcome back!"

        // Current date
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        tvDate.text = dateFormat.format(Date())

        // Habit progress
        val habits = prefsManager.getHabits()
        val completedHabits = habits.count { it.isCompleted }
        val totalHabits = habits.size
        val habitPercentage = if (totalHabits > 0) (completedHabits * 100) / totalHabits else 0
        tvHabitProgress.text = "$completedHabits/$totalHabits habits completed ($habitPercentage%)"

        // Mood entries count
        val moodEntries = prefsManager.getMoodEntries()
        val todayMoods = moodEntries.count {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val entryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp))
            entryDate == today
        }
        tvMoodCount.text = "$todayMoods mood entries today"

        // Water count
        val waterCount = prefsManager.getDailyWaterCount()
        tvWaterCount.text = "$waterCount glasses today"
    }
}