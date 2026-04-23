package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.TracksRepository
import com.example.playlistmaker.domain.models.TrackSearchResult
import com.example.playlistmaker.domain.search.TracksInteractor

class TracksInteractorImpl(
    private val repository: TracksRepository
) : TracksInteractor {

    override fun searchTracks(expression: String, consumer: (TrackSearchResult) -> Unit) {
        repository.searchTracks(expression, consumer)
    }
}