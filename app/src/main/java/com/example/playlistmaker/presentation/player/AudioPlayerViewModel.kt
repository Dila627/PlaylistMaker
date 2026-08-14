package com.example.playlistmaker.presentation.player

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.playlists.PlaylistsInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioPlayerViewModel(
    private val mediaPlayer: MediaPlayer,
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private var playerState = PlayerStateType.DEFAULT

    private val stateLiveData = MutableLiveData(PlayerState())
    fun observeState(): LiveData<PlayerState> = stateLiveData

    private val playlistsLiveData = MutableLiveData<List<Playlist>>()
    fun observePlaylists(): LiveData<List<Playlist>> = playlistsLiveData

    private val playlistAddResultLiveData =
        MutableLiveData<PlaylistAddResult?>()

    fun observePlaylistAddResult(): LiveData<PlaylistAddResult?> =
        playlistAddResultLiveData

    private var timerJob: Job? = null

    // Храним URL трека, который уже был передан в MediaPlayer
    private var currentPreviewUrl: String? = null

    init {
        observePlaylistsFromDatabase()
    }

    private fun observePlaylistsFromDatabase() {
        viewModelScope.launch {
            playlistsInteractor
                .getPlaylists()
                .collect { playlists ->
                    playlistsLiveData.value = playlists
                }
        }
    }

    fun addTrackToPlaylist(
        track: Track,
        playlist: Playlist
    ) {
        viewModelScope.launch {

            val isAdded = playlistsInteractor.addTrackToPlaylist(
                track = track,
                playlist = playlist
            )

            playlistAddResultLiveData.value =
                PlaylistAddResult(
                    playlistName = playlist.name,
                    isAdded = isAdded
                )
        }
    }

    fun playlistAddResultHandled() {
        playlistAddResultLiveData.value = null
    }

    fun preparePlayer(previewUrl: String?) {

        // Если ссылки нет — ничего не делаем
        if (previewUrl.isNullOrBlank()) return

        /*
         * Если этот трек уже был передан в MediaPlayer,
         * повторно setDataSource() вызывать нельзя.
         *
         * Именно из-за повторного setDataSource()
         * происходил IllegalStateException.
         */
        if (currentPreviewUrl == previewUrl) return

        stopTimer()

        /*
         * Возвращаем MediaPlayer в состояние Idle,
         * чтобы можно было безопасно вызвать setDataSource().
         */
        mediaPlayer.reset()

        playerState = PlayerStateType.DEFAULT

        stateLiveData.value =
            currentState().copy(
                isPlaying = false,
                currentTime = DEFAULT_TIME
            )

        mediaPlayer.setOnPreparedListener {
            playerState = PlayerStateType.PREPARED

            stateLiveData.value =
                currentState().copy(
                    isPlaying = false,
                    currentTime = DEFAULT_TIME
                )
        }

        mediaPlayer.setOnCompletionListener {
            playerState = PlayerStateType.PREPARED

            stopTimer()

            // Возвращаем трек в начало
            mediaPlayer.seekTo(0)

            stateLiveData.value =
                currentState().copy(
                    isPlaying = false,
                    currentTime = DEFAULT_TIME
                )
        }

        mediaPlayer.setOnErrorListener { _, _, _ ->

            stopTimer()

            playerState = PlayerStateType.DEFAULT
            currentPreviewUrl = null

            stateLiveData.value =
                currentState().copy(
                    isPlaying = false,
                    currentTime = DEFAULT_TIME
                )

            true
        }

        try {

            mediaPlayer.setDataSource(previewUrl)

            // Запоминаем URL, чтобы не загружать тот же трек повторно
            currentPreviewUrl = previewUrl

            mediaPlayer.prepareAsync()

        } catch (e: Exception) {

            currentPreviewUrl = null
            playerState = PlayerStateType.DEFAULT

            stateLiveData.value =
                currentState().copy(
                    isPlaying = false,
                    currentTime = DEFAULT_TIME
                )
        }
    }

    fun playbackControl() {
        when (playerState) {

            PlayerStateType.PLAYING -> {
                pausePlayer()
            }

            PlayerStateType.PREPARED -> {
                startPlayer()
            }

            PlayerStateType.DEFAULT -> {
                Unit
            }
        }
    }

    fun pausePlayer() {

        if (playerState != PlayerStateType.PLAYING) return

        mediaPlayer.pause()

        playerState = PlayerStateType.PREPARED

        stopTimer()

        stateLiveData.value =
            currentState().copy(
                isPlaying = false,
                currentTime = formatTime(
                    mediaPlayer.currentPosition
                )
            )
    }

    private fun startPlayer() {

        if (playerState != PlayerStateType.PREPARED) return

        mediaPlayer.start()

        playerState = PlayerStateType.PLAYING

        stateLiveData.value =
            currentState().copy(
                isPlaying = true,
                currentTime = formatTime(
                    mediaPlayer.currentPosition
                )
            )

        startTimer()
    }

    private fun startTimer() {

        stopTimer()

        timerJob = viewModelScope.launch {

            while (
                isActive &&
                playerState == PlayerStateType.PLAYING
            ) {

                stateLiveData.value =
                    currentState().copy(
                        isPlaying = true,
                        currentTime = formatTime(
                            mediaPlayer.currentPosition
                        )
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

    private fun formatTime(
        timeMillis: Int
    ): String {

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

        mediaPlayer.setOnPreparedListener(null)
        mediaPlayer.setOnCompletionListener(null)
        mediaPlayer.setOnErrorListener(null)

        mediaPlayer.release()

        currentPreviewUrl = null

        super.onCleared()
    }

    companion object {
        private const val TIMER_DELAY = 300L
        private const val DEFAULT_TIME = "00:00"
    }
}

data class PlaylistAddResult(
    val playlistName: String,
    val isAdded: Boolean
)