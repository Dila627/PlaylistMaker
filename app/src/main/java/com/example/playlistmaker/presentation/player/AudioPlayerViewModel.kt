package com.example.playlistmaker.presentation.player

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.search.FavoriteTracksInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioPlayerViewModel(
    private val mediaPlayer: MediaPlayer,
    private val favoriteTracksInteractor: FavoriteTracksInteractor
) : ViewModel() {

    private var playerState = PlayerStateType.DEFAULT
    private var timerJob: Job? = null

    private var currentTrack: Track? = null
    private var preparedTrackId: Long? = null

    private val stateLiveData = MutableLiveData(PlayerState())

    fun observeState(): LiveData<PlayerState> = stateLiveData

    fun initialize(track: Track) {
        currentTrack = track
        checkFavorite(track.trackId)

        if (preparedTrackId != track.trackId) {
            preparedTrackId = track.trackId
            preparePlayer(track.previewUrl)
        }
    }

    private fun checkFavorite(trackId: Long) {
        viewModelScope.launch {
            val isFavorite = favoriteTracksInteractor.isFavorite(trackId)

            stateLiveData.value = currentState().copy(
                isFavorite = isFavorite
            )
        }
    }

    fun onFavoriteClicked() {
        val track = currentTrack ?: return
        val isCurrentlyFavorite = currentState().isFavorite

        viewModelScope.launch {
            if (isCurrentlyFavorite) {
                favoriteTracksInteractor.removeTrack(track)
            } else {
                favoriteTracksInteractor.addTrack(track)
            }

            stateLiveData.value = currentState().copy(
                isFavorite = !isCurrentlyFavorite
            )
        }
    }

    private fun preparePlayer(previewUrl: String?) {
        if (previewUrl.isNullOrBlank()) return

        mediaPlayer.setDataSource(previewUrl)

        mediaPlayer.setOnPreparedListener {
            playerState = PlayerStateType.PREPARED

            stateLiveData.value = currentState().copy(
                isPlaying = false,
                currentTime = DEFAULT_TIME
            )
        }

        mediaPlayer.setOnCompletionListener {
            playerState = PlayerStateType.PREPARED
            stopTimer()

            stateLiveData.value = currentState().copy(
                isPlaying = false,
                currentTime = DEFAULT_TIME
            )
        }

        mediaPlayer.prepareAsync()
    }

    fun playbackControl() {
        when (playerState) {
            PlayerStateType.PLAYING -> pausePlayer()
            PlayerStateType.PREPARED -> startPlayer()
            PlayerStateType.DEFAULT -> Unit
        }
    }

    fun pausePlayer() {
        if (playerState != PlayerStateType.PLAYING) return

        mediaPlayer.pause()
        playerState = PlayerStateType.PREPARED
        stopTimer()

        stateLiveData.value = currentState().copy(
            isPlaying = false,
            currentTime = formatTime(mediaPlayer.currentPosition)
        )
    }

    private fun startPlayer() {
        mediaPlayer.start()
        playerState = PlayerStateType.PLAYING

        stateLiveData.value = currentState().copy(
            isPlaying = true,
            currentTime = formatTime(mediaPlayer.currentPosition)
        )

        startTimer()
    }

    private fun startTimer() {
        stopTimer()

        timerJob = viewModelScope.launch {
            while (isActive && playerState == PlayerStateType.PLAYING) {
                stateLiveData.value = currentState().copy(
                    isPlaying = true,
                    currentTime = formatTime(mediaPlayer.currentPosition)
                )

                delay(TIMER_DELAY)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun currentState(): PlayerState {
        return stateLiveData.value ?: PlayerState()
    }

    private fun formatTime(timeMillis: Int): String {
        val totalSeconds = timeMillis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onCleared() {
        stopTimer()
        mediaPlayer.release()
        super.onCleared()
    }

    companion object {
        private const val TIMER_DELAY = 300L
        private const val DEFAULT_TIME = "00:00"
    }
}