package com.example.wellnessapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {

    // Splash screen will be visible for 3 seconds
    private val SPLASH_TIME_OUT: Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Using a Handler to delay the transition to the next activity
        Handler(Looper.getMainLooper()).postDelayed({
            // Start the next activity
            val intent = Intent(this, SecurityActivity::class.java)
            startActivity(intent)

            // Close this activity
            finish()
        }, SPLASH_TIME_OUT)
    }
}
