package com.example.playlistmaker

import android.content.Context
import com.example.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.repository.TracksRepositoryImpl
import com.example.playlistmaker.domain.api.SearchHistoryRepository
import com.example.playlistmaker.domain.api.TracksRepository
import com.example.playlistmaker.domain.impl.SearchHistoryInteractorImpl
import com.example.playlistmaker.domain.impl.TracksInteractorImpl
import com.example.playlistmaker.domain.search.SearchHistoryInteractor
import com.example.playlistmaker.domain.search.TracksInteractor

object Creator {

    private fun provideTracksRepository(): TracksRepository {
        return TracksRepositoryImpl()
    }

    fun provideTracksInteractor(): TracksInteractor {
        return TracksInteractorImpl(provideTracksRepository())
    }

    private fun provideSearchHistoryRepository(context: Context): SearchHistoryRepository {
        val prefs = context.getSharedPreferences("playlist_maker_prefs", Context.MODE_PRIVATE)
        return SearchHistoryRepositoryImpl(prefs)
    }

    fun provideSearchHistoryInteractor(context: Context): SearchHistoryInteractor {
        return SearchHistoryInteractorImpl(provideSearchHistoryRepository(context))
    }
}