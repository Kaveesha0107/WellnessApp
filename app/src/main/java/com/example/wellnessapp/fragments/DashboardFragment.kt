package com.example.wellnessapp.fragments

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.wellnessapp.R
import com.example.wellnessapp.utils.SharedPrefsManager
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private lateinit var prefsManager: SharedPrefsManager
    private lateinit var tvWelcome: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvHabitProgress: TextView
    private lateinit var tvMoodCount: TextView
    private lateinit var tvWaterCount: TextView
    private lateinit var habitProgressChart: PieChart

    // Cards
    private lateinit var cardHabits: CardView
    private lateinit var cardMood: CardView
    private lateinit var cardHydration: CardView
    private lateinit var cardWidget: CardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefsManager = SharedPrefsManager(requireContext())
        setupViews(view)
        setupClickListeners()
        updateDashboard()
        setupAnimations()
    }

    private fun setupViews(view: View) {
        // Initialize TextViews
        tvWelcome = view.findViewById(R.id.tvWelcome)
        tvDate = view.findViewById(R.id.tvDate)
        tvHabitProgress = view.findViewById(R.id.tvHabitProgress)
        tvMoodCount = view.findViewById(R.id.tvMoodCount)
        tvWaterCount = view.findViewById(R.id.tvWaterCount)
        habitProgressChart = view.findViewById(R.id.habitProgressChart)

        // Initialize CardViews
        cardHabits = view.findViewById(R.id.cardHabits)
        cardMood = view.findViewById(R.id.cardMood)
        cardHydration = view.findViewById(R.id.cardHydration)
        cardWidget = view.findViewById(R.id.cardWidget)

        // Setup the chart with enhanced styling
        setupPieChart()
    }

    private fun setupPieChart() {
        habitProgressChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(ContextCompat.getColor(requireContext(), R.color.surface))
            holeRadius = 60f
            setTransparentCircleAlpha(0)
            setDrawEntryLabels(false)
            setTouchEnabled(false)
            setRotationEnabled(false)
            setCenterTextSize(16f)
            setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface))
            animateY(1000)
        }
    }

    private fun setupClickListeners() {
        // Enhanced click listeners with haptic feedback
        cardHabits.setOnClickListener {
            animateCardClick(cardHabits) {
                findNavController().navigate(R.id.navigation_habits)
            }
        }

        cardMood.setOnClickListener {
            animateCardClick(cardMood) {
                findNavController().navigate(R.id.navigation_mood)
            }
        }

        cardHydration.setOnClickListener {
            animateCardClick(cardHydration) {
                findNavController().navigate(R.id.navigation_settings)
            }
        }

        cardWidget.setOnClickListener {
            animateCardClick(cardWidget) {
                showWidgetInfo()
            }
        }
    }

    private fun animateCardClick(card: CardView, action: () -> Unit) {
        // Scale animation for card press
        card.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                card.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .withEndAction { action.invoke() }
                    .start()
            }
            .start()
    }

    private fun showWidgetInfo() {
        val message = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            "Long press on home screen → Widgets → WellCore to add widget! 📱"
        } else {
            "Widget feature available on Android 8.0+ 📱"
        }

        android.widget.Toast.makeText(
            context,
            message,
            android.widget.Toast.LENGTH_LONG
        ).show()
    }

    private fun setupAnimations() {
        // Staggered animation for cards
        val cards = listOf(cardHabits, cardMood, cardHydration, cardWidget)
        cards.forEachIndexed { index, card ->
            card.alpha = 0f
            card.translationY = 50f
            card.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay(index * 100L)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
    }

    private fun updateDashboard() {
        updateWelcomeSection()
        updateStatistics()
    }

    private fun updateWelcomeSection() {
        // Dynamic welcome message based on time
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when (hour) {
            in 0..11 -> "Good Morning! ☀️"
            in 12..17 -> "Good Afternoon! 🌤️"
            else -> "Good Evening! 🌙"
        }

        tvWelcome.text = greeting

        // Current date with better formatting
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        tvDate.text = dateFormat.format(Date())
    }

    private fun updateStatistics() {
        val habits = prefsManager.getHabits()
        val completedHabits = habits.count { it.isCompleted }
        val totalHabits = habits.size

        // Update habit progress with animation
        updateHabitProgress(completedHabits, totalHabits)

        // Update mood entries
        updateMoodStats()

        // Update water intake
        updateWaterStats()
    }

    private fun updateHabitProgress(completed: Int, total: Int) {
        val percentage = if (total > 0) (completed * 100) / total else 0


        val progressText = "$completed/$total habits  ($percentage%)"

        // Animate the text update
        animateTextChange(tvHabitProgress, progressText)

        // Update chart
        updateHabitProgressChart(completed, total, percentage)
    }

    private fun updateMoodStats() {
        val moodEntries = prefsManager.getMoodEntries()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayMoods = moodEntries.count {
            val entryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp))
            entryDate == today
        }

        val moodText = when (todayMoods) {
            0 -> "No entries today"
            1 -> "1 entry today"
            else -> "$todayMoods entries today"
        }

        animateTextChange(tvMoodCount, moodText)
    }

    private fun updateWaterStats() {
        val waterCount = prefsManager.getDailyWaterCount()
        val goalReached = waterCount >= 8 // Assuming 8 glasses goal

        val waterText = if (goalReached) {
            "$waterCount glasses ✅"
        } else {
            "$waterCount glasses"
        }

        animateTextChange(tvWaterCount, waterText)
    }

    private fun updateHabitProgressChart(completed: Int, total: Int, percentage: Int) {
        if (total == 0) {
            habitProgressChart.clear()
            habitProgressChart.centerText = "No habits\nyet"
            habitProgressChart.invalidate()
            return
        }

        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        if (completed > 0) {
            entries.add(PieEntry(completed.toFloat(), "Completed"))
            colors.add(ContextCompat.getColor(requireContext(), R.color.primary))
        }

        val remaining = total - completed
        if (remaining > 0) {
            entries.add(PieEntry(remaining.toFloat(), "Remaining"))
            colors.add(ContextCompat.getColor(requireContext(), R.color.divider))
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            setDrawValues(false)
            setDrawIcons(false)
            sliceSpace = 2f
            selectionShift = 5f
        }

        val data = PieData(dataSet)
        habitProgressChart.data = data

        // Enhanced center text
        val centerText = when {
            percentage == 100 -> "Perfect!\n$completed/$total"
            percentage >= 75 -> "Great!\n$completed/$total"
            percentage >= 50 -> "Good!\n$completed/$total"
            percentage > 0 -> "Keep going!\n$completed/$total"
            else -> "Get started!\n$completed/$total"
        }

        habitProgressChart.centerText = centerText
        habitProgressChart.animateY(800)
    }

    private fun animateTextChange(textView: TextView, newText: String) {
        // Fade out, change text, fade in
        textView.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                textView.text = newText
                textView.animate()
                    .alpha(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }

    private fun animateCountUp(textView: TextView, targetValue: Int, suffix: String = "") {
        val animator = ValueAnimator.ofInt(0, targetValue)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            textView.text = "${animation.animatedValue}$suffix"
        }
        animator.start()
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()

        // Add subtle pulse animation to welcome card
        view?.findViewById<CardView>(R.id.cardWelcome)?.let { welcomeCard ->
            welcomeCard.animate()
                .scaleX(1.02f)
                .scaleY(1.02f)
                .setDuration(1000)
                .withEndAction {
                    welcomeCard.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(1000)
                        .start()
                }
                .start()
        }
    }

    override fun onPause() {
        super.onPause()
        // Clear any ongoing animations to prevent memory leaks
        view?.findViewById<CardView>(R.id.cardWelcome)?.clearAnimation()
        cardHabits.clearAnimation()
        cardMood.clearAnimation()
        cardHydration.clearAnimation()
        cardWidget.clearAnimation()
    }
}