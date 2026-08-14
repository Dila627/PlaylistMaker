package com.example.playlistmaker.di

import com.example.playlistmaker.domain.impl.SearchHistoryInteractorImpl
import com.example.playlistmaker.domain.impl.SettingsInteractorImpl
import com.example.playlistmaker.domain.impl.TracksInteractorImpl
import com.example.playlistmaker.domain.search.SearchHistoryInteractor
import com.example.playlistmaker.domain.search.TracksInteractor
import com.example.playlistmaker.domain.settings.SettingsInteractor
import org.koin.dsl.module
import com.example.playlistmaker.domain.impl.PlaylistsInteractorImpl
import com.example.playlistmaker.domain.playlists.PlaylistsInteractor

val domainModule = module {

    single<TracksInteractor> {
        TracksInteractorImpl(get())
    }

    single<SearchHistoryInteractor> {
        SearchHistoryInteractorImpl(get())
    }

    single<SettingsInteractor> {
        SettingsInteractorImpl(get())
    }
    single<PlaylistsInteractor> {
        PlaylistsInteractorImpl(
            repository = get()
        )
    }
}