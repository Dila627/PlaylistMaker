package com.example.playlistmaker.domain.models

data class TrackSearchResult(
    val tracks: List<Track>,
    val isError: Boolean
)