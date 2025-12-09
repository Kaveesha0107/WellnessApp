package com.example.wellnessapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.wellnessapp.R
import com.example.wellnessapp.utils.NotificationHelper
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsFragment : Fragment() {

    private lateinit var prefsManager: SharedPrefsManager
    private lateinit var notificationHelper: NotificationHelper

    private var waterCountText: TextView? = null
    private var hydrationSwitch: SwitchMaterial? = null
    private var intervalSpinner: Spinner? = null
    private var intervalLabel: TextView? = null
    private var progressBar: ProgressBar? = null
    private var percentageText: TextView? = null
    private val waterGlasses = mutableListOf<TextView>()

    private val totalGlasses = 8
    private val mlPerGlass = 250

    private val intervalValues = arrayOf(30, 60, 120, 180, 240)
    private lateinit var intervalLabels: Array<String>

    companion object {
        private const val TAG = "SettingsFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        intervalLabels = arrayOf(
            getString(R.string.interval_30_minutes),
            getString(R.string.interval_60_minutes),
            getString(R.string.interval_120_minutes),
            getString(R.string.interval_180_minutes),
            getString(R.string.interval_240_minutes)
        )
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

        prefsManager = SharedPrefsManager(requireContext())
        notificationHelper = NotificationHelper(requireContext())

        setupUI(view)
        setupListeners()
        loadAndApplySettings()
    }

    private fun setupUI(view: View) {
        waterCountText = view.findViewById(R.id.tv_water_count)
        hydrationSwitch = view.findViewById(R.id.switch_hydration)
        intervalSpinner = view.findViewById(R.id.spinner_interval)
        intervalLabel = view.findViewById(R.id.tv_interval_label)
        progressBar = view.findViewById(R.id.hydration_progress_bar)
        percentageText = view.findViewById(R.id.tv_water_percentage)

        waterGlasses.clear()
        waterGlasses.add(view.findViewById(R.id.glass1))
        waterGlasses.add(view.findViewById(R.id.glass2))
        waterGlasses.add(view.findViewById(R.id.glass3))
        waterGlasses.add(view.findViewById(R.id.glass4))
        waterGlasses.add(view.findViewById(R.id.glass5))
        waterGlasses.add(view.findViewById(R.id.glass6))
        waterGlasses.add(view.findViewById(R.id.glass7))
        waterGlasses.add(view.findViewById(R.id.glass8))

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, intervalLabels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        intervalSpinner?.adapter = adapter

        updateWaterCountDisplay()
    }

    private fun setupListeners() {
        view?.findViewById<Button>(R.id.btn_add_water)?.setOnClickListener {
            if (prefsManager.getDailyWaterCount() < totalGlasses) {
                prefsManager.incrementWaterCount()
                updateWaterCountDisplay()
                Toast.makeText(context, getString(R.string.water_added), Toast.LENGTH_SHORT).show()
                if (prefsManager.getDailyWaterCount() == totalGlasses) {
                    Toast.makeText(context, getString(R.string.goal_achieved), Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "You have already reached your daily goal!", Toast.LENGTH_SHORT).show()
            }
        }

        view?.findViewById<Button>(R.id.btn_reset_water)?.setOnClickListener {
            prefsManager.resetDailyWaterCount()
            updateWaterCountDisplay()
            Toast.makeText(context, getString(R.string.water_count_reset), Toast.LENGTH_SHORT).show()
        }

        hydrationSwitch?.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.setHydrationEnabled(isChecked)
            updateIntervalSpinnerState(isChecked)
            if (isChecked) {
                notificationHelper.scheduleHydrationReminder(prefsManager.getHydrationInterval().toLong())
                Toast.makeText(context, getString(R.string.hydration_reminders_enabled), Toast.LENGTH_SHORT).show()
            } else {
                notificationHelper.cancelHydrationReminder()
                Toast.makeText(context, getString(R.string.hydration_reminders_disabled), Toast.LENGTH_SHORT).show()
            }
        }

        intervalSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedInterval = intervalValues[position]
                if (prefsManager.getHydrationInterval() != selectedInterval) {
                    prefsManager.setHydrationInterval(selectedInterval)
                    notificationHelper.cancelHydrationReminder()
                    if (prefsManager.isHydrationEnabled()) {
                        notificationHelper.scheduleHydrationReminder(selectedInterval.toLong())
                        Toast.makeText(context, getString(R.string.reminder_interval_updated), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        view?.findViewById<Button>(R.id.btn_clear_data)?.setOnClickListener {
            showClearDataDialog()
        }
    }

    private fun loadAndApplySettings() {
        val isEnabled = prefsManager.isHydrationEnabled()
        hydrationSwitch?.isChecked = isEnabled
        updateIntervalSpinnerState(isEnabled)

        val savedInterval = prefsManager.getHydrationInterval()
        val position = intervalValues.indexOf(savedInterval)
        if (position != -1) {
            intervalSpinner?.setSelection(position)
        }
    }

    private fun updateIntervalSpinnerState(isEnabled: Boolean) {
        intervalSpinner?.isEnabled = isEnabled
        intervalLabel?.isEnabled = isEnabled
    }

    private fun updateWaterCountDisplay() {
        val dailyWaterCount = prefsManager.getDailyWaterCount()
        val totalWaterGoal = totalGlasses * mlPerGlass
        val currentWaterAmount = dailyWaterCount * mlPerGlass

        waterCountText?.text = String.format("%d/%d glasses (%dml)", dailyWaterCount, totalGlasses, currentWaterAmount)

        for (i in waterGlasses.indices) {
            val glass = waterGlasses[i]
            if (i < dailyWaterCount) {
                glass.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.light_blue_tint)
                glass.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_fill))
            } else {
                glass.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.grey_bg)
                glass.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_text))
            }
        }

        val progressPercentage = ((dailyWaterCount.toFloat() / totalGlasses.toFloat()) * 100).toInt()
        progressBar?.progress = progressPercentage
        percentageText?.text = String.format("%d%%", progressPercentage)
    }

    private fun showClearDataDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.clear_all_data_title))
            .setMessage(getString(R.string.clear_all_data_message))
            .setPositiveButton(getString(R.string.clear_button_text)) { _, _ ->
                prefsManager.saveHabits(emptyList())
                prefsManager.saveMoodEntries(emptyList())
                prefsManager.resetDailyWaterCount()
                updateWaterCountDisplay()
                Toast.makeText(context, getString(R.string.all_data_cleared), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.cancel_button_text), null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume - reloading and applying settings")
        updateWaterCountDisplay()
        loadAndApplySettings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        waterCountText = null
        hydrationSwitch = null
        intervalSpinner = null
        intervalLabel = null
        progressBar = null
        percentageText = null
        waterGlasses.clear()
    }
}