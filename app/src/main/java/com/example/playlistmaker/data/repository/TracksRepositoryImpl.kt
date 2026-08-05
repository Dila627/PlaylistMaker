package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.db.FavoriteTrackDao
import com.example.playlistmaker.data.dto.TrackDto
import com.example.playlistmaker.data.network.ITunesApi
import com.example.playlistmaker.domain.api.TracksRepository
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.models.TrackSearchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class TracksRepositoryImpl(
    private val iTunesApi: ITunesApi,
    private val favoriteTrackDao: FavoriteTrackDao
) : TracksRepository {

    override fun searchTracks(
        expression: String
    ): Flow<TrackSearchResult> {
        return flow {
            val response = iTunesApi.search(expression)
            val favoriteTrackIds = favoriteTrackDao.getFavoriteTrackIds()

            val tracks = response.results.orEmpty().map { dto ->
                mapToDomain(
                    dto = dto,
                    favoriteTrackIds = favoriteTrackIds
                )
            }

            emit(
                TrackSearchResult(
                    tracks = tracks,
                    isError = false
                )
            )
        }.catch {
            emit(
                TrackSearchResult(
                    tracks = emptyList(),
                    isError = true
                )
            )
        }
    }

    private fun mapToDomain(
        dto: TrackDto,
        favoriteTrackIds: List<Long>
    ): Track {
        val trackId = dto.trackId ?: 0L

        return Track(
            trackId = trackId,
            trackName = dto.trackName.orEmpty(),
            artistName = dto.artistName.orEmpty(),
            trackTimeMillis = dto.trackTimeMillis ?: 0L,
            artworkUrl100 = dto.artworkUrl100.orEmpty(),
            collectionName = dto.collectionName,
            releaseDate = dto.releaseDate,
            primaryGenreName = dto.primaryGenreName,
            country = dto.country,
            previewUrl = dto.previewUrl,
            isFavorite = trackId in favoriteTrackIds
        )
    }
}