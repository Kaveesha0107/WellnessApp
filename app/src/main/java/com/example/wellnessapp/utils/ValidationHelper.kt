package com.example.wellnessapp.utils

import android.content.Context
import android.widget.Toast
import com.example.wellnessapp.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class ValidationHelper(private val context: Context) {


    private fun validateNotEmpty(input: TextInputEditText, errorMessageResource: Int): Boolean {
        val isValid = input.text.toString().trim().isNotEmpty()
        if (!isValid) {
            // Set error on the parent TextInputLayout if it exists
            val parent = input.parent.parent
            if (parent is TextInputLayout) {
                parent.error = context.getString(errorMessageResource)
            }
        } else {
            // Clear any previous error
            val parent = input.parent.parent
            if (parent is TextInputLayout) {
                parent.error = null
            }
        }
        return isValid
    }


    fun validateHabitForm(
        nameInput: TextInputEditText,
        targetInput: TextInputEditText
    ): Boolean {
        // Clear previous errors first
        (nameInput.parent.parent as? TextInputLayout)?.error = null
        (targetInput.parent.parent as? TextInputLayout)?.error = null

        var isValid = true

        // Validate habit name
        val name = nameInput.text.toString().trim()
        if (name.isEmpty()) {
            (nameInput.parent.parent as? TextInputLayout)?.error = context.getString(R.string.habit_name_required)
            isValid = false
        } else if (name.length < 2) {
            (nameInput.parent.parent as? TextInputLayout)?.error = context.getString(R.string.habit_name_too_short)
            isValid = false
        } else if (name.length > 50) {
            (nameInput.parent.parent as? TextInputLayout)?.error = context.getString(R.string.habit_name_too_long)
            isValid = false
        }

        // Validate target count
        val targetText = targetInput.text.toString().trim()
        if (targetText.isEmpty()) {
            (targetInput.parent.parent as? TextInputLayout)?.error = context.getString(R.string.target_count_required)
            isValid = false
        } else {
            try {
                val targetValue = targetText.toInt()
                if (targetValue <= 0) {
                    (targetInput.parent.parent as? TextInputLayout)?.error = context.getString(R.string.target_count_positive)
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                (targetInput.parent.parent as? TextInputLayout)?.error = context.getString(R.string.target_count_numeric)
                isValid = false
            }
        }

        if (!isValid) {
            Toast.makeText(context, "Validation failed. Please check your input.", Toast.LENGTH_SHORT).show()
        }

        return isValid
    }


    fun showValidationError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    fun showValidationSuccess(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    fun validatePinForm(pinInput: TextInputEditText): Boolean {
        // Clear previous error
        (pinInput.parent.parent as? TextInputLayout)?.error = null

        val pin = pinInput.text.toString().trim()
        var isValid = true

        if (pin.isEmpty()) {
            (pinInput.parent.parent as? TextInputLayout)?.error = context.getString(R.string.pin_required)
            isValid = false
        } else if (pin.length != 4) {
            (pinInput.parent.parent as? TextInputLayout)?.error = context.getString(R.string.pin_must_be_4_digits)
            isValid = false
        } else if (!pin.all { it.isDigit() }) {
            (pinInput.parent.parent as? TextInputLayout)?.error = context.getString(R.string.pin_must_be_numeric)
            isValid = false
        }

        if (!isValid) {
            showValidationError("Invalid PIN. Please enter a 4-digit number.")
        }

        return isValid
    }
}
