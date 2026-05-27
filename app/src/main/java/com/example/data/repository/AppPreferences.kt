package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@JsonClass(generateAdapter = true)
data class AppSettings(
    // Appearance
    val themeMode: Int = 0, // 0 System, 1 Light, 2 Dark
    val blurIntensity: Float = 0.5f,
    val glowIntensity: Float = 0.5f,
    val glassTransparency: Float = 0.5f,
    val compactMode: Boolean = false,
    val enableAmbientGradient: Boolean = true,
    val scaleCornerRadius: Float = 1f,

    // Chat Behavior
    val autoScroll: Boolean = true,
    val sendOnEnter: Boolean = false,
    val renderMarkdown: Boolean = true,
    val renderHtml: Boolean = false,
    val groupMessages: Boolean = true,
    val showTimestamps: Boolean = true,
    val autoFollowStream: Boolean = true,
    
    // Agent / Search Behavior
    val defaultMode: String = "CHAT",
    val deepResearchEnabled: Boolean = false,
    val maxSources: Int = 5,
    val citationDensity: String = "NORMAL",
    val autoFailover: Boolean = true,

    // Diagnostics
    val diagnosticsMode: Boolean = false,
    val networkLogging: Boolean = false
)

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val jsonAdapter = moshi.adapter(AppSettings::class.java)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val str = prefs.getString("settings_json", null)
        return try {
            if (str != null) jsonAdapter.fromJson(str) ?: AppSettings() else AppSettings()
        } catch (e: Exception) {
            AppSettings()
        }
    }

    private fun saveSettings(s: AppSettings) {
        val jsonStr = jsonAdapter.toJson(s)
        prefs.edit().putString("settings_json", jsonStr).apply()
        _settings.value = s
    }

    fun updateSettings(updater: (AppSettings) -> AppSettings) {
        saveSettings(updater(_settings.value))
    }
    
    fun resetSettings() {
        saveSettings(AppSettings())
    }
}

