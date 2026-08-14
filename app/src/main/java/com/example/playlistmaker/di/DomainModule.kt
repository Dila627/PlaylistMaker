package com.example.playlistmaker.di

import com.example.playlistmaker.domain.impl.FavoriteTracksInteractorImpl
import com.example.playlistmaker.domain.impl.PlaylistsInteractorImpl
import com.example.playlistmaker.domain.impl.SearchHistoryInteractorImpl
import com.example.playlistmaker.domain.impl.SettingsInteractorImpl
import com.example.playlistmaker.domain.impl.TracksInteractorImpl
import com.example.playlistmaker.domain.playlists.PlaylistsInteractor
import com.example.playlistmaker.domain.search.FavoriteTracksInteractor
import com.example.playlistmaker.domain.search.SearchHistoryInteractor
import com.example.playlistmaker.domain.search.TracksInteractor
import com.example.playlistmaker.domain.settings.SettingsInteractor
import org.koin.dsl.module

val domainModule = module {

    // =========================================================
    // TRACKS
    // =========================================================

    single<TracksInteractor> {
        TracksInteractorImpl(
            get()
        )
    }

    // =========================================================
    // SEARCH HISTORY
    // =========================================================

    single<SearchHistoryInteractor> {
        SearchHistoryInteractorImpl(
            get()
        )
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    single<SettingsInteractor> {
        SettingsInteractorImpl(
            get()
        )
    }

    // =========================================================
    // FAVORITES
    // =========================================================

    single<FavoriteTracksInteractor> {
        FavoriteTracksInteractorImpl(
            repository = get()
        )
    }

    // =========================================================
    // PLAYLISTS
    // =========================================================

    single<PlaylistsInteractor> {
        PlaylistsInteractorImpl(
            repository = get()
        )
    }
}