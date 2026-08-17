package com.example.playlistmaker.presentation.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.playlists.PlaylistsInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private val playlistLiveData =
        MutableLiveData<Playlist?>()

    fun observePlaylist(): LiveData<Playlist?> =
        playlistLiveData

    private val tracksLiveData =
        MutableLiveData<List<Track>>(emptyList())

    fun observeTracks(): LiveData<List<Track>> =
        tracksLiveData


    private val playlistDeletedLiveData =
        MutableLiveData(false)

    fun observePlaylistDeleted(): LiveData<Boolean> =
        playlistDeletedLiveData

    private var playlistJob: Job? = null

    private var currentPlaylistId: Long =
        INVALID_PLAYLIST_ID

    fun loadPlaylist(
        playlistId: Long
    ) {

        if (playlistId < 0) {
            return
        }

        currentPlaylistId =
            playlistId

        playlistJob?.cancel()

        playlistJob =
            viewModelScope.launch {

                playlistsInteractor
                    .getPlaylistById(
                        playlistId
                    )
                    .collect { playlist ->

                        playlistLiveData.value =
                            playlist
                    }
            }

        loadTracks(
            playlistId
        )
    }

    private fun loadTracks(
        playlistId: Long
    ) {

        if (playlistId < 0) {
            return
        }

        viewModelScope.launch {

            val tracks =
                playlistsInteractor
                    .getPlaylistTracks(
                        playlistId
                    )

            tracksLiveData.value =
                tracks
        }
    }


    fun deleteTrack(
        track: Track
    ) {

        if (currentPlaylistId < 0) {
            return
        }

        viewModelScope.launch {

            playlistsInteractor
                .deleteTrackFromPlaylist(
                    playlistId =
                        currentPlaylistId,
                    trackId =
                        track.trackId
                )


            loadTracks(
                currentPlaylistId
            )
        }
    }


    fun deletePlaylist() {

        if (currentPlaylistId < 0) {
            return
        }

        viewModelScope.launch {

            playlistsInteractor
                .deletePlaylist(
                    currentPlaylistId
                )

            playlistDeletedLiveData.value =
                true
        }
    }


    fun playlistDeletedHandled() {
        playlistDeletedLiveData.value =
            false
    }

    override fun onCleared() {
        playlistJob?.cancel()
        super.onCleared()
    }

    companion object {

        private const val INVALID_PLAYLIST_ID =
            -1L
    }
}