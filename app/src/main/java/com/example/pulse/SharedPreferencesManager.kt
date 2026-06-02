package com.example.pulse

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPreferencesManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("PulseAppPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_HABITS_LIST = "habits_list"
        private const val KEY_NEXT_HABIT_ID = "next_habit_id"
        private const val KEY_MOODS_LIST = "moods_list"
        private const val KEY_NEXT_MOOD_ID = "next_mood_id"
    }

    // Save habits list to SharedPreferences
    fun saveHabits(habits: List<Habit>) {
        val jsonString = gson.toJson(habits)
        sharedPreferences.edit().putString(KEY_HABITS_LIST, jsonString).apply()
    }

    // Load habits list from SharedPreferences
    fun loadHabits(): List<Habit> {
        val jsonString = sharedPreferences.getString(KEY_HABITS_LIST, null)
        return if (jsonString != null) {
            val type = object : TypeToken<List<Habit>>() {}.type
            gson.fromJson(jsonString, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    // Save next habit ID
    fun saveNextHabitId(nextId: Int) {
        sharedPreferences.edit().putInt(KEY_NEXT_HABIT_ID, nextId).apply()
    }

    // Load next habit ID
    fun loadNextHabitId(): Int {
        return sharedPreferences.getInt(KEY_NEXT_HABIT_ID, 1)
    }

    // Save moods list to SharedPreferences
    fun saveMoods(moods: List<MoodEntry>) {
        val jsonString = gson.toJson(moods)
        sharedPreferences.edit().putString(KEY_MOODS_LIST, jsonString).apply()
    }

    // Load moods list from SharedPreferences
    fun loadMoods(): List<MoodEntry> {
        val jsonString = sharedPreferences.getString(KEY_MOODS_LIST, null)
        return if (jsonString != null) {
            val type = object : TypeToken<List<MoodEntry>>() {}.type
            gson.fromJson(jsonString, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    // Save next mood ID
    fun saveNextMoodId(nextId: Int) {
        sharedPreferences.edit().putInt(KEY_NEXT_MOOD_ID, nextId).apply()
    }

    // Load next mood ID
    fun loadNextMoodId(): Int {
        return sharedPreferences.getInt(KEY_NEXT_MOOD_ID, 1)
    }

    // Clear all data (for testing/reset)
    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
    }
}