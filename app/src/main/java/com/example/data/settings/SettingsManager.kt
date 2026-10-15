package com.example.data.settings

import android.content.Context
import android.content.SharedPreferences
import com.example.data.remote.GeminiAdvisorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    AMOLED
}

enum class AccentTheme(val label: String, val hexPreview: String) {
    CYAN("Muzli Teal", "#00D4B2"),
    PURPLE("Amadine Violet", "#9B5DE5"),
    ROSE("Magnific Sunset", "#FF5E7E"),
    AMBER("Muzli Gold", "#FFB020"),
    EMERALD("Neon Mint", "#00E5A3"),
    INDIGO("Electric Indigo", "#6C63FF"),
    SLATE("Cyber Blue", "#38BDF8"),
    DYNAMIC("Wallpaper Dynamic", "#6750A4")
}

data class StudentProfile(
    val name: String = "Student",
    val rollNo: String = "2026-CS-42",
    val university: String = "Universal Institute of Tech",
    val semester: String = "Semester 6 • Computer Science"
)

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("attendease_settings_prefs", Context.MODE_PRIVATE)

    // Appearance: Default to Muzli Dark UI (DARK + CYAN + Dynamic Color false)
    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _dynamicColorEnabled = MutableStateFlow(prefs.getBoolean(KEY_DYNAMIC_COLOR, false))
    val dynamicColorEnabled: StateFlow<Boolean> = _dynamicColorEnabled.asStateFlow()

    private val _accentTheme = MutableStateFlow(loadAccentTheme())
    val accentTheme: StateFlow<AccentTheme> = _accentTheme.asStateFlow()

    private val _compactCardView = MutableStateFlow(prefs.getBoolean(KEY_COMPACT_CARDS, false))
    val compactCardView: StateFlow<Boolean> = _compactCardView.asStateFlow()

    // Smoothness & Rendering Performance
    private val _maxRefreshRateEnabled = MutableStateFlow(prefs.getBoolean(KEY_MAX_REFRESH_RATE, true))
    val maxRefreshRateEnabled: StateFlow<Boolean> = _maxRefreshRateEnabled.asStateFlow()

    private val _lowLatencyHaptics = MutableStateFlow(prefs.getBoolean(KEY_LOW_LATENCY_HAPTICS, true))
    val lowLatencyHaptics: StateFlow<Boolean> = _lowLatencyHaptics.asStateFlow()

    private val _gpuHardwareLayers = MutableStateFlow(prefs.getBoolean(KEY_GPU_HARDWARE_LAYERS, true))
    val gpuHardwareLayers: StateFlow<Boolean> = _gpuHardwareLayers.asStateFlow()

    // Academic Criteria
    private val _targetPercentage = MutableStateFlow(prefs.getFloat(KEY_TARGET_PERCENTAGE, 75.0f).toDouble())
    val targetPercentage: StateFlow<Double> = _targetPercentage.asStateFlow()

    private val _warningMargin = MutableStateFlow(prefs.getFloat(KEY_WARNING_MARGIN, 5.0f).toDouble())
    val warningMargin: StateFlow<Double> = _warningMargin.asStateFlow()

    private val _strictSingleRegistration = MutableStateFlow(prefs.getBoolean(KEY_STRICT_REGISTRATION, true))
    val strictSingleRegistration: StateFlow<Boolean> = _strictSingleRegistration.asStateFlow()

    private val _includeWeekendDays = MutableStateFlow(prefs.getBoolean(KEY_INCLUDE_WEEKENDS, false))
    val includeWeekendDays: StateFlow<Boolean> = _includeWeekendDays.asStateFlow()

    // Student Profile
    private val _studentProfile = MutableStateFlow(loadStudentProfile())
    val studentProfile: StateFlow<StudentProfile> = _studentProfile.asStateFlow()

    // AI Configuration
    private val _selectedModel = MutableStateFlow(
        GeminiAdvisorService.normalizeApiModelId(
            prefs.getString(KEY_SELECTED_MODEL, "gemini-2.5-flash") ?: "gemini-2.5-flash"
        )
    )
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _autoApplyAiActions = MutableStateFlow(prefs.getBoolean(KEY_AUTO_APPLY_ACTIONS, true))
    val autoApplyAiActions: StateFlow<Boolean> = _autoApplyAiActions.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
        _dynamicColorEnabled.value = enabled
    }

    fun setAccentTheme(accent: AccentTheme) {
        prefs.edit().putString(KEY_ACCENT_THEME, accent.name).apply()
        _accentTheme.value = accent
    }

    fun setCompactCardView(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_COMPACT_CARDS, enabled).apply()
        _compactCardView.value = enabled
    }

    fun setMaxRefreshRateEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MAX_REFRESH_RATE, enabled).apply()
        _maxRefreshRateEnabled.value = enabled
    }

    fun setLowLatencyHaptics(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LOW_LATENCY_HAPTICS, enabled).apply()
        _lowLatencyHaptics.value = enabled
    }

    fun setGpuHardwareLayers(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GPU_HARDWARE_LAYERS, enabled).apply()
        _gpuHardwareLayers.value = enabled
    }

    fun setTargetPercentage(target: Double) {
        val coerced = target.coerceIn(50.0, 95.0)
        prefs.edit().putFloat(KEY_TARGET_PERCENTAGE, coerced.toFloat()).apply()
        _targetPercentage.value = coerced
    }

    fun setWarningMargin(margin: Double) {
        val coerced = margin.coerceIn(1.0, 15.0)
        prefs.edit().putFloat(KEY_WARNING_MARGIN, coerced.toFloat()).apply()
        _warningMargin.value = coerced
    }

    fun setStrictSingleRegistration(strict: Boolean) {
        prefs.edit().putBoolean(KEY_STRICT_REGISTRATION, strict).apply()
        _strictSingleRegistration.value = strict
    }

    fun setIncludeWeekendDays(include: Boolean) {
        prefs.edit().putBoolean(KEY_INCLUDE_WEEKENDS, include).apply()
        _includeWeekendDays.value = include
    }

    fun updateStudentProfile(name: String, rollNo: String, university: String, semester: String) {
        prefs.edit()
            .putString(KEY_PROFILE_NAME, name.trim())
            .putString(KEY_PROFILE_ROLL, rollNo.trim())
            .putString(KEY_PROFILE_UNI, university.trim())
            .putString(KEY_PROFILE_SEM, semester.trim())
            .apply()
        _studentProfile.value = StudentProfile(
            name = name.trim().ifBlank { "Student" },
            rollNo = rollNo.trim(),
            university = university.trim(),
            semester = semester.trim()
        )
    }

    fun setSelectedModel(modelId: String) {
        val normalized = GeminiAdvisorService.normalizeApiModelId(modelId)
        prefs.edit().putString(KEY_SELECTED_MODEL, normalized).apply()
        _selectedModel.value = normalized
    }

    fun setAutoApplyAiActions(auto: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_APPLY_ACTIONS, auto).apply()
        _autoApplyAiActions.value = auto
    }

    private fun loadThemeMode(): ThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, ThemeMode.DARK.name) ?: ThemeMode.DARK.name
        return try {
            ThemeMode.valueOf(name)
        } catch (e: Exception) {
            ThemeMode.DARK
        }
    }

    private fun loadAccentTheme(): AccentTheme {
        val name = prefs.getString(KEY_ACCENT_THEME, AccentTheme.CYAN.name) ?: AccentTheme.CYAN.name
        return try {
            AccentTheme.valueOf(name)
        } catch (e: Exception) {
            AccentTheme.CYAN
        }
    }

    private fun loadStudentProfile(): StudentProfile {
        return StudentProfile(
            name = prefs.getString(KEY_PROFILE_NAME, "Student") ?: "Student",
            rollNo = prefs.getString(KEY_PROFILE_ROLL, "2026-CS-42") ?: "2026-CS-42",
            university = prefs.getString(KEY_PROFILE_UNI, "Universal Institute of Tech") ?: "Universal Institute of Tech",
            semester = prefs.getString(KEY_PROFILE_SEM, "Semester 6 • Computer Science") ?: "Semester 6 • Computer Science"
        )
    }

    companion object {
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_DYNAMIC_COLOR = "pref_dynamic_color"
        private const val KEY_ACCENT_THEME = "pref_accent_theme"
        private const val KEY_COMPACT_CARDS = "pref_compact_cards"
        private const val KEY_MAX_REFRESH_RATE = "pref_max_refresh_rate"
        private const val KEY_LOW_LATENCY_HAPTICS = "pref_low_latency_haptics"
        private const val KEY_GPU_HARDWARE_LAYERS = "pref_gpu_hardware_layers"
        private const val KEY_TARGET_PERCENTAGE = "pref_target_percentage"
        private const val KEY_WARNING_MARGIN = "pref_warning_margin"
        private const val KEY_STRICT_REGISTRATION = "pref_strict_registration"
        private const val KEY_INCLUDE_WEEKENDS = "pref_include_weekends"
        private const val KEY_PROFILE_NAME = "pref_profile_name"
        private const val KEY_PROFILE_ROLL = "pref_profile_roll"
        private const val KEY_PROFILE_UNI = "pref_profile_uni"
        private const val KEY_PROFILE_SEM = "pref_profile_sem"
        private const val KEY_SELECTED_MODEL = "pref_selected_model"
        private const val KEY_AUTO_APPLY_ACTIONS = "pref_auto_apply_actions"
    }
}
