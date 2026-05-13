package com.example.playlistmaker.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.domain.settings.SettingsInteractor

class SettingsViewModelFactory(
    private val settingsInteractor: SettingsInteractor
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(settingsInteractor) as T
    }
}