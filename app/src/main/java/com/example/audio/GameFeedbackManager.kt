package com.example.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class GameFeedbackManager(private val context: Context) {

    private var toneGenerator: ToneGenerator? = null
    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (e: Exception) {
        null
    }

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (e: Exception) {
            Log.e("GameFeedbackManager", "Failed to init ToneGenerator", e)
        }
    }

    fun playClick() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
            vibrate(15)
        } catch (_: Exception) {}
    }

    fun playCorrect() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 80)
            vibrate(40)
        } catch (_: Exception) {}
    }

    fun playCombo(comboCount: Int) {
        try {
            val tone = when (comboCount % 3) {
                1 -> ToneGenerator.TONE_DTMF_A
                2 -> ToneGenerator.TONE_DTMF_B
                else -> ToneGenerator.TONE_DTMF_C
            }
            toneGenerator?.startTone(tone, 90)
            vibrate(50)
        } catch (_: Exception) {}
    }

    fun playWrong() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 120)
            vibrate(100)
        } catch (_: Exception) {}
    }

    fun playLevelSuccess() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
            vibratePattern(longArrayOf(0, 50, 50, 80))
        } catch (_: Exception) {}
    }

    fun playPowerUpUsed() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_DTMF_D, 100)
            vibrate(60)
        } catch (_: Exception) {}
    }

    private fun vibrate(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun vibratePattern(pattern: LongArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, -1)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
