package com.example.playlistmaker.di

import android.content.Context
import android.media.MediaPlayer
import androidx.room.Room
import com.example.playlistmaker.data.db.AppDatabase
import com.example.playlistmaker.data.network.ITunesApi
import com.example.playlistmaker.data.network.RetrofitClient
import com.example.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.data.repository.TracksRepositoryImpl
import com.example.playlistmaker.domain.api.SearchHistoryRepository
import com.example.playlistmaker.domain.api.SettingsRepository
import com.example.playlistmaker.domain.api.TracksRepository
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import com.example.playlistmaker.data.repository.PlaylistsRepositoryImpl
import com.example.playlistmaker.domain.api.PlaylistsRepository

val dataModule = module {

    single<ITunesApi> {
        RetrofitClient.itunesApi
    }

    single {
        Gson()
    }

    single {
        androidContext().getSharedPreferences(
            "playlist_maker_prefs",
            Context.MODE_PRIVATE
        )
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "playlist_maker_database.db"
        ).build()
    }

    single {
        get<AppDatabase>().playlistDao()
    }

    single<TracksRepository> {
        TracksRepositoryImpl(get())
    }

    single<SearchHistoryRepository> {
        SearchHistoryRepositoryImpl(
            sharedPreferences = get(),
            gson = get()
        )
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(get())
    }

    factory {
        MediaPlayer()
    }

    single<PlaylistsRepository> {
        PlaylistsRepositoryImpl(
            playlistDao = get()
        )
    }
}