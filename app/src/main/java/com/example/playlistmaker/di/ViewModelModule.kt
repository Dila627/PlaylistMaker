package com.example.playlistmaker.di

import com.example.playlistmaker.presentation.medialibrary.FavoriteTracksViewModel
import com.example.playlistmaker.presentation.medialibrary.MediaLibraryViewModel
import com.example.playlistmaker.presentation.medialibrary.PlaylistsViewModel
import com.example.playlistmaker.presentation.player.AudioPlayerViewModel
import com.example.playlistmaker.presentation.playlist.CreatePlaylistViewModel
import com.example.playlistmaker.presentation.playlist.PlaylistViewModel
import com.example.playlistmaker.presentation.search.SearchViewModel
import com.example.playlistmaker.presentation.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    // =========================================================
    // SEARCH
    // =========================================================

    viewModel {
        SearchViewModel(
            tracksInteractor = get(),
            searchHistoryInteractor = get()
        )
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    viewModel {
        SettingsViewModel(
            settingsInteractor = get()
        )
    }

    // =========================================================
    // AUDIO PLAYER
    // =========================================================

    viewModel {
        AudioPlayerViewModel(
            mediaPlayer = get(),
            favoriteTracksInteractor = get(),
            playlistsInteractor = get()
        )
    }

    // =========================================================
    // MEDIA LIBRARY
    // =========================================================

    viewModel {
        MediaLibraryViewModel()
    }

    // =========================================================
    // FAVORITES
    // =========================================================

    viewModel {
        FavoriteTracksViewModel(
            favoriteTracksInteractor = get()
        )
    }

    // =========================================================
    // PLAYLISTS
    // =========================================================

    viewModel {
        PlaylistsViewModel(
            playlistsInteractor = get()
        )
    }

    // =========================================================
    // CREATE / EDIT PLAYLIST
    // =========================================================

    viewModel {
        CreatePlaylistViewModel(
            playlistsInteractor = get()
        )
    }

    // =========================================================
    // PLAYLIST DETAILS
    // =========================================================

    viewModel {
        PlaylistViewModel(
            playlistsInteractor = get()
        )
    }
}