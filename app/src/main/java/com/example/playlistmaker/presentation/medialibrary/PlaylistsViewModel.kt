package com.example.playlistmaker.presentation.medialibrary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.playlists.PlaylistsInteractor
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private val playlistsLiveData = MutableLiveData<List<Playlist>>()

    fun observePlaylists(): LiveData<List<Playlist>> = playlistsLiveData

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            playlistsInteractor
                .getPlaylists()
                .collect { playlists ->
                    playlistsLiveData.value = playlists
                }
        }
    }
}