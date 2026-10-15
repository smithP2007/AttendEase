package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AttendEaseApp
import com.example.ui.MainViewModel
import com.example.ui.theme.AttendEaseTheme
import com.example.util.DisplayRateHelper
import com.example.util.TouchFeedback

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Hardware Acceleration: Guarantee GPU rendering on window
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )

        // 2. High-precision color format for smooth banding-free GPU compositing
        window.setFormat(android.graphics.PixelFormat.RGBA_8888)

        // 3. Low-latency tactile feedback enabled at window level
        window.decorView.isHapticFeedbackEnabled = true

        // 4. Maximum Refresh Rate: Apply peak display refresh rate (120Hz/90Hz)
        DisplayRateHelper.applyMaxRefreshRate(this, forceMaxRate = true)

        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsStateWithLifecycle()
            val accentTheme by viewModel.accentTheme.collectAsStateWithLifecycle()
            val maxRefreshRateEnabled by viewModel.maxRefreshRateEnabled.collectAsStateWithLifecycle()
            val lowLatencyHaptics by viewModel.lowLatencyHaptics.collectAsStateWithLifecycle()

            // Dynamically re-apply display rate whenever user toggles the setting
            LaunchedEffect(maxRefreshRateEnabled) {
                DisplayRateHelper.applyMaxRefreshRate(this@MainActivity, forceMaxRate = maxRefreshRateEnabled)
            }

            // Dynamically sync haptics state with window and TouchFeedback
            LaunchedEffect(lowLatencyHaptics) {
                TouchFeedback.hapticsEnabled = lowLatencyHaptics
                window.decorView.isHapticFeedbackEnabled = lowLatencyHaptics
            }

            AttendEaseTheme(
                themeMode = themeMode,
                dynamicColor = dynamicColorEnabled,
                accentTheme = accentTheme
            ) {
                AttendEaseApp(viewModel = viewModel)
            }
        }
    }
}
