package com.example.pulse

data class Habit(
    val id: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val targetCount: Int = 1, // For habits like "drink 8 glasses of water"
    val currentCount: Int = 0
) {
    fun getCompletionPercentage(): Int {
        return if (targetCount > 0) {
            (currentCount * 100 / targetCount).coerceAtMost(100)
        } else {
            if (isCompleted) 100 else 0
        }
    }
}