package com.example.playlistmaker.presentation.player

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AudioPlayerViewModel(
    private val mediaPlayer: MediaPlayer
) : ViewModel() {

    private val handler = Handler(Looper.getMainLooper())

    private var playerState = PlayerStateType.DEFAULT

    private val stateLiveData = MutableLiveData(PlayerState())

    fun observeState(): LiveData<PlayerState> = stateLiveData

    private val timerRunnable = object : Runnable {
        override fun run() {
            stateLiveData.value = PlayerState(
                isPlaying = playerState == PlayerStateType.PLAYING,
                currentTime = formatTime(mediaPlayer.currentPosition)
            )
            handler.postDelayed(this, TIMER_DELAY)
        }
    }

    fun preparePlayer(previewUrl: String?) {
        if (previewUrl.isNullOrBlank()) return

        mediaPlayer.setDataSource(previewUrl)

        mediaPlayer.setOnPreparedListener {
            playerState = PlayerStateType.PREPARED
        }

        mediaPlayer.setOnCompletionListener {
            playerState = PlayerStateType.PREPARED
            stopTimer()
            stateLiveData.value = PlayerState(
                isPlaying = false,
                currentTime = "00:00"
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
        if (playerState == PlayerStateType.PLAYING) {
            mediaPlayer.pause()
            playerState = PlayerStateType.PREPARED
            stopTimer()
            stateLiveData.value = PlayerState(
                isPlaying = false,
                currentTime = stateLiveData.value?.currentTime ?: "00:00"
            )
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        playerState = PlayerStateType.PLAYING
        startTimer()
        stateLiveData.value = PlayerState(
            isPlaying = true,
            currentTime = stateLiveData.value?.currentTime ?: "00:00"
        )
    }

    private fun startTimer() {
        handler.post(timerRunnable)
    }

    private fun stopTimer() {
        handler.removeCallbacks(timerRunnable)
    }

    private fun formatTime(timeMillis: Int): String {
        val seconds = timeMillis / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        mediaPlayer.release()
    }

    companion object {
        private const val TIMER_DELAY = 300L
    }
}