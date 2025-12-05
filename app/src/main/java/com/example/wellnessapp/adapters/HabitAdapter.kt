package com.example.wellnessapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.R
import com.example.wellnessapp.models.Habit

class HabitAdapter(
    private val habits: List<Habit>,
    private val onItemClick: (Habit) -> Unit,
    private val onCompleteClick: (Habit) -> Unit,
    private val onDeleteClick: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val habitIcon: TextView = itemView.findViewById(R.id.tv_habit_icon)
        val habitName: TextView = itemView.findViewById(R.id.tv_habit_name)
        val habitDescription: TextView = itemView.findViewById(R.id.tv_habit_description)
        val habitProgress: ProgressBar = itemView.findViewById(R.id.progress_habit)
        val progressText: TextView = itemView.findViewById(R.id.tv_progress_text)
        val completeCheckBox: CheckBox = itemView.findViewById(R.id.cb_complete)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]

        holder.habitIcon.text = habit.icon
        holder.habitName.text = habit.name
        holder.habitDescription.text = habit.description

        // Update progress bar and text
        holder.habitProgress.progress = (habit.currentCount * 100) / habit.targetCount
        holder.progressText.text = "${habit.currentCount}/${habit.targetCount}"

        // Set isCompleted status
        holder.completeCheckBox.isChecked = habit.isCompleted

        // Click listeners
        holder.itemView.setOnClickListener {
            onItemClick(habit)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(habit)
        }

        // Use a click listener for the checkbox to avoid the infinite loop
        holder.completeCheckBox.setOnClickListener {
            onCompleteClick(habit)
        }
    }

    override fun getItemCount() = habits.size
}