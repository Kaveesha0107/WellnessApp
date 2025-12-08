package com.example.wellnessapp.models

import java.io.Serializable
import java.util.*

// This data class defines the structure of a single habit.

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var description: String = "",
    var icon: String,
    var targetCount: Int,
    var currentCount: Int = 0,
    var isCompleted: Boolean = false
) : Serializable