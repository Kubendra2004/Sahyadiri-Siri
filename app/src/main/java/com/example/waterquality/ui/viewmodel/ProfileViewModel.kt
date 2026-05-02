package com.example.waterquality.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

enum class ThemeMode { LIGHT, DARK, SYSTEM }

data class ProfileStats(
    val username: String  = "Sahyadri User",
    val joinedDays: Int   = 14,
    val totalSubmitted: Int = 12,
    val streak: Int       = 5,
    val badgeCount: Int   = 3
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("sahyadri_prefs", Context.MODE_PRIVATE)

    // ── Theme mode ─────────────────────────────────────────────────────────────
    private val _themeMode = MutableStateFlow(
        ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    // ── Notifications ─────────────────────────────────────────────────────────
    private val _notificationsEnabled = MutableStateFlow(
        prefs.getBoolean("notifications", true)
    )
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun toggleNotifications() {
        val newValue = !_notificationsEnabled.value
        _notificationsEnabled.value = newValue
        prefs.edit().putBoolean("notifications", newValue).apply()
    }

    // ── Language ──────────────────────────────────────────────────────────────
    private val _selectedLanguage = MutableStateFlow(
        prefs.getString("language", "English") ?: "English"
    )
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    fun setLanguage(language: String) {
        _selectedLanguage.value = language
        prefs.edit().putString("language", language).apply()
    }

    // ── Profile stats (mock — replace with Room/API later) ────────────────────
    private val _profileStats = MutableStateFlow(ProfileStats())
    val profileStats: StateFlow<ProfileStats> = _profileStats.asStateFlow()
}
