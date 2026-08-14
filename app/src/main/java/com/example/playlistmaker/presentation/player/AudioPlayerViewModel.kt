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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioPlayerViewModel(
    private val mediaPlayer: MediaPlayer,
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private var playerState =
        PlayerStateType.DEFAULT

    // =========================================================
    // PLAYER STATE
    // =========================================================

    private val stateLiveData =
        MutableLiveData(
            PlayerState()
        )

    fun observeState():
            LiveData<PlayerState> =
        stateLiveData

    // =========================================================
    // PLAYLISTS
    // =========================================================

    private val playlistsLiveData =
        MutableLiveData<List<Playlist>>()

    fun observePlaylists():
            LiveData<List<Playlist>> =
        playlistsLiveData

    // =========================================================
    // ADD TRACK RESULT
    //
    // SharedFlow используется как одноразовое событие.
    //
    // replay = 0:
    // старый результат НЕ будет отправлен новому observer.
    // =========================================================

    private val playlistAddResultFlow =
        MutableSharedFlow<PlaylistAddResult>(
            replay = 0,
            extraBufferCapacity = 1
        )

    fun observePlaylistAddResult():
            Flow<PlaylistAddResult> =
        playlistAddResultFlow
            .asSharedFlow()

    // =========================================================
    // TIMER
    // =========================================================

    private var timerJob:
            Job? = null

    // =========================================================
    // CURRENT TRACK URL
    // =========================================================

    private var currentPreviewUrl:
            String? = null

    // =========================================================
    // INIT
    // =========================================================

    init {
        observePlaylistsFromDatabase()
    }

    // =========================================================
    // OBSERVE PLAYLISTS FROM ROOM
    // =========================================================

    private fun observePlaylistsFromDatabase() {

        viewModelScope.launch {

            playlistsInteractor
                .getPlaylists()
                .collect { playlists ->

                    playlistsLiveData.value =
                        playlists
                }
        }
    }

    // =========================================================
    // ADD TRACK TO PLAYLIST
    // =========================================================

    fun addTrackToPlaylist(
        track: Track,
        playlist: Playlist
    ) {

        viewModelScope.launch {

            val isAdded =
                playlistsInteractor
                    .addTrackToPlaylist(
                        track = track,
                        playlist = playlist
                    )

            /*
             * Если в этот момент Bottom Sheet открыт,
             * событие получит его collector.
             *
             * Если Bottom Sheet уже закрыт —
             * событие не сохранится и не появится
             * при следующем открытии.
             */
            playlistAddResultFlow.emit(
                PlaylistAddResult(
                    playlistName =
                        playlist.name,

                    isAdded =
                        isAdded
                )
            )
        }
    }

    // =========================================================
    // PREPARE PLAYER
    // =========================================================

    fun preparePlayer(
        previewUrl: String?
    ) {

        if (
            previewUrl.isNullOrBlank()
        ) {
            return
        }

        /*
         * Если этот же трек уже подготовлен,
         * повторно setDataSource не вызываем.
         */
        if (
            currentPreviewUrl ==
            previewUrl
        ) {
            return
        }

        stopTimer()

        mediaPlayer.reset()

        playerState =
            PlayerStateType.DEFAULT

        stateLiveData.value =
            currentState().copy(
                isPlaying = false,
                currentTime = DEFAULT_TIME
            )

        // =====================================================
        // PREPARED
        // =====================================================

        mediaPlayer
            .setOnPreparedListener {

                playerState =
                    PlayerStateType.PREPARED

                stateLiveData.value =
                    currentState().copy(
                        isPlaying = false,
                        currentTime = DEFAULT_TIME
                    )
            }

        // =====================================================
        // COMPLETION
        // =====================================================

        mediaPlayer
            .setOnCompletionListener {

                playerState =
                    PlayerStateType.PREPARED

                stopTimer()

                mediaPlayer.seekTo(
                    0
                )

                stateLiveData.value =
                    currentState().copy(
                        isPlaying = false,
                        currentTime = DEFAULT_TIME
                    )
            }

        // =====================================================
        // ERROR
        // =====================================================

        mediaPlayer
            .setOnErrorListener {
                    _,
                    _,
                    _ ->

                stopTimer()

                playerState =
                    PlayerStateType.DEFAULT

                currentPreviewUrl =
                    null

                stateLiveData.value =
                    currentState().copy(
                        isPlaying = false,
                        currentTime = DEFAULT_TIME
                    )

                true
            }

        // =====================================================
        // DATA SOURCE
        // =====================================================

        try {

            mediaPlayer.setDataSource(
                previewUrl
            )

            currentPreviewUrl =
                previewUrl

            mediaPlayer.prepareAsync()

        } catch (
            exception: Exception
        ) {

            currentPreviewUrl =
                null

            playerState =
                PlayerStateType.DEFAULT

            stateLiveData.value =
                currentState().copy(
                    isPlaying = false,
                    currentTime = DEFAULT_TIME
                )
        }
    }

    // =========================================================
    // PLAYBACK CONTROL
    // =========================================================

    fun playbackControl() {

        when (
            playerState
        ) {

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

    // =========================================================
    // PAUSE
    // =========================================================

    fun pausePlayer() {

        if (
            playerState !=
            PlayerStateType.PLAYING
        ) {
            return
        }

        mediaPlayer.pause()

        playerState =
            PlayerStateType.PREPARED

        stopTimer()

        stateLiveData.value =
            currentState().copy(
                isPlaying = false,
                currentTime =
                    formatTime(
                        mediaPlayer.currentPosition
                    )
            )
    }

    // =========================================================
    // START
    // =========================================================

    private fun startPlayer() {

        if (
            playerState !=
            PlayerStateType.PREPARED
        ) {
            return
        }

        mediaPlayer.start()

        playerState =
            PlayerStateType.PLAYING

        stateLiveData.value =
            currentState().copy(
                isPlaying = true,
                currentTime =
                    formatTime(
                        mediaPlayer.currentPosition
                    )
            )

        startTimer()
    }

    // =========================================================
    // TIMER
    // =========================================================

    private fun startTimer() {

        stopTimer()

        timerJob =
            viewModelScope.launch {

                while (
                    isActive &&
                    playerState ==
                    PlayerStateType.PLAYING
                ) {

                    stateLiveData.value =
                        currentState().copy(
                            isPlaying = true,
                            currentTime =
                                formatTime(
                                    mediaPlayer
                                        .currentPosition
                                )
                        )

                    delay(
                        TIMER_DELAY
                    )
                }
            }
    }

    private fun stopTimer() {

        timerJob?.cancel()

        timerJob =
            null
    }

    // =========================================================
    // CURRENT STATE
    // =========================================================

    private fun currentState():
            PlayerState {

        return stateLiveData.value
            ?: PlayerState()
    }

    // =========================================================
    // FORMAT TIME
    // =========================================================

    private fun formatTime(
        timeMillis: Int
    ): String {

        val seconds =
            timeMillis / 1000

        val minutes =
            seconds / 60

        val remainingSeconds =
            seconds % 60

        return String.format(
            "%02d:%02d",
            minutes,
            remainingSeconds
        )
    }

    // =========================================================
    // CLEAR
    // =========================================================

    override fun onCleared() {

        stopTimer()

        mediaPlayer
            .setOnPreparedListener(
                null
            )

        mediaPlayer
            .setOnCompletionListener(
                null
            )

        mediaPlayer
            .setOnErrorListener(
                null
            )

        mediaPlayer.release()

        currentPreviewUrl =
            null

        super.onCleared()
    }

    companion object {

        private const val TIMER_DELAY =
            300L

        private const val DEFAULT_TIME =
            "00:00"
    }
}

// =============================================================
// ADD RESULT
// =============================================================

data class PlaylistAddResult(
    val playlistName: String,
    val isAdded: Boolean
)