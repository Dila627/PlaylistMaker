package com.example.playlistmaker.domain.api

interface SettingsRepository {
    fun getThemeSettings(): Boolean
    fun updateThemeSetting(isDarkThemeEnabled: Boolean)
}