package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages Gemini API Key storage and persistent preferences.
 * All Google Sign-In dependencies have been removed.
 */
class AuthManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("attendease_auth_prefs", Context.MODE_PRIVATE)

    private val _customApiKey = MutableStateFlow<String>(prefs.getString(KEY_CUSTOM_GEMINI_KEY, "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    fun setCustomGeminiApiKey(apiKey: String) {
        val clean = apiKey.trim()
        prefs.edit().putString(KEY_CUSTOM_GEMINI_KEY, clean).apply()
        _customApiKey.value = clean
    }

    fun getCustomGeminiApiKey(): String {
        return prefs.getString(KEY_CUSTOM_GEMINI_KEY, "") ?: ""
    }

    fun clearCustomGeminiApiKey() {
        prefs.edit().remove(KEY_CUSTOM_GEMINI_KEY).apply()
        _customApiKey.value = ""
    }

    companion object {
        private const val KEY_CUSTOM_GEMINI_KEY = "custom_gemini_api_key"
    }
}
