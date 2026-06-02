package com.example.pulse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HabitAdapter(
    private var habits: List<Habit>,
    private val onHabitClick: (Habit) -> Unit,
    private val onDeleteClick: (Habit) -> Unit,
    private val onIncrementClick: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.tv_habit_title)
        val descriptionTextView: TextView = itemView.findViewById(R.id.tv_habit_description)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
        val progressText: TextView = itemView.findViewById(R.id.tv_progress)
        val deleteButton: Button = itemView.findViewById(R.id.btn_delete)
        val incrementButton: Button = itemView.findViewById(R.id.btn_increment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]

        holder.titleTextView.text = habit.title
        holder.descriptionTextView.text = habit.description

        val progress = habit.getCompletionPercentage()
        holder.progressBar.progress = progress
        holder.progressText.text = "$progress%"

        // Show increment button only for countable habits
        holder.incrementButton.visibility = if (habit.targetCount > 1) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            onHabitClick(habit)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(habit)
        }

        holder.incrementButton.setOnClickListener {
            onIncrementClick(habit)
        }
    }

    override fun getItemCount(): Int = habits.size

    fun updateHabits(newHabits: List<Habit>) {
        habits = newHabits
        notifyDataSetChanged()
    }
}