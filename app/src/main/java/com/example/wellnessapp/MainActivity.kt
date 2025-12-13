package com.example.wellnessapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.wellnessapp.utils.NotificationHelper
import com.example.wellnessapp.utils.SharedPrefsManager
import com.example.wellnessapp.R

class MainActivity : AppCompatActivity() {

    private lateinit var prefsManager: SharedPrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefsManager = SharedPrefsManager(this)

        setupNavigation()

        // Initialize notifications
        NotificationHelper(this).createNotificationChannel()

        // Schedule hydration reminders if enabled
        scheduleHydrationReminders()

        // Handle fragment navigation from dashboard
        handleFragmentNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set up the bottom navigation view with the NavController
        bottomNav.setupWithNavController(navController)

        // Handle re-selection of the same item to pop back to the top of the destination stack
        // This is a common pattern for "home" buttons
        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.navigation_dashboard) {
                // If the user is on another fragment and clicks dashboard, pop everything off
                // the back stack and navigate back to the dashboard.
                navController.popBackStack(R.id.navigation_dashboard, false)
            } else {
                // For other items, perform the normal navigation
                navController.navigate(item.itemId)
            }
            true
        }
    }

    private fun scheduleHydrationReminders() {
        if (prefsManager.isHydrationEnabled()) {
            NotificationHelper(this).scheduleHydrationReminder(
                prefsManager.getHydrationInterval().toLong()
            )
        }
    }

    private fun handleFragmentNavigation() {
        val fragment = intent.getStringExtra("fragment")
        if (fragment != null) {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController

            when (fragment) {
                "dashboard" -> navController.navigate(R.id.navigation_dashboard)
                "habits" -> navController.navigate(R.id.navigation_habits)
                "mood" -> navController.navigate(R.id.navigation_mood)
                "settings" -> navController.navigate(R.id.navigation_settings)
            }
        }
    }
}
