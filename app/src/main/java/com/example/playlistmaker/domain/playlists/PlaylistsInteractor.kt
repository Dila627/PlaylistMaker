package com.example.playlistmaker.domain.playlists

import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistsInteractor {

    suspend fun createPlaylist(
        playlist: Playlist
    ): Long

    fun getPlaylists(): Flow<List<Playlist>>

    fun getPlaylistById(
        playlistId: Long
    ): Flow<Playlist?>

    suspend fun addTrackToPlaylist(
        track: Track,
        playlist: Playlist
    ): Boolean

    suspend fun getPlaylistTracks(
        playlistId: Long
    ): List<Track>

    suspend fun deleteTrackFromPlaylist(
        playlistId: Long,
        trackId: Long
    )

    suspend fun deletePlaylist(
        playlistId: Long
    )
    suspend fun updatePlaylist(
        playlist: Playlist
    )
}