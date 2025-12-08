package com.example.wellnessapp.models // Correct package

data class MoodEntry(
    val id: String,
    val moodEmoji: String,
    val note: String,
    val timestamp: Long,
    val moodValue: Int
)
