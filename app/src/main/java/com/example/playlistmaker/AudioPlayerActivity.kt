package com.example.playlistmaker

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.bumptech.glide.Glide
import com.example.playlistmaker.model.Track

class AudioPlayerActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var playerState = STATE_DEFAULT
    private var isPrepared = false

    private lateinit var btnPlay: ImageButton
    private lateinit var tvCurrentTime: TextView

    private val updateProgressRunnable = object : Runnable {
        override fun run() {
            if (playerState == STATE_PLAYING) {
                tvCurrentTime.text = formatTime(mediaPlayer?.currentPosition ?: 0)
                handler.postDelayed(this, UPDATE_PROGRESS_DELAY)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }

        val track = intent.getSerializableExtra(TRACK_KEY) as? Track

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnPlay = findViewById(R.id.btnPlay)
        val btnLike = findViewById<ImageButton>(R.id.btnLike)
        val cover = findViewById<ImageView>(R.id.cover)

        val tvTrackName = findViewById<TextView>(R.id.tvTrackName)
        val tvArtist = findViewById<TextView>(R.id.tvArtist)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)

        val tvAlbumLabel = findViewById<TextView>(R.id.tvAlbumLabel)
        val tvAlbumValue = findViewById<TextView>(R.id.tvAlbumValue)

        val tvYearLabel = findViewById<TextView>(R.id.tvYearLabel)
        val tvYearValue = findViewById<TextView>(R.id.tvYearValue)

        val tvGenreValue = findViewById<TextView>(R.id.tvGenreValue)
        val tvCountryValue = findViewById<TextView>(R.id.tvCountryValue)
        val tvDurationValue = findViewById<TextView>(R.id.tvDurationValue)

        btnPlay.isEnabled = false
        btnPlay.alpha = 0.5f

        btnBack.setOnClickListener {
            finish()
        }

        tvTrackName.text = track?.trackName.orEmpty()
        tvArtist.text = track?.artistName.orEmpty()
        tvCurrentTime.text = getString(R.string.player_time_default)

        tvDurationValue.text = track?.trackTime.orEmpty()
        tvGenreValue.text = track?.primaryGenreName.orEmpty()
        tvCountryValue.text = track?.country.orEmpty()

        val album = track?.collectionName.orEmpty()
        val year = track?.releaseDate?.take(4).orEmpty()

        val hasAlbum = album.isNotBlank()
        val hasYear = year.isNotBlank()

        tvAlbumLabel.isVisible = hasAlbum
        tvAlbumValue.isVisible = hasAlbum
        tvAlbumValue.text = album

        tvYearLabel.isVisible = hasYear
        tvYearValue.isVisible = hasYear
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

            if (isLiked) {
                btnLike.setImageResource(R.drawable.filled_like_icon)
                btnLike.clearColorFilter()
            } else {
                btnLike.setImageResource(R.drawable.ic_playlist_like)
                btnLike.setColorFilter(getColor(R.color.player_small_button_icon))
            }
        }
    }

    private fun preparePlayer(previewUrl: String?) {
        if (previewUrl.isNullOrEmpty()) return

        mediaPlayer = MediaPlayer().apply {
            setDataSource(previewUrl)
            prepareAsync()

            setOnPreparedListener {
                isPrepared = true
                btnPlay.isEnabled = true
                btnPlay.alpha = 1f
                playerState = STATE_PREPARED
            }

            setOnCompletionListener {
                btnPlay.setImageResource(R.drawable.ic_playlist_play)
                tvCurrentTime.text = getString(R.string.player_time_default)
                handler.removeCallbacks(updateProgressRunnable)
                playerState = STATE_PREPARED
            }
        }
    }

    private fun playbackControl() {
        if (!isPrepared) return

        when (playerState) {
            STATE_PREPARED, STATE_PAUSED -> startPlayer()
            STATE_PLAYING -> pausePlayer()
        }
    }

    private fun startPlayer() {
        mediaPlayer?.start()
        btnPlay.setImageResource(R.drawable.ic_playlist_pause)
        playerState = STATE_PLAYING
        handler.post(updateProgressRunnable)
    }

    private fun pausePlayer() {
        mediaPlayer?.pause()
        btnPlay.setImageResource(R.drawable.ic_playlist_play)
        playerState = STATE_PAUSED
        handler.removeCallbacks(updateProgressRunnable)
    }

    private fun formatTime(millis: Int): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onPause() {
        super.onPause()
        if (playerState == STATE_PLAYING) {
            pausePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateProgressRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
        isPrepared = false
    }

    companion object {
        const val TRACK_KEY = "track"

        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3

        private const val UPDATE_PROGRESS_DELAY = 300L
    }
}