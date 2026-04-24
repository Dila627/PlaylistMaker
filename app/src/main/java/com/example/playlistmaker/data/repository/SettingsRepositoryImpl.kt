package com.example.playlistmaker.data.repository

import android.content.SharedPreferences
import com.example.playlistmaker.domain.api.SettingsRepository

class SettingsRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : SettingsRepository {

    override fun getThemeSettings(): Boolean {
        return sharedPreferences.getBoolean(THEME_KEY, false)
    }

    override fun updateThemeSetting(isDarkThemeEnabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(THEME_KEY, isDarkThemeEnabled)
            .apply()
    }

    companion object {
        private const val THEME_KEY = "dark_theme"
    }
}