package com.example.playlistmaker.presentation.player

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentTime: String = "00:00"
)