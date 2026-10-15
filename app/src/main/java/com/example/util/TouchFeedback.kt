package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

object TouchFeedback {
    @Volatile
    var hapticsEnabled: Boolean = true

    private var vibrator: Vibrator? = null

    private fun isHapticsAllowed(context: Context): Boolean {
        if (!hapticsEnabled) return false
        return try {
            val prefs = context.getSharedPreferences("attendease_settings_prefs", Context.MODE_PRIVATE)
            prefs.getBoolean("pref_low_latency_haptics", true)
        } catch (_: Exception) {
            hapticsEnabled
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        if (vibrator == null) {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        }
        return vibrator
    }

    /**
     * Ultra-low latency light tick (e.g. for button tap or chip select)
     */
    fun tick(context: Context) {
        if (!isHapticsAllowed(context)) return
        try {
            val vib = getVibrator(context) ?: return
            if (!vib.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(8L)
            }
        } catch (_: Exception) {}
    }

    /**
     * Low-latency crisp click (e.g. marking Present / Absent or saving record)
     */
    fun click(context: Context) {
        if (!isHapticsAllowed(context)) return
        try {
            val vib = getVibrator(context) ?: return
            if (!vib.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(15L)
            }
        } catch (_: Exception) {}
    }

    /**
     * Heavy haptic pulse for alert or delete action
     */
    fun heavy(context: Context) {
        if (!isHapticsAllowed(context)) return
        try {
            val vib = getVibrator(context) ?: return
            if (!vib.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(30L)
            }
        } catch (_: Exception) {}
    }

    /**
     * View-based fallback for immediate window feedback
     */
    fun performHaptic(view: View, constant: Int = HapticFeedbackConstants.KEYBOARD_TAP) {
        if (!isHapticsAllowed(view.context)) return
        try {
            view.performHapticFeedback(constant, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
        } catch (_: Exception) {}
    }
}
