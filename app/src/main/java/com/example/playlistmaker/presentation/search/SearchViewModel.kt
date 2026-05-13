package com.example.playlistmaker.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.search.SearchHistoryInteractor
import com.example.playlistmaker.domain.search.TracksInteractor


class SearchViewModel(
    private val tracksInteractor: TracksInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor
) : ViewModel() {

    private val stateLiveData = MutableLiveData<SearchState>()

    fun observeState(): LiveData<SearchState> = stateLiveData

    fun search(text: String) {
        if (text.isBlank()) return

        stateLiveData.postValue(SearchState.Loading)

        tracksInteractor.searchTracks(text) { result ->
            when {
                result.isError -> {
                    stateLiveData.postValue(SearchState.Error)
                }

                result.tracks.isEmpty() -> {
                    stateLiveData.postValue(SearchState.Empty)
                }

                else -> {
                    stateLiveData.postValue(SearchState.Content(result.tracks))
                }
            }
        }
    }

    fun showHistory() {
        val history = searchHistoryInteractor.getHistory()

        if (history.isNotEmpty()) {
            stateLiveData.value = SearchState.History(history)
        }
    }

    fun addTrackToHistory(track: Track) {
        searchHistoryInteractor.addTrack(track)
    }

    fun clearHistory() {
        searchHistoryInteractor.clearHistory()
    }
}