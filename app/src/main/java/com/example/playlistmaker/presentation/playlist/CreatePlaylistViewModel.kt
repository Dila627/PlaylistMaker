package com.example.playlistmaker.presentation.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.playlists.PlaylistsInteractor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CreatePlaylistViewModel(
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
    // SAVE PROTECTION
    // =========================================================

    private var operationInProgress =
        false

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

        viewModelScope.launch {

            val playlist =
                playlistsInteractor
                    .getPlaylistById(
                        playlistId
                    )
                    .first()

            playlistLiveData.value =
                playlist
        }
    }

    // =========================================================
    // CHECK DUPLICATE NAME
    // =========================================================

    private suspend fun isPlaylistNameAlreadyUsed(
        name: String,
        excludedPlaylistId: Long? = null
    ): Boolean {

        val normalizedName =
            name.trim()

        val playlists =
            playlistsInteractor
                .getPlaylists()
                .first()

        return playlists.any { playlist ->

            val isAnotherPlaylist =
                excludedPlaylistId == null ||
                        playlist.id !=
                        excludedPlaylistId

            val hasSameName =
                playlist.name
                    .trim()
                    .equals(
                        normalizedName,
                        ignoreCase = true
                    )

            isAnotherPlaylist &&
                    hasSameName
        }
    }

    // =========================================================
    // CREATE
    // =========================================================

    fun createPlaylist(
        name: String,
        description: String,
        imagePath: String?,
        trackToAdd: Track? = null,
        onNameExists: (() -> Unit)? = null,
        onCreated: (() -> Unit)? = null
    ) {

        val normalizedName =
            name.trim()

        if (
            normalizedName.isBlank() ||
            operationInProgress
        ) {
            return
        }

        viewModelScope.launch {

            operationInProgress =
                true

            try {

                // =============================================
                // DUPLICATE NAME
                // =============================================

                val nameAlreadyUsed =
                    isPlaylistNameAlreadyUsed(
                        normalizedName
                    )

                if (
                    nameAlreadyUsed
                ) {

                    onNameExists?.invoke()

                    return@launch
                }

                // =============================================
                // CREATE PLAYLIST
                // =============================================

                val playlist =
                    Playlist(
                        id = 0,
                        name = normalizedName,
                        description =
                            description
                                .trim()
                                .ifBlank {
                                    null
                                },
                        imagePath =
                            imagePath,
                        trackIds =
                            emptyList(),
                        tracksCount =
                            0
                    )

                val newPlaylistId =
                    playlistsInteractor
                        .createPlaylist(
                            playlist
                        )

                if (
                    newPlaylistId <=
                    0
                ) {
                    return@launch
                }

                val createdPlaylist =
                    playlist.copy(
                        id =
                            newPlaylistId
                    )

                // =============================================
                // ADD TRACK IF CREATED FROM PLAYER
                // =============================================

                if (
                    trackToAdd !=
                    null
                ) {

                    playlistsInteractor
                        .addTrackToPlaylist(
                            track =
                                trackToAdd,
                            playlist =
                                createdPlaylist
                        )
                }

                onCreated?.invoke()

            } finally {

                operationInProgress =
                    false
            }
        }
    }

    // =========================================================
    // UPDATE
    // =========================================================

    fun updatePlaylist(
        playlistId: Long,
        name: String,
        description: String,
        imagePath: String?,
        onNameExists: (() -> Unit)? = null,
        onUpdated: (() -> Unit)? = null
    ) {

        val normalizedName =
            name.trim()

        if (
            playlistId <
            0 ||
            normalizedName.isBlank() ||
            operationInProgress
        ) {
            return
        }

        viewModelScope.launch {

            operationInProgress =
                true

            try {

                // =============================================
                // DUPLICATE NAME
                // =============================================

                /*
                 * Свой собственный ID исключаем.
                 *
                 * То есть можно нажать "Сохранить",
                 * не меняя название текущего плейлиста.
                 *
                 * Но название другого плейлиста
                 * использовать нельзя.
                 */
                val nameAlreadyUsed =
                    isPlaylistNameAlreadyUsed(
                        name =
                            normalizedName,
                        excludedPlaylistId =
                            playlistId
                    )

                if (
                    nameAlreadyUsed
                ) {

                    onNameExists?.invoke()

                    return@launch
                }

                val currentPlaylist =
                    playlistsInteractor
                        .getPlaylistById(
                            playlistId
                        )
                        .first()
                        ?: return@launch

                val updatedPlaylist =
                    currentPlaylist.copy(

                        name =
                            normalizedName,

                        description =
                            description
                                .trim()
                                .ifBlank {
                                    null
                                },

                        imagePath =
                            imagePath
                    )

                playlistsInteractor
                    .updatePlaylist(
                        updatedPlaylist
                    )

                onUpdated?.invoke()

            } finally {

                operationInProgress =
                    false
            }
        }
    }
}