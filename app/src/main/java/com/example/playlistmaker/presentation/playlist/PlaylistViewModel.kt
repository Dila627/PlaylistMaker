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

    // =========================================================
    // PLAYLIST
    // =========================================================

    private val playlistLiveData =
        MutableLiveData<Playlist?>()

    fun observePlaylist():
            LiveData<Playlist?> =
        playlistLiveData

    // =========================================================
    // TRACKS
    // =========================================================

    private val tracksLiveData =
        MutableLiveData<List<Track>>(
            emptyList()
        )

    fun observeTracks():
            LiveData<List<Track>> =
        tracksLiveData

    // =========================================================
    // DELETE PLAYLIST EVENT
    // =========================================================

    private val playlistDeletedLiveData =
        MutableLiveData(false)

    fun observePlaylistDeleted():
            LiveData<Boolean> =
        playlistDeletedLiveData

    // =========================================================
    // DATA
    // =========================================================

    private var playlistJob:
            Job? = null

    private var currentPlaylistId =
        INVALID_PLAYLIST_ID

    // =========================================================
    // LOAD PLAYLIST
    // =========================================================

    fun loadPlaylist(
        playlistId: Long
    ) {

        if (
            playlistId <
            0
        ) {
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

        viewModelScope.launch {

            refreshTracks(
                playlistId
            )
        }
    }

    // =========================================================
    // REFRESH TRACKS
    // =========================================================

    /*
     * Теперь этот метод НЕ запускает
     * дополнительную coroutine.
     *
     * Вызывающий код ждёт получения
     * нового списка треков.
     */
    private suspend fun refreshTracks(
        playlistId: Long
    ) {

        if (
            playlistId <
            0
        ) {
            return
        }

        val tracks =
            playlistsInteractor
                .getPlaylistTracks(
                    playlistId
                )

        tracksLiveData.value =
            tracks
    }

    // =========================================================
    // DELETE TRACK
    // =========================================================

    fun deleteTrack(
        track: Track
    ) {

        val playlistId =
            currentPlaylistId

        if (
            playlistId <
            0
        ) {
            return
        }

        viewModelScope.launch {

            // Сначала реально удаляем из Room.
            playlistsInteractor
                .deleteTrackFromPlaylist(
                    playlistId = playlistId,
                    trackId = track.trackId
                )

            // И только ПОСЛЕ завершения удаления
            // снова читаем список из Room.
            refreshTracks(
                playlistId
            )
        }
    }

    // =========================================================
    // DELETE PLAYLIST
    // =========================================================

    fun deletePlaylist() {

        val playlistId =
            currentPlaylistId

        if (
            playlistId <
            0
        ) {
            return
        }

        viewModelScope.launch {

            playlistsInteractor
                .deletePlaylist(
                    playlistId
                )

            playlistDeletedLiveData.value =
                true
        }
    }

    // =========================================================
    // DELETE EVENT HANDLED
    // =========================================================

    fun playlistDeletedHandled() {

        playlistDeletedLiveData.value =
            false
    }

    // =========================================================
    // CLEAR
    // =========================================================

    override fun onCleared() {

        playlistJob?.cancel()

        super.onCleared()
    }

    companion object {

        private const val INVALID_PLAYLIST_ID =
            -1L
    }
}