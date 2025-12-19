package com.example.wellnessapp.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.R
import com.example.wellnessapp.adapters.HabitAdapter
import com.example.wellnessapp.models.Habit
import com.example.wellnessapp.utils.SharedPrefsManager
import com.example.wellnessapp.utils.ValidationHelper
import com.example.wellnessapp.widgets.HabitWidgetProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.util.*
import kotlin.math.log
import com.google.android.material.card.MaterialCardView

class HabitsFragment : Fragment(), SensorEventListener {

    private lateinit var prefsManager: SharedPrefsManager
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var validationHelper: ValidationHelper
    private var habits = mutableListOf<Habit>()
    private lateinit var progressText: TextView
    private lateinit var recyclerViewHabits: RecyclerView
    private lateinit var fabAddHabit: FloatingActionButton
    private lateinit var shareProgressButton: Button

    // Step counter
    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var stepCount = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsManager = SharedPrefsManager(requireContext())
        validationHelper = ValidationHelper(requireContext())

        recyclerViewHabits = view.findViewById(R.id.recycler_view_habits)
        fabAddHabit = view.findViewById(R.id.fab_add_habit)
        progressText = view.findViewById(R.id.tv_progress)
        shareProgressButton = view.findViewById(R.id.btn_share_progress)

        setupRecyclerView()
        setupListeners()
        setupStepCounter()

