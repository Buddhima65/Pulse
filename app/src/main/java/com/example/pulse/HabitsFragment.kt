package com.example.pulse

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.EditText

class HabitsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddHabit: FloatingActionButton
    private lateinit var adapter: HabitAdapter
    private var habitsList = mutableListOf<Habit>()
    private var nextId = 1
    private lateinit var prefsManager: SharedPreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_habits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize SharedPreferences Manager
        prefsManager = SharedPreferencesManager(requireContext())

        setupViews(view)
        setupRecyclerView()
        loadHabitsFromStorage() // Load from SharedPreferences instead of sample data
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.habits_recycler_view)
        fabAddHabit = view.findViewById(R.id.fab_add_habit)

        fabAddHabit.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = HabitAdapter(
            habits = habitsList,
            onHabitClick = { habit ->
                showEditHabitDialog(habit)
            },
            onDeleteClick = { habit ->
                deleteHabit(habit)
            },
            onIncrementClick = { habit ->
                incrementHabitProgress(habit)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun loadHabitsFromStorage() {
        // Load from SharedPreferences
        habitsList.clear()
        habitsList.addAll(prefsManager.loadHabits())
        nextId = prefsManager.loadNextHabitId()

        // If no habits exist, load sample data
        if (habitsList.isEmpty()) {
            loadSampleHabits()
        }

        adapter.updateHabits(habitsList)
    }

    private fun loadSampleHabits() {
        habitsList.clear()
        habitsList.addAll(
            listOf(
                Habit(1, "Drink Water", "Stay hydrated throughout the day", targetCount = 8),
                Habit(2, "Morning Meditation", "10 minutes of mindfulness", false),
                Habit(3, "Daily Steps", "Walk 10,000 steps", currentCount = 3500, targetCount = 10000)
            )
        )
        nextId = 4
        saveHabitsToStorage()
    }

    private fun showAddHabitDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_habit, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.et_habit_title)
        val etDescription = dialogView.findViewById<EditText>(R.id.et_habit_description)
        val etTargetCount = dialogView.findViewById<EditText>(R.id.et_target_count)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add New Habit")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, which ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val targetCount = etTargetCount.text.toString().toIntOrNull() ?: 1

                if (title.isNotEmpty()) {
                    val newHabit = Habit(
                        id = nextId++,
                        title = title,
                        description = description,
                        targetCount = targetCount
                    )
                    addHabit(newHabit)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun showEditHabitDialog(habit: Habit) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit Habit")
            .setMessage("Edit '${habit.title}'?")
            .setPositiveButton("Mark Complete") { dialog, which ->
                toggleHabitCompletion(habit)
            }
            .setNeutralButton("Cancel", null)
            .setNegativeButton("Reset Progress") { dialog, which ->
                resetHabitProgress(habit)
            }
            .create()

        dialog.show()
    }

    private fun addHabit(habit: Habit) {
        habitsList.add(habit)
        adapter.updateHabits(habitsList)
        saveHabitsToStorage()
    }

    private fun deleteHabit(habit: Habit) {
        habitsList.removeAll { it.id == habit.id }
        adapter.updateHabits(habitsList)
        saveHabitsToStorage()
    }

    private fun incrementHabitProgress(habit: Habit) {
        val updatedHabit = habit.copy(
            currentCount = (habit.currentCount + 1).coerceAtMost(habit.targetCount)
        )
        updateHabitInList(updatedHabit)
    }

    private fun toggleHabitCompletion(habit: Habit) {
        val updatedHabit = if (habit.targetCount > 1) {
            // For countable habits, set to max
            habit.copy(currentCount = habit.targetCount)
        } else {
            // For boolean habits, toggle completion
            habit.copy(isCompleted = !habit.isCompleted)
        }
        updateHabitInList(updatedHabit)
    }

    private fun resetHabitProgress(habit: Habit) {
        val updatedHabit = habit.copy(
            isCompleted = false,
            currentCount = 0
        )
        updateHabitInList(updatedHabit)
    }

    private fun updateHabitInList(updatedHabit: Habit) {
        val index = habitsList.indexOfFirst { it.id == updatedHabit.id }
        if (index != -1) {
            habitsList[index] = updatedHabit
            adapter.updateHabits(habitsList)
            saveHabitsToStorage()
        }
    }

    private fun saveHabitsToStorage() {
        prefsManager.saveHabits(habitsList)
        prefsManager.saveNextHabitId(nextId)
    }
}