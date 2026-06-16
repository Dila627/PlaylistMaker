package com.example.playlistmaker.di

import com.example.playlistmaker.presentation.medialibrary.FavoriteTracksViewModel
import com.example.playlistmaker.presentation.medialibrary.MediaLibraryViewModel
import com.example.playlistmaker.presentation.medialibrary.PlaylistsViewModel
import com.example.playlistmaker.presentation.player.AudioPlayerViewModel
import com.example.playlistmaker.presentation.search.SearchViewModel
import com.example.playlistmaker.presentation.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        SearchViewModel(
            tracksInteractor = get(),
            searchHistoryInteractor = get()
        )
    }

    viewModel {
        SettingsViewModel(
            settingsInteractor = get()
        )
    }

    viewModel {
        AudioPlayerViewModel(
            mediaPlayer = get()
        )
    }

    viewModel {
        MediaLibraryViewModel()
    }

    viewModel {
        FavoriteTracksViewModel()
    }

    viewModel {
        PlaylistsViewModel()
    }
}