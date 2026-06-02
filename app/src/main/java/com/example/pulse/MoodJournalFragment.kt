package com.example.pulse

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class MoodJournalFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddMood: FloatingActionButton
    private lateinit var adapter: MoodAdapter
    private var moodEntriesList = mutableListOf<MoodEntry>()
    private var nextMoodId = 1
    private lateinit var prefsManager: SharedPreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_mood, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize SharedPreferences Manager
        prefsManager = SharedPreferencesManager(requireContext())

        setupViews(view)
        setupRecyclerView()
        loadMoodsFromStorage()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.mood_recycler_view)
        fabAddMood = view.findViewById(R.id.fab_add_mood)

        fabAddMood.setOnClickListener {
            showMoodSelectionDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = MoodAdapter(
            moodEntries = moodEntriesList,
            onDeleteClick = { moodEntry ->
                deleteMoodEntry(moodEntry)
            },
            onShareClick = { moodEntry ->
                shareMoodEntry(moodEntry)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun loadMoodsFromStorage() {
        // Load from SharedPreferences
        moodEntriesList.clear()
        moodEntriesList.addAll(prefsManager.loadMoods())
        nextMoodId = prefsManager.loadNextMoodId()

        // If no moods exist, load sample data
        if (moodEntriesList.isEmpty()) {
            loadSampleMoodEntries()
        }

        // Add sample data for chart testing
        addSampleMoodDataForChart()

        adapter.updateMoodEntries(moodEntriesList)
    }

    private fun loadSampleMoodEntries() {
        moodEntriesList.clear()
        moodEntriesList.addAll(
            listOf(
                MoodEntry(1, "😊", "Happy", "Had a productive day!"),
                MoodEntry(2, "😢", "Sad", "Feeling a bit down today"),
                MoodEntry(3, "😴", "Tired", "Need more sleep")
            )
        )
        nextMoodId = 4
        saveMoodsToStorage()
    }

    private fun addSampleMoodDataForChart() {
        // Only add sample data if we have less than 3 mood entries with different dates
        val recentMoods = moodEntriesList.takeLast(3)
        if (recentMoods.size < 3) {
            val sampleMoods = listOf(
                MoodEntry(
                    id = nextMoodId++,
                    moodEmoji = "😊",
                    moodText = "Happy",
                    notes = "Great day!",
                    date = getDateForDaysAgo(2) // 2 days ago
                ),
                MoodEntry(
                    id = nextMoodId++,
                    moodEmoji = "😴",
                    moodText = "Tired",
                    notes = "Long day at work",
                    date = getDateForDaysAgo(1) // 1 day ago
                ),
                MoodEntry(
                    id = nextMoodId++,
                    moodEmoji = "😃",
                    moodText = "Excited",
                    notes = "Looking forward to the weekend!",
                    date = Date() // Today
                )
            )

            // Only add if we don't already have these moods
            sampleMoods.forEach { sampleMood ->
                if (!moodEntriesList.any { isSameDay(it.date, sampleMood.date) }) {
                    moodEntriesList.add(0, sampleMood)
                }
            }
            saveMoodsToStorage()
        }
    }

    private fun getDateForDaysAgo(daysAgo: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return calendar.time
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    private fun showMoodSelectionDialog() {
        val moods = listOf(
            "😊 Happy" to "😊",
            "😢 Sad" to "😢",
            "😠 Angry" to "😠",
            "😴 Tired" to "😴",
            "😃 Excited" to "😃",
            "😌 Relaxed" to "😌",
            "😰 Anxious" to "😰",
            "😎 Confident" to "😎"
        )

        val moodItems = moods.map { it.first }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("How are you feeling?")
            .setItems(moodItems) { dialog, which ->
                val selectedMood = moods[which]
                showMoodNotesDialog(selectedMood.first, selectedMood.second)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMoodNotesDialog(moodText: String, moodEmoji: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_mood_notes, null)
        val tvSelectedMood = dialogView.findViewById<TextView>(R.id.tv_selected_mood)
        val etNotes = dialogView.findViewById<EditText>(R.id.et_mood_notes)

        tvSelectedMood.text = "$moodEmoji $moodText"

        AlertDialog.Builder(requireContext())
            .setTitle("Add Notes (Optional)")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, which ->
                val notes = etNotes.text.toString().trim()
                val newMoodEntry = MoodEntry(
                    id = nextMoodId++,
                    moodEmoji = moodEmoji,
                    moodText = moodText.split(" ")[1], // Get just "Happy" from "😊 Happy"
                    notes = notes
                )
                addMoodEntry(newMoodEntry)
            }
            .setNegativeButton("Skip", null)
            .show()
    }

    private fun addMoodEntry(moodEntry: MoodEntry) {
        moodEntriesList.add(0, moodEntry) // Add to top
        adapter.updateMoodEntries(moodEntriesList)
        saveMoodsToStorage()
    }

    private fun deleteMoodEntry(moodEntry: MoodEntry) {
        moodEntriesList.removeAll { it.id == moodEntry.id }
        adapter.updateMoodEntries(moodEntriesList)
        saveMoodsToStorage()
    }

    private fun shareMoodEntry(moodEntry: MoodEntry) {
        val shareText = "My current mood: ${moodEntry.moodEmoji} ${moodEntry.moodText}" +
                if (moodEntry.notes.isNotEmpty()) "\n\nNotes: ${moodEntry.notes}" else "" +
                        "\n\nShared via Pulse Wellness App"

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        startActivity(Intent.createChooser(shareIntent, "Share your mood"))
    }

    private fun saveMoodsToStorage() {
        prefsManager.saveMoods(moodEntriesList)
        prefsManager.saveNextMoodId(nextMoodId)
    }
}