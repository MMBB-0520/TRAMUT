package com.example.tramut.userInterface.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// 1. Define the available modes
enum class AppThemeMode {
    Light, Dark, System
}

// 2. Create the ViewModel to manage the state
class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    private val KEY_THEME = "theme_mode"

    // StateFlow to hold the current theme mode
    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode = _themeMode.asStateFlow()

    // Function to save the selected theme
    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.name).apply()
        _themeMode.value = mode
    }

    // Function to load the saved theme (Default to System)
    private fun loadThemeMode(): AppThemeMode {
        val savedName = prefs.getString(KEY_THEME, AppThemeMode.System.name)
        return try {
            AppThemeMode.valueOf(savedName ?: AppThemeMode.System.name)
        } catch (e: Exception) {
            AppThemeMode.System
        }
    }
}