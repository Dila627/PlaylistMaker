package com.example.playlistmaker.presentation.player

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var viewModel: AudioPlayerViewModel

    private lateinit var btnPlay: ImageButton
    private lateinit var tvCurrentTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        viewModel = ViewModelProvider(this)[AudioPlayerViewModel::class.java]

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

        viewModel.observeState().observe(this) { state ->
            tvCurrentTime.text = state.currentTime

            btnPlay.setImageResource(
                if (state.isPlaying) R.drawable.ic_playlist_pause
                else R.drawable.ic_playlist_play
            )
        }

        viewModel.preparePlayer(track?.previewUrl)

        btnPlay.setOnClickListener {
            viewModel.playbackControl()
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

    override fun onPause() {
        super.onPause()
        viewModel.pausePlayer()
    }

    companion object {
        const val TRACK_KEY = "track"
    }
}