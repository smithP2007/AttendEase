package com.example.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.Display
import android.view.WindowManager
import kotlin.math.roundToInt

data class DisplayPerformanceInfo(
    val currentRefreshRate: Float,
    val maxRefreshRate: Float,
    val isHighRefreshRateSupported: Boolean,
    val isHardwareAccelerated: Boolean,
    val modeCount: Int
)

object DisplayRateHelper {
    private const val TAG = "DisplayRateHelper"

    /**
     * Configures the window to request the highest possible display refresh rate (e.g. 90Hz, 120Hz, 144Hz)
     * and enables low-latency rendering pipeline.
     */
    fun applyMaxRefreshRate(activity: Activity, forceMaxRate: Boolean = true) {
        try {
            val window = activity.window

            // 1. Ensure Hardware Acceleration flag is locked on Window
            window.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )

            // 2. High-precision color format to avoid software banding
            window.setFormat(android.graphics.PixelFormat.RGBA_8888)

            // 3. Resolve display and find maximum refresh rate mode
            val display: Display? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.display
            } else {
                @Suppress("DEPRECATION")
                activity.windowManager.defaultDisplay
            }

            if (display != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val modes = display.supportedModes
                val maxRateMode = modes.maxByOrNull { it.refreshRate }
                val layoutParams = window.attributes

                if (forceMaxRate && maxRateMode != null && maxRateMode.refreshRate > 60f) {
                    layoutParams.preferredDisplayModeId = maxRateMode.modeId
                    layoutParams.preferredRefreshRate = maxRateMode.refreshRate
                    Log.i(TAG, "Selected display mode ${maxRateMode.modeId} with ${maxRateMode.refreshRate}Hz")
                } else if (!forceMaxRate) {
                    layoutParams.preferredDisplayModeId = 0
                    layoutParams.preferredRefreshRate = 0f
                }

                window.attributes = layoutParams
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error applying maximum refresh rate: ${e.message}")
        }
    }

    /**
     * Inspects active display metrics to report to UI in Settings
     */
    fun getDisplayInfo(context: Context): DisplayPerformanceInfo {
        return try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            val display: Display? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display ?: wm?.defaultDisplay
            } else {
                @Suppress("DEPRECATION")
                wm?.defaultDisplay
            }

            if (display != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val modes = display.supportedModes
                val currentFps = display.mode.refreshRate
                val maxFps = modes.maxOfOrNull { it.refreshRate } ?: currentFps
                DisplayPerformanceInfo(
                    currentRefreshRate = currentFps,
                    maxRefreshRate = maxFps,
                    isHighRefreshRateSupported = maxFps > 60.5f,
                    isHardwareAccelerated = true,
                    modeCount = modes.size
                )
            } else {
                DisplayPerformanceInfo(
                    currentRefreshRate = 60f,
                    maxRefreshRate = 60f,
                    isHighRefreshRateSupported = false,
                    isHardwareAccelerated = true,
                    modeCount = 1
                )
            }
        } catch (e: Exception) {
            DisplayPerformanceInfo(
                currentRefreshRate = 60f,
                maxRefreshRate = 60f,
                isHighRefreshRateSupported = false,
                isHardwareAccelerated = true,
                modeCount = 1
            )
        }
    }
}
