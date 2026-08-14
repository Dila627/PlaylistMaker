package com.example.playlistmaker.domain.search

import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteTracksInteractor {

    suspend fun addTrack(track: Track)

    suspend fun removeTrack(track: Track)

    suspend fun isFavorite(trackId: Long): Boolean

    fun getFavoriteTracks(): Flow<List<Track>>

    suspend fun getFavoriteTrackIds(): List<Long>
}