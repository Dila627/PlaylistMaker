package com.example.playlistmaker.presentation.player

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.R

class AudioPlayerViewModel : ViewModel() {

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    private var playerState = STATE_DEFAULT

    private val stateLiveData = MutableLiveData(PlayerState())

    fun observeState(): LiveData<PlayerState> = stateLiveData

    private val timerRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                stateLiveData.value = PlayerState(
                    isPlaying = playerState == STATE_PLAYING,
                    currentTime = formatTime(it.currentPosition)
                )
                handler.postDelayed(this, TIMER_DELAY)
            }
        }
    }

    fun preparePlayer(previewUrl: String?) {
        if (previewUrl.isNullOrBlank()) return

        mediaPlayer = MediaPlayer().apply {
            setDataSource(previewUrl)

            setOnPreparedListener {
                playerState = STATE_PREPARED
            }

            setOnCompletionListener {
                playerState = STATE_PREPARED
                stopTimer()
                stateLiveData.value = PlayerState(
                    isPlaying = false,
                    currentTime = "00:00"
                )
            }

            prepareAsync()
        }
    }

    fun playbackControl() {
        when (playerState) {
            STATE_PLAYING -> pausePlayer()
            STATE_PREPARED -> startPlayer()
        }
    }

    fun pausePlayer() {
        if (playerState == STATE_PLAYING) {
            mediaPlayer?.pause()
            playerState = STATE_PREPARED
            stopTimer()
            stateLiveData.value = PlayerState(
                isPlaying = false,
                currentTime = stateLiveData.value?.currentTime ?: "00:00"
            )
        }
    }

    private fun startPlayer() {
        mediaPlayer?.start()
        playerState = STATE_PLAYING
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
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val TIMER_DELAY = 300L
    }
}