package com.example.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    private val prefsName = "playlist_maker_prefs"
    private val keyDarkTheme = "dark_theme"

    var darkTheme = false
        private set

    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        darkTheme = prefs.getBoolean(keyDarkTheme, false)
        applyTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled

        getSharedPreferences(prefsName, MODE_PRIVATE)
            .edit()
            .putBoolean(keyDarkTheme, darkThemeEnabled)
            .apply()

        applyTheme(darkThemeEnabled)
    }

    private fun applyTheme(darkThemeEnabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}