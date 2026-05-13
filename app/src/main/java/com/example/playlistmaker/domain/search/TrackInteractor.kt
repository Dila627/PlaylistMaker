package com.example.playlistmaker.domain.search

import com.example.playlistmaker.domain.models.TrackSearchResult

interface TracksInteractor {
    fun searchTracks(expression: String, consumer: (TrackSearchResult) -> Unit)
}