package com.example.playlistmaker.domain.models

data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String?,
    val imagePath: String?,
    val trackIds: List<Long>,
    val tracksCount: Int
)