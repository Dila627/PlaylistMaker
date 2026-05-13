package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.dto.SearchResponse
import com.example.playlistmaker.data.dto.TrackDto
import com.example.playlistmaker.data.network.ITunesApi
import com.example.playlistmaker.domain.api.TracksRepository
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.models.TrackSearchResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class TracksRepositoryImpl(
    private val iTunesApi: ITunesApi
) : TracksRepository {

    private val timeFormatter = SimpleDateFormat("mm:ss", Locale.getDefault())

    override fun searchTracks(expression: String, consumer: (TrackSearchResult) -> Unit) {
        iTunesApi.search(expression).enqueue(object : Callback<SearchResponse> {

            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {
                if (response.isSuccessful) {
                    val tracks = response.body()?.results.orEmpty().map { dto ->
                        mapToDomain(dto)
                    }
                    consumer(TrackSearchResult(tracks = tracks, isError = false))
                } else {
                    consumer(TrackSearchResult(tracks = emptyList(), isError = true))
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                consumer(TrackSearchResult(tracks = emptyList(), isError = true))
            }
        })
    }

    private fun mapToDomain(dto: TrackDto): Track {
        return Track(
            trackId = dto.trackId ?: 0L,
            trackName = dto.trackName ?: "",
            artistName = dto.artistName ?: "",
            trackTime = timeFormatter.format(dto.trackTimeMillis ?: 0L),
            artworkUrl100 = dto.artworkUrl100 ?: "",
            collectionName = dto.collectionName,
            releaseDate = dto.releaseDate,
            primaryGenreName = dto.primaryGenreName,
            country = dto.country,
            previewUrl = dto.previewUrl
        )
    }
}