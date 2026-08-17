package com.example.playlistmaker.presentation.player

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.playlists.PlaylistsInteractor
import com.example.playlistmaker.domain.search.FavoriteTracksInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AudioPlayerViewModel(
    private val mediaPlayer: MediaPlayer,
    private val favoriteTracksInteractor: FavoriteTracksInteractor,
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
    // ADD TO PLAYLIST RESULT
    // =========================================================

    /*
     * Одноразовое событие.
     *
     * replay = 0 означает:
     * старый результат не будет повторно
     * отдан при следующем открытии BottomSheet.
     */
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
    // FAVORITES
    // =========================================================

    /*
     * Не позволяет двум быстрым нажатиям
     * одновременно менять состояние избранного.
     */
    private val favoriteMutex =
        Mutex()

    private var currentTrack:
            Track? = null

    // =========================================================
    // PLAYER
    // =========================================================

    private var timerJob:
            Job? = null

    private var currentPreviewUrl:
            String? = null

    private var preparedTrackId:
            Long? = null

    // =========================================================
    // INIT
    // =========================================================

    init {
        observePlaylistsFromDatabase()
    }

    // =========================================================
    // INITIALIZE TRACK
    // =========================================================

    fun initialize(
        track: Track
    ) {

        currentTrack =
            track

        /*
         * Сразу показываем состояние,
         * которое пришло вместе с Track,
         * а затем дополнительно проверяем Room.
         */
        stateLiveData.value =
            currentState().copy(
                isFavorite =
                    track.isFavorite
            )

        checkFavorite(
            track.trackId
        )

        /*
         * Один и тот же трек повторно
         * в MediaPlayer не загружаем.
         */
        if (
            preparedTrackId !=
            track.trackId
        ) {

            preparedTrackId =
                track.trackId

            preparePlayer(
                track.previewUrl
            )
        }
    }

    // =========================================================
    // CHECK FAVORITE
    // =========================================================

    private fun checkFavorite(
        trackId: Long
    ) {

        viewModelScope.launch {

            val isFavorite =
                favoriteTracksInteractor
                    .isFavorite(
                        trackId
                    )

            /*
             * Проверяем, что пользователь
             * всё ещё находится на этом треке.
             */
            if (
                currentTrack?.trackId ==
                trackId
            ) {

                stateLiveData.value =
                    currentState().copy(
                        isFavorite =
                            isFavorite
                    )
            }
        }
    }

    // =========================================================
    // FAVORITE CLICK
    // =========================================================

    fun onFavoriteClicked() {

        val track =
            currentTrack
                ?: return

        viewModelScope.launch {

            favoriteMutex.withLock {

                /*
                 * ВАЖНО:
                 * актуальное значение берём из Room
                 * непосредственно внутри coroutine.
                 */
                val isCurrentlyFavorite =
                    favoriteTracksInteractor
                        .isFavorite(
                            track.trackId
                        )

                if (
                    isCurrentlyFavorite
                ) {

                    favoriteTracksInteractor
                        .removeTrack(
                            track
                        )

                } else {

                    favoriteTracksInteractor
                        .addTrack(
                            track
                        )
                }

                if (
                    currentTrack?.trackId ==
                    track.trackId
                ) {

                    stateLiveData.value =
                        currentState().copy(
                            isFavorite =
                                !isCurrentlyFavorite
                        )
                }
            }
        }
    }

    // =========================================================
    // OBSERVE PLAYLISTS
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

    private fun preparePlayer(
        previewUrl: String?
    ) {

        if (
            previewUrl.isNullOrBlank()
        ) {

            preparedTrackId =
                null

            return
        }

        /*
         * Если этот URL уже подготовлен,
         * второй раз setDataSource не вызываем.
         */
        if (
            currentPreviewUrl ==
            previewUrl
        ) {
            return
        }

        stopTimer()

        /*
         * MediaPlayer должен вернуться
         * в состояние Idle перед новым setDataSource.
         */
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

                preparedTrackId =
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

            preparedTrackId =
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
                                    mediaPlayer.currentPosition
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

        val totalSeconds =
            timeMillis / 1000

        val minutes =
            totalSeconds / 60

        val seconds =
            totalSeconds % 60

        return String.format(
            "%02d:%02d",
            minutes,
            seconds
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

        preparedTrackId =
            null

        currentTrack =
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

data class PlaylistAddResult(
    val playlistName: String,
    val isAdded: Boolean
)