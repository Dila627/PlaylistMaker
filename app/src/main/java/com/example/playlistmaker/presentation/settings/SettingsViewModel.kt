package com.example.playlistmaker.presentation.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.settings.SettingsInteractor

class SettingsViewModel(
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    private val darkThemeLiveData = MutableLiveData<Boolean>()

    fun observeTheme(): LiveData<Boolean> = darkThemeLiveData

    fun loadTheme() {
        darkThemeLiveData.value = settingsInteractor.getThemeSettings()
    }

    fun switchTheme(isDarkTheme: Boolean) {
        settingsInteractor.updateThemeSetting(isDarkTheme)
        darkThemeLiveData.value = isDarkTheme
    }
}