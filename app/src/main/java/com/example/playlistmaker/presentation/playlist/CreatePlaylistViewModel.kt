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

    private val playlistLiveData =
        MutableLiveData<Playlist?>()

    fun observePlaylist(): LiveData<Playlist?> =
        playlistLiveData

    // =========================================================
    // ЗАГРУЗКА ПЛЕЙЛИСТА ДЛЯ РЕДАКТИРОВАНИЯ
    // =========================================================

    fun loadPlaylist(
        playlistId: Long
    ) {

        if (playlistId < 0) {
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
    // СОЗДАНИЕ НОВОГО ПЛЕЙЛИСТА
    // =========================================================

    fun createPlaylist(
        name: String,
        description: String,
        imagePath: String?,
        trackToAdd: Track? = null,
        onCreated: (() -> Unit)? = null
    ) {

        if (name.isBlank()) {
            return
        }

        viewModelScope.launch {

            // Создаём новый пустой плейлист
            val playlist =
                Playlist(
                    id = 0,
                    name = name.trim(),
                    description =
                        description
                            .trim()
                            .ifBlank { null },
                    imagePath =
                        imagePath,
                    trackIds =
                        emptyList(),
                    tracksCount =
                        0
                )

            // Room возвращает настоящий ID
            // созданного плейлиста
            val newPlaylistId =
                playlistsInteractor
                    .createPlaylist(
                        playlist
                    )

            // Создаём объект уже
            // с настоящим ID
            val createdPlaylist =
                playlist.copy(
                    id = newPlaylistId
                )

            // =================================================
            // ЕСЛИ ПРИШЛИ ИЗ AUDIO PLAYER
            // =================================================

            if (trackToAdd != null) {

                playlistsInteractor
                    .addTrackToPlaylist(
                        track = trackToAdd,
                        playlist = createdPlaylist
                    )
            }

            onCreated?.invoke()
        }
    }

    fun updatePlaylist(
        playlistId: Long,
        name: String,
        description: String,
        imagePath: String?,
        onUpdated: (() -> Unit)? = null
    ) {

        if (
            playlistId < 0 ||
            name.isBlank()
        ) {
            return
        }

        viewModelScope.launch {

            // Получаем существующий плейлист
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
                        name.trim(),

                    description =
                        description
                            .trim()
                            .ifBlank { null },

                    imagePath =
                        imagePath
                )

            playlistsInteractor
                .updatePlaylist(
                    updatedPlaylist
                )

            onUpdated?.invoke()
        }
    }
}