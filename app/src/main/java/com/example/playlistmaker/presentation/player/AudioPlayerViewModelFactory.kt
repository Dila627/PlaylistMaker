package com.example.playlistmaker.presentation.player

import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AudioPlayerViewModelFactory(
    private val mediaPlayer: MediaPlayer
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AudioPlayerViewModel(mediaPlayer) as T
    }
}