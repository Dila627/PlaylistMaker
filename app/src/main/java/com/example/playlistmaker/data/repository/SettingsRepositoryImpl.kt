package com.example.playlistmaker.data.repository

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
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

        AppCompatDelegate.setDefaultNightMode(
            if (isDarkThemeEnabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    companion object {
        private const val THEME_KEY = "dark_theme"
    }
}