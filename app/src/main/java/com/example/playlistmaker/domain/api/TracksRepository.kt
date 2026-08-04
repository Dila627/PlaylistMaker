package com.example.playlistmaker.domain.api

import com.example.playlistmaker.domain.models.TrackSearchResult
import kotlinx.coroutines.flow.Flow

interface TracksRepository {

    fun searchTracks(expression: String): Flow<TrackSearchResult>
}