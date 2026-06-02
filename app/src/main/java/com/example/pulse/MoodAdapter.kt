package com.example.pulse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MoodAdapter(
    private var moodEntries: List<MoodEntry>,
    private val onDeleteClick: (MoodEntry) -> Unit,
    private val onShareClick: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emojiTextView: TextView = itemView.findViewById(R.id.tv_mood_emoji)
        val moodTextTextView: TextView = itemView.findViewById(R.id.tv_mood_text)
        val dateTextView: TextView = itemView.findViewById(R.id.tv_mood_date)
        val notesTextView: TextView = itemView.findViewById(R.id.tv_mood_notes)
        val deleteButton: Button = itemView.findViewById(R.id.btn_delete_mood)
        val shareButton: Button = itemView.findViewById(R.id.btn_share_mood)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_entry, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val moodEntry = moodEntries[position]

        holder.emojiTextView.text = moodEntry.moodEmoji
        holder.moodTextTextView.text = moodEntry.moodText
        holder.dateTextView.text = moodEntry.getFormattedDate()
        holder.notesTextView.text = moodEntry.notes
        holder.notesTextView.visibility = if (moodEntry.notes.isNotEmpty()) View.VISIBLE else View.GONE

        holder.deleteButton.setOnClickListener {
            onDeleteClick(moodEntry)
        }

        holder.shareButton.setOnClickListener {
            onShareClick(moodEntry)
        }
    }

    override fun getItemCount(): Int = moodEntries.size

    fun updateMoodEntries(newEntries: List<MoodEntry>) {
        moodEntries = newEntries
        notifyDataSetChanged()
    }
}