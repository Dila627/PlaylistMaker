package com.example.playlistmaker.presentation.medialibrary

import com.example.playlistmaker.domain.models.Track

sealed interface FavoriteTracksState {

    data class Content(
        val tracks: List<Track>
    ) : FavoriteTracksState

    data object Empty : FavoriteTracksState
}