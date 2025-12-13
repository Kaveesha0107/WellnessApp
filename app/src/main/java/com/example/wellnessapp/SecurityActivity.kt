package com.example.wellnessapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnessapp.utils.SharedPrefsManager
import com.example.wellnessapp.utils.ValidationHelper
import com.google.android.material.textfield.TextInputEditText

class SecurityActivity : AppCompatActivity() {

    private lateinit var prefsManager: SharedPrefsManager
    private lateinit var validationHelper: ValidationHelper
    private lateinit var etPin: TextInputEditText
    private lateinit var btnSubmit: Button
    private lateinit var btnSetPin: Button
    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security)

        prefsManager = SharedPrefsManager(this)
        validationHelper = ValidationHelper(this)
        setupViews()
        checkSecuritySetup()
    }

    private fun setupViews() {
        etPin = findViewById(R.id.etPin)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnSetPin = findViewById(R.id.btnSetPin)
        tvTitle = findViewById(R.id.tvTitle)
        tvSubtitle = findViewById(R.id.tvSubtitle)

        btnSubmit.setOnClickListener {
            handlePinSubmit()
        }
        btnSetPin.setOnClickListener {
            handleSetPin()
        }
    }

    private fun checkSecuritySetup() {
        val savedPin = prefsManager.getSecurityPin()
        if (savedPin.isEmpty()) {
            tvTitle.text = getString(R.string.security_title_set_pin)
            tvSubtitle.text = getString(R.string.security_subtitle_set_pin)
            btnSubmit.visibility = View.GONE
            btnSetPin.visibility = View.VISIBLE
        } else {
            tvTitle.text = getString(R.string.security_title_enter_pin)
            tvSubtitle.text = getString(R.string.security_subtitle_enter_pin)
            btnSubmit.visibility = View.VISIBLE
            btnSetPin.visibility = View.GONE
        }
    }

    private fun handlePinSubmit() {
        if (validationHelper.validatePinForm(etPin)) {
            val enteredPin = etPin.text.toString()
            val savedPin = prefsManager.getSecurityPin()

            if (enteredPin == savedPin) {
                // Navigate directly to MainActivity with the "dashboard" fragment as the starting point
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("fragment", "dashboard")
                }
                startActivity(intent)
                finish()
            } else {
                validationHelper.showValidationError(getString(R.string.security_incorrect_pin))
                etPin.text?.clear()
            }
        }
    }

    private fun handleSetPin() {
        if (validationHelper.validatePinForm(etPin)) {
            val enteredPin = etPin.text.toString()
            prefsManager.setSecurityPin(enteredPin)
            validationHelper.showValidationSuccess(getString(R.string.security_pin_set))
            // Navigate directly to MainActivity with the "dashboard" fragment after setting the PIN
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("fragment", "dashboard")
            }
            startActivity(intent)
            finish()
        }
    }
}
