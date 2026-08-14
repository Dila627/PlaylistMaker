package com.example.playlistmaker.di

import androidx.room.Room
import com.example.playlistmaker.data.db.AppDatabase
import org.koin.dsl.module

val databaseModule = module {

    single {
        Room.databaseBuilder(
            get(),
            AppDatabase::class.java,
            "playlist_database.db"
        ).build()
    }

    single {
        get<AppDatabase>().favoriteTrackDao()
    }
}