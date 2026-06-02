package com.example.pulse

import java.util.Date

data class MoodEntry(
    val id: Int,
    val moodEmoji: String,
    val moodText: String,
    val notes: String = "",
    val date: Date = Date()
) {
    fun getFormattedDate(): String {
        return android.text.format.DateFormat.format("MMM dd, yyyy HH:mm", date).toString()
    }
}