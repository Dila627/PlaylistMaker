package com.example.playlistmaker.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.search.SearchHistoryInteractor
import com.example.playlistmaker.domain.search.TracksInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchViewModel(
    private val tracksInteractor: TracksInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor
) : ViewModel() {

    private val stateLiveData = MutableLiveData<SearchState>()
    fun observeState(): LiveData<SearchState> = stateLiveData

    private var searchJob: Job? = null

    fun search(text: String) {
        if (text.isBlank()) return

        searchJob?.cancel()

        stateLiveData.value = SearchState.Loading

        searchJob = viewModelScope.launch {
            tracksInteractor.searchTracks(text).collect { result ->
                stateLiveData.value = when {
                    result.isError -> {
                        SearchState.Error
                    }

                    result.tracks.isEmpty() -> {
                        SearchState.Empty
                    }

                    else -> {
                        SearchState.Content(result.tracks)
                    }
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

    override fun onCleared() {
        searchJob?.cancel()
        super.onCleared()
    }
}