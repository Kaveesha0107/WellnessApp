package com.example.wellnessapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.R
import com.example.wellnessapp.models.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

class MoodAdapter(
    private var moods: List<MoodEntry>,
    private val onItemClick: (MoodEntry) -> Unit,
    private val onDeleteClick: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    fun updateData(newMoods: List<MoodEntry>) {
        moods = newMoods
        notifyDataSetChanged()
    }

    inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emojiText: TextView = itemView.findViewById(R.id.tv_mood_emoji)
        val timestampText: TextView = itemView.findViewById(R.id.tv_mood_timestamp)
        val noteText: TextView = itemView.findViewById(R.id.tv_mood_note_snippet)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_entry, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val mood = moods[position]

        holder.emojiText.text = mood.moodEmoji

        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        holder.timestampText.text = dateFormat.format(Date(mood.timestamp))

        holder.noteText.text = mood.note.ifEmpty { "No note" }

        holder.itemView.setOnClickListener {
            onItemClick(mood)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(mood)
        }
    }

    override fun getItemCount(): Int = moods.size
}
