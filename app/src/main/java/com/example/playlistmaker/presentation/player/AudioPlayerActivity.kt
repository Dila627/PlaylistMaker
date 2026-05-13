package com.example.playlistmaker.presentation.player

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track

class AudioPlayerActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var btnPlay: ImageButton
    private lateinit var tvCurrentTime: TextView

    private var playerState = STATE_DEFAULT

    private val timerRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                tvCurrentTime.text = formatTime(it.currentPosition)
                handler.postDelayed(this, TIMER_DELAY)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        val track = intent.getSerializableExtra(TRACK_KEY) as? Track

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnPlay = findViewById(R.id.btnPlay)
        val btnLike = findViewById<ImageButton>(R.id.btnLike)
        val cover = findViewById<ImageView>(R.id.cover)

        val tvTrackName = findViewById<TextView>(R.id.tvTrackName)
        val tvArtist = findViewById<TextView>(R.id.tvArtist)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)

        val tvDurationValue = findViewById<TextView>(R.id.tvDurationValue)
        val tvAlbumLabel = findViewById<TextView>(R.id.tvAlbumLabel)
        val tvAlbumValue = findViewById<TextView>(R.id.tvAlbumValue)
        val tvYearLabel = findViewById<TextView>(R.id.tvYearLabel)
        val tvYearValue = findViewById<TextView>(R.id.tvYearValue)
        val tvGenreValue = findViewById<TextView>(R.id.tvGenreValue)
        val tvCountryValue = findViewById<TextView>(R.id.tvCountryValue)

        btnBack.setOnClickListener {
            finish()
        }

        tvTrackName.text = track?.trackName.orEmpty()
        tvArtist.text = track?.artistName.orEmpty()
        tvCurrentTime.text = "00:00"

        tvDurationValue.text = track?.trackTime.orEmpty()
        tvGenreValue.text = track?.primaryGenreName.orEmpty()
        tvCountryValue.text = track?.country.orEmpty()

        val album = track?.collectionName.orEmpty()
        val year = track?.releaseDate?.take(4).orEmpty()

        tvAlbumLabel.isVisible = album.isNotBlank()
        tvAlbumValue.isVisible = album.isNotBlank()
        tvAlbumValue.text = album

        tvYearLabel.isVisible = year.isNotBlank()
        tvYearValue.isVisible = year.isNotBlank()
        tvYearValue.text = year

        Glide.with(this)
            .load(track?.getCoverArtwork())
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .into(cover)

        preparePlayer(track?.previewUrl)

        btnPlay.setOnClickListener {
            playbackControl()
        }

        var isLiked = false
        btnLike.setOnClickListener {
            isLiked = !isLiked
            btnLike.setImageResource(
                if (isLiked) R.drawable.filled_like_icon
                else R.drawable.ic_playlist_like
            )
        }
    }

    private fun preparePlayer(previewUrl: String?) {
        if (previewUrl.isNullOrBlank()) return

        mediaPlayer = MediaPlayer().apply {
            setDataSource(previewUrl)

            setOnPreparedListener {
                playerState = STATE_PREPARED
            }

            setOnCompletionListener {
                playerState = STATE_PREPARED
                btnPlay.setImageResource(R.drawable.ic_playlist_play)
                tvCurrentTime.text = "00:00"
                stopTimer()
            }

            prepareAsync()
        }
    }

    private fun playbackControl() {
        when (playerState) {
            STATE_PLAYING -> pausePlayer()
            STATE_PREPARED -> startPlayer()
        }
    }

    private fun startPlayer() {
        mediaPlayer?.start()
        btnPlay.setImageResource(R.drawable.ic_playlist_pause)
        playerState = STATE_PLAYING
        startTimer()
    }

    private fun pausePlayer() {
        mediaPlayer?.pause()
        btnPlay.setImageResource(R.drawable.ic_playlist_play)
        playerState = STATE_PREPARED
        stopTimer()
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

    override fun onPause() {
        super.onPause()
        if (playerState == STATE_PLAYING) {
            pausePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        const val TRACK_KEY = "track"

        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2

        private const val TIMER_DELAY = 300L
    }
}