package com.example.wellnessapp.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.example.wellnessapp.R // Import your app's R class

class SoundHelper(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var successSoundId: Int = 0
    private var errorSoundId: Int = 0
    private var clickSoundId: Int = 0

    companion object {
        private const val TAG = "SoundHelper"
    }

    init {
        initializeSounds()
    }

    private fun initializeSounds() {
        soundPool = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                SoundPool.Builder()
                    .setMaxStreams(3) // Max 3 sounds playing simultaneously
                    .setAudioAttributes(audioAttributes)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                SoundPool(3, AudioManager.STREAM_MUSIC, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing SoundPool", e)
            null // SoundPool initialization failed
        }

        // Load sound effects from res/raw
        // Make sure you have success_sound.mp3, error_sound.mp3, and click_sound.mp3
        // (or .ogg, .wav) in your app's res/raw/ folder.
        soundPool?.let { pool ->
            try {
                successSoundId = pool.load(context, R.raw.success_sound, 1)
                errorSoundId = pool.load(context, R.raw.error_sound, 1)
                clickSoundId = pool.load(context, R.raw.click_sound, 1)

                if (successSoundId == 0) Log.w(TAG, "Failed to load success_sound.")
                if (errorSoundId == 0) Log.w(TAG, "Failed to load error_sound.")
                if (clickSoundId == 0) Log.w(TAG, "Failed to load click_sound.")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading sounds from res/raw", e)
                // Ensure IDs are 0 if loading failed, so it falls back to vibration
                successSoundId = 0
                errorSoundId = 0
                clickSoundId = 0
            }
        }
    }

    fun playSuccessSound() {
        playSound(successSoundId)
    }

    fun playErrorSound() {
        playSound(errorSoundId)
    }

    fun playClickSound() {
        playSound(clickSoundId)
    }

    private fun playSound(soundId: Int) {
        if (soundPool != null && soundId != 0) {
            soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
        } else {
            // Fallback to vibration if sound is not available or SoundPool failed
            Log.d(TAG, "Sound ID $soundId not loaded or SoundPool unavailable. Vibrating instead.")
            vibrate()
        }
    }

    private fun vibrate() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (it.hasVibrator()) { // Check if device has a vibrator
                        it.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    if (it.hasVibrator()) {
                        it.vibrate(100)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during vibration", e)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        Log.d(TAG, "SoundPool released.")
    }
}

