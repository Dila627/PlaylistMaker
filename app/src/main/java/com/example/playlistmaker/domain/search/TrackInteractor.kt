package com.example.playlistmaker.domain.search

import com.example.playlistmaker.domain.models.TrackSearchResult
import kotlinx.coroutines.flow.Flow

interface TracksInteractor {

    fun searchTracks(expression: String): Flow<TrackSearchResult>
}