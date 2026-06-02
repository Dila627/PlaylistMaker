package com.example.playlistmaker

import android.app.Application
import com.example.playlistmaker.di.appModule
import com.example.playlistmaker.domain.settings.SettingsInteractor
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

class App : Application(), KoinComponent {

    private val settingsInteractor: SettingsInteractor by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(appModule)
        }

        settingsInteractor.updateThemeSetting(
            settingsInteractor.getThemeSettings()
        )
    }
}