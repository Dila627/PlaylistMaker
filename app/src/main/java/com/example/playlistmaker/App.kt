package com.example.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.di.appModule
import com.example.playlistmaker.domain.settings.SettingsInteractor
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

class App : Application(), KoinComponent {

    private var darkTheme = false

    private val settingsInteractor: SettingsInteractor by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(appModule)
        }

        darkTheme = settingsInteractor.getThemeSettings()
        applyTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        settingsInteractor.updateThemeSetting(darkThemeEnabled)
        applyTheme(darkThemeEnabled)
    }

    fun isDarkThemeEnabled(): Boolean {
        return darkTheme
    }

    private fun applyTheme(darkThemeEnabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}