package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.FavoriteTracksRepository
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.search.FavoriteTracksInteractor
import kotlinx.coroutines.flow.Flow

class FavoriteTracksInteractorImpl(
    private val repository: FavoriteTracksRepository
) : FavoriteTracksInteractor {

    override suspend fun addTrack(track: Track) {
        repository.addTrack(track)
    }

    override suspend fun removeTrack(track: Track) {
        repository.removeTrack(track)
    }

    override suspend fun isFavorite(trackId: Long): Boolean {
        return repository.isFavorite(trackId)
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return repository.getFavoriteTracks()
    }

    override suspend fun getFavoriteTrackIds(): List<Long> {
        return repository.getFavoriteTrackIds()
    }
}