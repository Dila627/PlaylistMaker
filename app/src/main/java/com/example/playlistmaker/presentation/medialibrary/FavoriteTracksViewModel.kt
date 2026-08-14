package com.example.playlistmaker.presentation.medialibrary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.search.FavoriteTracksInteractor
import kotlinx.coroutines.launch

class FavoriteTracksViewModel(
    private val favoriteTracksInteractor: FavoriteTracksInteractor
) : ViewModel() {

    private val stateLiveData = MutableLiveData<FavoriteTracksState>()

    fun observeState(): LiveData<FavoriteTracksState> = stateLiveData

    init {
        loadFavoriteTracks()
    }

    private fun loadFavoriteTracks() {
        viewModelScope.launch {
            favoriteTracksInteractor
                .getFavoriteTracks()
                .collect { tracks ->
                    stateLiveData.value =
                        if (tracks.isEmpty()) {
                            FavoriteTracksState.Empty
                        } else {
                            FavoriteTracksState.Content(tracks)
                        }
                }
        }
    }
}