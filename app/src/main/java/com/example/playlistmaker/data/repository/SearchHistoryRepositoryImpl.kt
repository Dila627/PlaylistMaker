package com.example.playlistmaker.data.repository

import android.content.SharedPreferences
import com.example.playlistmaker.domain.api.SearchHistoryRepository
import com.example.playlistmaker.domain.models.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistoryRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : SearchHistoryRepository {

    private val gson = Gson()

    override fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(SEARCH_HISTORY_KEY, null) ?: return emptyList()
        return gson.fromJson(json, object : TypeToken<ArrayList<Track>>() {}.type)
    }

    override fun addTrack(track: Track) {
        val history = getHistory().toMutableList()

        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)

        if (history.size > HISTORY_MAX_SIZE) {
            history.removeAt(history.lastIndex)
        }

        sharedPreferences.edit()
            .putString(SEARCH_HISTORY_KEY, gson.toJson(history))
            .apply()
    }

    override fun clearHistory() {
        sharedPreferences.edit()
            .remove(SEARCH_HISTORY_KEY)
            .apply()
    }

    companion object {
        private const val SEARCH_HISTORY_KEY = "search_history"
        private const val HISTORY_MAX_SIZE = 10
    }
}