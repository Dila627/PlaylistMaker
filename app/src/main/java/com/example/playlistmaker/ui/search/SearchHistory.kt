package com.example.playlistmaker.ui.search

import android.content.SharedPreferences
import com.example.playlistmaker.model.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(
    private val prefs: SharedPreferences,
    private val gson: Gson = Gson()
) {
    companion object {
        private const val KEY_HISTORY = "search_history"
        private const val MAX_SIZE = 10
    }

    fun getHistory(): ArrayList<Track> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return arrayListOf()
        val type = object : TypeToken<ArrayList<Track>>() {}.type
        return gson.fromJson(json, type) ?: arrayListOf()
    }

    fun addTrack(track: Track) {
        val history = getHistory()

        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)

        if (history.size > MAX_SIZE) {
            history.subList(MAX_SIZE, history.size).clear()
        }

        save(history)
    }

    fun clear() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun save(history: ArrayList<Track>) {
        prefs.edit().putString(KEY_HISTORY, gson.toJson(history)).apply()
    }
}