        loadHabits()
        updateProgress()
    }

    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(
            habits,
            onItemClick = { habit ->
                showAddHabitDialog(habit)
            },
            onCompleteClick = { habit ->
                toggleHabitCompletion(habit)
            },
            onDeleteClick = { habit ->
                showDeleteDialog(habit)
            }
        )
        recyclerViewHabits.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewHabits.adapter = habitAdapter
    }

    private fun setupListeners() {
        fabAddHabit.setOnClickListener {
            showAddHabitDialog(null)
        }

        shareProgressButton.setOnClickListener {
            shareProgress()
        }
    }

    private fun setupStepCounter() {
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null) {
            Toast.makeText(context, "Step counter sensor not available!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddHabitDialog(habitToEdit: Habit?) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_habit, null)
        val etHabitName = dialogView.findViewById<TextInputEditText>(R.id.et_habit_name)
        val etHabitDescription = dialogView.findViewById<TextInputEditText>(R.id.et_habit_description)
        val etTargetCount = dialogView.findViewById<TextInputEditText>(R.id.et_target_count)
        val emojiGrid = dialogView.findViewById<GridLayout>(R.id.habit_emoji_grid)

        val emojiList = listOf("✨", "💡", "📚", "🏃", "🍎", "🧘", "💧", "💰", "😴", "💪")

        // Corrected the null-safety issue
        var selectedEmoji: String = habitToEdit?.icon?.takeIf { it.isNotEmpty() } ?: emojiList[0]

        val cardViews = mutableListOf<MaterialCardView>()

        emojiList.forEach { emoji ->
            val cardView = MaterialCardView(requireContext()).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8, 8, 8, 8)
                }
                radius = 16f
                strokeWidth = 2
                isCheckable = true
                isChecked = (emoji == selectedEmoji)

                val textView = TextView(context).apply {
                    text = emoji
                    textSize = 36f
                    gravity = Gravity.CENTER
                    setPadding(16, 16, 16, 16)
                }
                addView(textView)

                setOnClickListener {
                    selectedEmoji = emoji
                    cardViews.forEach { it.isChecked = false }
                    this.isChecked = true
                }
            }
            cardViews.add(cardView)
            emojiGrid.addView(cardView)
        }

        cardViews.forEach { card ->
            val textView = card.getChildAt(0) as TextView
            card.isChecked = (textView.text == selectedEmoji)
            // Corrected unresolved color reference
            card.strokeColor = if (card.isChecked) requireContext().getColor(R.color.primary) else requireContext().getColor(R.color.gray_light)
        }

        cardViews.forEach { card ->
            card.setOnCheckedChangeListener { _, isChecked ->
                // Corrected unresolved color reference
                card.strokeColor = if (isChecked) requireContext().getColor(R.color.primary) else requireContext().getColor(R.color.gray_light)
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (habitToEdit == null) "Add New Habit" else "Edit Habit")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                if (validationHelper.validateHabitForm(etHabitName, etTargetCount)) {
                    val name = etHabitName.text.toString().trim()
                    val description = etHabitDescription.text.toString().trim()
                    val targetCountStr = etTargetCount.text.toString().trim()
                    val targetCount = targetCountStr.toInt()

                    if (habitToEdit != null) {
                        val index = habits.indexOfFirst { it.id == habitToEdit.id }
                        if (index != -1) {
                            habits[index].name = name
                            habits[index].description = description.ifEmpty { "" }
                            habits[index].targetCount = targetCount
                            habits[index].icon = selectedEmoji
                            saveHabits()
                            habitAdapter.notifyItemChanged(index)
                            Toast.makeText(context, getString(R.string.habit_updated), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val newHabit = Habit(
                            name = name,
                            description = description.ifEmpty { "" },
                            icon = selectedEmoji,
                            targetCount = targetCount
                        )
                        habits.add(newHabit)
                        saveHabits()
                        habitAdapter.notifyItemInserted(habits.size - 1)
                        Toast.makeText(context, getString(R.string.habit_added), Toast.LENGTH_SHORT).show()
                    }
                    updateProgress()

                    //update widget after add/edit
                    HabitWidgetProvider.triggerWidgetUpdate(requireContext())
                } else {
                    Toast.makeText(context, "Please fill out the required fields correctly.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun toggleHabitCompletion(habit: Habit) {
        val position = habits.indexOf(habit)
        if (position != -1) {
            val isCurrentlyCompleted = habits[position].isCompleted
            habits[position].isCompleted = !isCurrentlyCompleted

            habits[position].currentCount = if (!isCurrentlyCompleted) habits[position].targetCount else 0

            saveHabits()
            habitAdapter.notifyItemChanged(position)
            updateProgress()
            val message = if (habits[position].isCompleted) getString(R.string.habit_completed) else getString(R.string.habit_uncompleted)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()


            //update the widget after change the  completion status of the habit
            HabitWidgetProvider.triggerWidgetUpdate(requireContext())
        }
    }

    private fun showDeleteDialog(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete this habit? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                val position = habits.indexOf(habit)
                if (position != -1) {
                    habits.removeAt(position)
                    saveHabits()
                    habitAdapter.notifyItemRemoved(position)
                    updateProgress()
                    Toast.makeText(context, getString(R.string.habit_deleted), Toast.LENGTH_SHORT).show()

                    //update the widget after delete the habit
                    HabitWidgetProvider.triggerWidgetUpdate(requireContext())
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateProgress() {
        val completed = habits.count { it.isCompleted }
        val total = habits.size
        progressText.text = "$completed/$total habits completed"
    }

    private fun shareProgress() {
        val completed = habits.count { it.isCompleted }
        val total = habits.size
        // Use floating-point division for accurate percentage
        val percentage = if (total > 0) (completed.toFloat() * 100).toInt() / total else 0
        val shareText = "My daily habit progress: $completed/$total habits completed ($percentage%) with WellnessApp!"
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun loadHabits() {
        habits.clear()
        habits.addAll(prefsManager.getHabits())
        habitAdapter.notifyDataSetChanged()
    }

    private fun saveHabits() {
        prefsManager.saveHabits(habits)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                stepCount = it.values[0].toInt()
                habits.find { h -> h.name.contains("Steps", ignoreCase = true) }?.let { stepHabit ->
                    val position = habits.indexOf(stepHabit)
                    val wasCompleted = stepHabit.isCompleted

                    stepHabit.currentCount = stepCount
                    if (stepCount >= stepHabit.targetCount && !stepHabit.isCompleted) {
                        stepHabit.isCompleted = true
                    }

                    if (wasCompleted != stepHabit.isCompleted) {

                        HabitWidgetProvider.triggerWidgetUpdate(requireContext())
                    }

                    saveHabits()
                    habitAdapter.notifyItemChanged(position)
                    updateProgress()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        stepSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        loadHabits()
        updateProgress()
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }
}