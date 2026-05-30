package com.example.playlistmaker.presentation.search

import com.example.playlistmaker.domain.models.Track

sealed interface SearchState {

    data object Loading : SearchState

    data class Content(
        val tracks: List<Track>
    ) : SearchState

    data object Empty : SearchState

    data object Error : SearchState

    data class History(
        val tracks: List<Track>
    ) : SearchState
}