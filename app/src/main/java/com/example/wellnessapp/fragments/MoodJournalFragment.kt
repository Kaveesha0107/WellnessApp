package com.example.wellnessapp.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter // Import ValueFormatter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.wellnessapp.R
import com.example.wellnessapp.adapters.MoodAdapter
import com.example.wellnessapp.models.MoodEntry
import com.example.wellnessapp.utils.SharedPrefsManager
import com.example.wellnessapp.utils.ValidationHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt

class MoodJournalFragment : Fragment(), SensorEventListener {

    private lateinit var prefsManager: SharedPrefsManager
    private lateinit var validationHelper: ValidationHelper
    private lateinit var moodAdapter: MoodAdapter
    private var moodEntries = mutableListOf<MoodEntry>()
    private lateinit var lineChart: LineChart

    // Sensor for shake detection
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastShakeTime: Long = 0
    private val SHAKE_THRESHOLD = 15.0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mood_journal, container, false)
        prefsManager = SharedPrefsManager(requireContext())
        validationHelper = ValidationHelper(requireContext())

        lineChart = view.findViewById(R.id.moodLineChart)
        setupChart()

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_mood_entries)
        moodAdapter = MoodAdapter(
            moods = moodEntries,
            onItemClick = { moodEntry ->
                showUpdateMoodDialog(moodEntry)
            },
            onDeleteClick = { moodEntry ->
                showDeleteMoodDialog(moodEntry)
            }
        )
        recyclerView.adapter = moodAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        view.findViewById<FloatingActionButton>(R.id.fab_add_mood).setOnClickListener {
            showAddMoodDialog()
        }


        view.findViewById<Button>(R.id.btn_share_mood).setOnClickListener {
            shareMoodSummary()
        }

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        loadMoodEntries()

        return view
    }

    private fun setupChart() {
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(false)
        lineChart.setPinchZoom(false)
        lineChart.isDragEnabled = false

        lineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setDrawAxisLine(true)
            textColor = resources.getColor(R.color.on_background, null)
            granularity = 1f
        }

        lineChart.axisLeft.apply {
            // Set axis bounds for mood values (0 to 5 for safety)
            axisMinimum = 0f
            axisMaximum = 5f
            setDrawGridLines(false)
            setDrawAxisLine(false)
            setDrawLabels(false)
        }
        lineChart.axisRight.isEnabled = false
        lineChart.legend.isEnabled = false
    }

    private fun updateChart() {
        if (moodEntries.isEmpty()) {
            lineChart.clear()
            lineChart.invalidate()
            return
        }

        // Sort entries by timestamp (ascending) for chronological display on chart
        val sortedEntries = moodEntries.sortedBy { it.timestamp }
        val entries = sortedEntries.mapIndexed { index, moodEntry ->
            // X-axis value is the index, Y-axis is the moodValue
            Entry(index.toFloat(), moodEntry.moodValue.toFloat())
        }

        // 1. Implement X-Axis Value Formatter to show dates
        val xAxisFormatter = object : ValueFormatter() {
            private val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                // Use sortedEntries to map the index back to the timestamp
                return if (index >= 0 && index < sortedEntries.size) {
                    dateFormat.format(Date(sortedEntries[index].timestamp))
                } else {
                    ""
                }
            }
        }

        // 2. Apply X-Axis Settings
        lineChart.xAxis.apply {
            valueFormatter = xAxisFormatter // Apply the date formatter
            // Set label count dynamically, max 7 labels, force centering
            setLabelCount(entries.size.coerceAtMost(7), true)
            labelRotationAngle = -45f // Rotate for better readability
            axisMinimum = 0f
            // Set max to the last index to ensure the last point is visible
            axisMaximum = entries.size.toFloat() - 1f
        }

        // 3. Update LineDataSet
        val dataSet = LineDataSet(entries, "Mood Over Time").apply {
            color = resources.getColor(R.color.primary, null)
            valueTextColor = resources.getColor(R.color.on_background, null)
            setDrawCircles(true)
            setDrawValues(false)
            circleColors = listOf(resources.getColor(R.color.primary, null))
            lineWidth = 2f
            circleRadius = 4f
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.notifyDataSetChanged()
        lineChart.invalidate() // Redraw chart
    }

    private fun loadMoodEntries() {
        moodEntries.clear()
        moodEntries.addAll(prefsManager.getMoodEntries())
        // Sorting here is for RecyclerView (descending)
        moodEntries.sortByDescending { it.timestamp }
        moodAdapter.notifyDataSetChanged()
        updateChart()
    }

    private fun saveMoodEntries() {
        prefsManager.saveMoodEntries(moodEntries)
        // Re-sort after saving (for RecyclerView)
        moodEntries.sortByDescending { it.timestamp }
        moodAdapter.notifyDataSetChanged()
        updateChart()
    }

    private fun addMoodEntry(moodEntry: MoodEntry) {
        moodEntries.add(0, moodEntry)
        saveMoodEntries()
        Toast.makeText(context, getString(R.string.mood_entry_saved), Toast.LENGTH_SHORT).show()
    }

    private fun updateMoodEntry(updatedEntry: MoodEntry) {
        val index = moodEntries.indexOfFirst { it.id == updatedEntry.id }
        if (index != -1) {
            moodEntries[index] = updatedEntry
            saveMoodEntries()
            Toast.makeText(context, getString(R.string.mood_entry_updated), Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteMoodEntry(moodEntry: MoodEntry) {
        moodEntries.remove(moodEntry)
        saveMoodEntries()
        Toast.makeText(context, getString(R.string.mood_entry_deleted), Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteMoodDialog(moodEntry: MoodEntry) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_mood_entry_title))
            .setMessage(getString(R.string.delete_mood_entry_message))
            .setPositiveButton(getString(R.string.delete_button_text)) { _, _ ->
                deleteMoodEntry(moodEntry)
            }
            .setNegativeButton(getString(R.string.cancel_button_text), null)
            .show()
    }

    private fun showAddMoodDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_mood, null)
        val builder = AlertDialog.Builder(context)
            .setTitle(getString(R.string.add_mood_dialog_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save_button_text), null)
            .setNegativeButton(getString(R.string.cancel_button_text), null)
        val dialog = builder.create()
        dialog.show()

        setupMoodDialog(dialogView, dialog) { emoji, note ->
            val newEntry = MoodEntry(
                id = UUID.randomUUID().toString(),
                moodEmoji = emoji,
                note = note,
                timestamp = System.currentTimeMillis(),
                moodValue = getMoodValueFromEmoji(emoji)
            )
            addMoodEntry(newEntry)
            dialog.dismiss()
        }
    }

    private fun showUpdateMoodDialog(moodEntry: MoodEntry) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_mood, null)
        val builder = AlertDialog.Builder(context)
            .setTitle(getString(R.string.update_mood_dialog_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save_button_text), null)
            .setNegativeButton(getString(R.string.cancel_button_text), null)
        val dialog = builder.create()
        dialog.show()

        val etNote = dialogView.findViewById<EditText>(R.id.et_mood_note)
        etNote.setText(moodEntry.note)

        setupMoodDialog(dialogView, dialog, moodEntry.moodEmoji) { emoji, note ->
            val updatedEntry = moodEntry.copy(
                moodEmoji = emoji,
                note = note,
                moodValue = getMoodValueFromEmoji(emoji)
            )
            updateMoodEntry(updatedEntry)
            dialog.dismiss()
        }
    }

    private fun setupMoodDialog(dialogView: View, dialog: AlertDialog, initialEmoji: String = "", onSave: (String, String) -> Unit) {
        val emojiGrid = dialogView.findViewById<GridLayout>(R.id.emoji_grid)
        val etNote = dialogView.findViewById<EditText>(R.id.et_mood_note)
        val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val emojis = listOf("😊", "😐", "😔", "😠", "😂", "😢", "😴", "🤩", "🤔")
        var selectedEmoji: String = initialEmoji.ifEmpty { emojis[0] }

        // add imojies dynimacally
        emojis.forEach { emoji ->
            val emojiView = TextView(context).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
                text = emoji
                gravity = Gravity.CENTER
                textSize = 32f
                setPadding(12, 12, 12, 12)
                background = context.getDrawable(R.drawable.emoji_button_background)
                setOnClickListener {
                    selectedEmoji = text.toString()
                    updateEmojiSelection(emojiGrid, selectedEmoji)
                }
            }
            emojiGrid.addView(emojiView)
        }


        updateEmojiSelection(emojiGrid, selectedEmoji)

        saveButton.setOnClickListener {
            val note = etNote.text.toString()
            if (note.length > 200) {
                Toast.makeText(context, getString(R.string.note_too_long), Toast.LENGTH_SHORT).show()
            } else {
                onSave(selectedEmoji, note)
            }
        }
    }

    private fun updateEmojiSelection(emojiGrid: GridLayout, selectedEmoji: String) {
        for (i in 0 until emojiGrid.childCount) {
            val emojiView = emojiGrid.getChildAt(i) as TextView
            emojiView.isSelected = (emojiView.text == selectedEmoji)
        }
    }

    private fun getMoodValueFromEmoji(emoji: String): Int {
        return when (emoji) {
            "😊" -> 5
            "😐" -> 3
            "😔" -> 1
            "😠" -> 0
            "😂" -> 5
            "😢" -> 1
            "😴" -> 2
            "🤩" -> 4
            "🤔" -> 3
            "🤷" -> 3
            else -> 3
        }
    }

    private fun shareMoodSummary() {
        val summaryText = moodEntries.joinToString("\n") {
            val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it.timestamp))
            val note = it.note.ifEmpty { "No note" }
            "$date - ${it.moodEmoji}: $note"
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "My Mood Journal Summary")
            putExtra(Intent.EXTRA_TEXT, summaryText)
        }
        startActivity(Intent.createChooser(shareIntent, "Share your mood journal"))
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]
                val acceleration = sqrt(x * x + y * y + z * z)
                if (acceleration > SHAKE_THRESHOLD) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastShakeTime > 1000) {
                        lastShakeTime = currentTime
                        onShakeDetected()
                    }
                }
            }
        }
    }

    private fun onShakeDetected() {
        Toast.makeText(context, getString(R.string.quick_mood_entry_added), Toast.LENGTH_SHORT).show()
        val quickEntry = MoodEntry(
            id = UUID.randomUUID().toString(),
            moodEmoji = "🤷",
            note = "Quick entry (shake)",
            timestamp = System.currentTimeMillis(),
            moodValue = 3
        )
        addMoodEntry(quickEntry)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        loadMoodEntries()
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }
}