package com.example.wellnessapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.wellnessapp.adapters.OnboardingAdapter
import com.example.wellnessapp.utils.SharedPrefsManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var btnNext: Button
    private lateinit var btnSkip: Button
    private lateinit var btnGetStarted: Button
    private lateinit var prefsManager: SharedPrefsManager

    private val onboardingPages = listOf(
        OnboardingPage(
            "Welcome to WellnessApp",
            "Your personal wellness companion for a healthier lifestyle",
            R.drawable.ic_launcher_foreground
        ),
        OnboardingPage(
            "Track Your Habits",
            "Build healthy habits with our intuitive habit tracker",
            R.drawable.ic_water_drop
        ),
        OnboardingPage(
            "Monitor Your Mood",
            "Log your daily mood and track emotional wellness trends",
            R.drawable.ic_water_drop
        ),
        OnboardingPage(
            "Stay Hydrated",
            "Get smart reminders to drink water throughout the day",
            R.drawable.ic_water_drop
        ),
        OnboardingPage(
            "Secure & Private",
            "Your data is protected with PIN/Pattern security",
            R.drawable.ic_water_drop
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        prefsManager = SharedPrefsManager(this)
        
        // Check if user has already completed onboarding
        if (prefsManager.isOnboardingCompleted()) {
            startMainActivity()
            return
        }

        setupViews()
        setupViewPager()
        setupClickListeners()
    }

    private fun setupViews() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        btnNext = findViewById(R.id.btnNext)
        btnSkip = findViewById(R.id.btnSkip)
        btnGetStarted = findViewById(R.id.btnGetStarted)
    }

    private fun setupViewPager() {
        val adapter = OnboardingAdapter(onboardingPages)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtonVisibility(position)
            }
        })
    }

    private fun setupClickListeners() {
        btnNext.setOnClickListener {
            if (viewPager.currentItem < onboardingPages.size - 1) {
                viewPager.currentItem = viewPager.currentItem + 1
            }
        }

        btnSkip.setOnClickListener {
            completeOnboarding()
        }

        btnGetStarted.setOnClickListener {
            completeOnboarding()
        }
    }

    private fun updateButtonVisibility(position: Int) {
        val isLastPage = position == onboardingPages.size - 1
        btnNext.visibility = if (isLastPage) View.GONE else View.VISIBLE
        btnGetStarted.visibility = if (isLastPage) View.VISIBLE else View.GONE
    }

    private fun completeOnboarding() {
        prefsManager.setOnboardingCompleted(true)
        startMainActivity()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: Int
)
