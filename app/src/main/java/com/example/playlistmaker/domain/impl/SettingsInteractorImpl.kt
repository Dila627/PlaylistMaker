package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.SettingsRepository
import com.example.playlistmaker.domain.settings.SettingsInteractor

class SettingsInteractorImpl(
    private val repository: SettingsRepository
) : SettingsInteractor {

    override fun getThemeSettings(): Boolean {
        return repository.getThemeSettings()
    }

    override fun updateThemeSetting(isDarkThemeEnabled: Boolean) {
        repository.updateThemeSetting(isDarkThemeEnabled)
    }
}