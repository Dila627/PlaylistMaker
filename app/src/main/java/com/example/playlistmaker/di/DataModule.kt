package com.example.playlistmaker.di

import android.content.Context
import android.media.MediaPlayer
import com.example.playlistmaker.data.network.ITunesApi
import com.example.playlistmaker.data.network.RetrofitClient
import com.example.playlistmaker.data.repository.FavoriteTracksRepositoryImpl
import com.example.playlistmaker.data.repository.PlaylistsRepositoryImpl
import com.example.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.data.repository.TracksRepositoryImpl
import com.example.playlistmaker.domain.api.FavoriteTracksRepository
import com.example.playlistmaker.domain.api.PlaylistsRepository
import com.example.playlistmaker.domain.api.SearchHistoryRepository
import com.example.playlistmaker.domain.api.SettingsRepository
import com.example.playlistmaker.domain.api.TracksRepository
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {

    // =========================================================
    // NETWORK
    // =========================================================

    single<ITunesApi> {
        RetrofitClient.itunesApi
    }

    // =========================================================
    // GSON
    // =========================================================

    single {
        Gson()
    }

    // =========================================================
    // SHARED PREFERENCES
    // =========================================================

    single {
        androidContext()
            .getSharedPreferences(
                "playlist_maker_prefs",
                Context.MODE_PRIVATE
            )
    }

    // =========================================================
    // TRACKS
    // =========================================================

    single<TracksRepository> {
        TracksRepositoryImpl(
            iTunesApi = get(),
            favoriteTrackDao = get()
        )
    }

    // =========================================================
    // SEARCH HISTORY
    // =========================================================

    single<SearchHistoryRepository> {
        SearchHistoryRepositoryImpl(
            sharedPreferences = get(),
            gson = get()
        )
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    single<SettingsRepository> {
        SettingsRepositoryImpl(
            get()
        )
    }

    // =========================================================
    // MEDIA PLAYER
    // =========================================================

    factory {
        MediaPlayer()
    }

    // =========================================================
    // FAVORITES
    // =========================================================

    single<FavoriteTracksRepository> {
        FavoriteTracksRepositoryImpl(
            favoriteTrackDao = get(),
            mapper = get()
        )
    }

    // =========================================================
    // PLAYLISTS
    // =========================================================

    single<PlaylistsRepository> {
        PlaylistsRepositoryImpl(
            playlistDao = get()
        )
    }
}