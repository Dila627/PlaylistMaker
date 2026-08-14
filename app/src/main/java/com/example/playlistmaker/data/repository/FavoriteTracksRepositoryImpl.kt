package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.db.FavoriteTrackDao
import com.example.playlistmaker.data.mapper.TrackDbMapper
import com.example.playlistmaker.domain.api.FavoriteTracksRepository
import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class FavoriteTracksRepositoryImpl(
    private val favoriteTrackDao: FavoriteTrackDao,
    private val mapper: TrackDbMapper
) : FavoriteTracksRepository {

    override suspend fun addTrack(
        track: Track
    ) {
        favoriteTrackDao.insertTrack(
            mapper.map(
                track = track,
                addedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun removeTrack(
        track: Track
    ) {
        favoriteTrackDao.deleteTrack(
            mapper.map(
                track = track,
                addedAt = 0L
            )
        )
    }

    override suspend fun isFavorite(
        trackId: Long
    ): Boolean {
        return favoriteTrackDao.isFavorite(
            trackId
        )
    }

    override fun getFavoriteTracks():
            Flow<List<Track>> {

        return favoriteTrackDao
            .getFavoriteTracks()
            .distinctUntilChanged()
            .map { entities ->

                entities.map { entity ->
                    mapper.map(entity)
                }
            }
    }

    override suspend fun getFavoriteTrackIds():
            List<Long> {

        return favoriteTrackDao
            .getFavoriteTrackIds()
    }
}