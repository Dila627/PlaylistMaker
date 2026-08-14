package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.PlaylistsRepository
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.playlists.PlaylistsInteractor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistsInteractorImpl(
    private val repository: PlaylistsRepository
) : PlaylistsInteractor {

    override suspend fun createPlaylist(
        playlist: Playlist
    ): Long {
        return repository.createPlaylist(
            playlist
        )
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return repository.getPlaylists()
    }

    override fun getPlaylistById(
        playlistId: Long
    ): Flow<Playlist?> {

        return repository
            .getPlaylists()
            .map { playlists ->

                playlists.firstOrNull {
                    it.id == playlistId
                }
            }
    }

    override suspend fun addTrackToPlaylist(
        track: Track,
        playlist: Playlist
    ): Boolean {

        return repository.addTrackToPlaylist(
            track = track,
            playlist = playlist
        )
    }

    override suspend fun getPlaylistTracks(
        playlistId: Long
    ): List<Track> {

        return repository.getPlaylistTracks(
            playlistId
        )
    }

    override suspend fun deleteTrackFromPlaylist(
        playlistId: Long,
        trackId: Long
    ) {

        repository.deleteTrackFromPlaylist(
            playlistId = playlistId,
            trackId = trackId
        )
    }
    override suspend fun updatePlaylist(
        playlist: Playlist
    ) {
        repository.updatePlaylist(
            playlist
        )
    }
    override suspend fun deletePlaylist(
        playlistId: Long
    ) {

        repository.deletePlaylist(
            playlistId
        )
    }
}