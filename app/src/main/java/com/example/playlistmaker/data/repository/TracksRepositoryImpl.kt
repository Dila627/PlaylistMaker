package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.dto.SearchResponse
import com.example.playlistmaker.data.network.RetrofitClient
import com.example.playlistmaker.domain.api.TracksRepository
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.models.TrackSearchResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class TracksRepositoryImpl : TracksRepository {

    override fun searchTracks(expression: String, consumer: (TrackSearchResult) -> Unit) {
        RetrofitClient.itunesApi.search(expression).enqueue(object : Callback<SearchResponse> {

            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                if (response.isSuccessful) {
                    val tracks = response.body()?.results.orEmpty().map { dto ->
                        Track(
                            trackId = dto.trackId ?: 0L,
                            trackName = dto.trackName ?: "",
                            artistName = dto.artistName ?: "",
                            trackTime = SimpleDateFormat("mm:ss", Locale.getDefault())
                                .format(dto.trackTimeMillis ?: 0L),
                            artworkUrl100 = dto.artworkUrl100 ?: "",
                            collectionName = dto.collectionName,
                            releaseDate = dto.releaseDate,
                            primaryGenreName = dto.primaryGenreName,
                            country = dto.country,
                            previewUrl = dto.previewUrl
                        )
                    }
                    consumer(TrackSearchResult(tracks, false))
                } else {
                    consumer(TrackSearchResult(emptyList(), true))
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                consumer(TrackSearchResult(emptyList(), true))
            }
        })
    }
}