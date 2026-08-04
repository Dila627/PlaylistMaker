package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.TracksRepository
import com.example.playlistmaker.domain.models.TrackSearchResult
import com.example.playlistmaker.domain.search.TracksInteractor
import kotlinx.coroutines.flow.Flow

class TracksInteractorImpl(
    private val repository: TracksRepository
) : TracksInteractor {

    override fun searchTracks(
        expression: String
    ): Flow<TrackSearchResult> {
        return repository.searchTracks(expression)
    }
}