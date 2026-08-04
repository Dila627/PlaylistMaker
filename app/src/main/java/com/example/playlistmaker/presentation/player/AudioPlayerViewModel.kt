package com.example.playlistmaker.presentation.player

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioPlayerViewModel(
    private val mediaPlayer: MediaPlayer
) : ViewModel() {

    private var playerState = PlayerStateType.DEFAULT

    private val stateLiveData = MutableLiveData(PlayerState())
    fun observeState(): LiveData<PlayerState> = stateLiveData

    private var timerJob: Job? = null

    fun preparePlayer(previewUrl: String?) {
        if (previewUrl.isNullOrBlank()) return

        mediaPlayer.setDataSource(previewUrl)

        mediaPlayer.setOnPreparedListener {
            playerState = PlayerStateType.PREPARED

            stateLiveData.value = PlayerState(
                isPlaying = false,
                currentTime = DEFAULT_TIME
            )
        }

        mediaPlayer.setOnCompletionListener {
            playerState = PlayerStateType.PREPARED
            stopTimer()

            stateLiveData.value = PlayerState(
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

        stateLiveData.value = PlayerState(
            isPlaying = false,
            currentTime = formatTime(mediaPlayer.currentPosition)
        )
    }

    private fun startPlayer() {
        mediaPlayer.start()
        playerState = PlayerStateType.PLAYING

        stateLiveData.value = PlayerState(
            isPlaying = true,
            currentTime = formatTime(mediaPlayer.currentPosition)
        )

        startTimer()
    }

    private fun startTimer() {
        stopTimer()

        timerJob = viewModelScope.launch {
            while (isActive && playerState == PlayerStateType.PLAYING) {
                stateLiveData.value = PlayerState(
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

    private fun formatTime(timeMillis: Int): String {
        val seconds = timeMillis / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        return String.format(
            "%02d:%02d",
            minutes,
            remainingSeconds
        )
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