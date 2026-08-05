package com.example.playlistmaker.domain.api

import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteTracksRepository {

    suspend fun addTrack(track: Track)

    suspend fun removeTrack(track: Track)

    suspend fun isFavorite(trackId: Long): Boolean

    fun getFavoriteTracks(): Flow<List<Track>>

    suspend fun getFavoriteTrackIds(): List<Long>
}