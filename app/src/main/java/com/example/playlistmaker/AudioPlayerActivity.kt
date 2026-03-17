package com.example.playlistmaker

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.playlistmaker.model.Track

class AudioPlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        val track = intent.getSerializableExtra(TRACK_KEY) as? Track

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnPlay = findViewById<ImageButton>(R.id.btnPlay)
        val btnLike = findViewById<ImageButton>(R.id.btnLike)
        val cover = findViewById<ImageView>(R.id.cover)

        val tvTrackName = findViewById<TextView>(R.id.tvTrackName)
        val tvArtist = findViewById<TextView>(R.id.tvArtist)
        val tvCurrentTime = findViewById<TextView>(R.id.tvCurrentTime)

        val tvDurationLabel = findViewById<TextView>(R.id.tvDurationLabel)
        val tvDurationValue = findViewById<TextView>(R.id.tvDurationValue)

        val tvAlbumLabel = findViewById<TextView>(R.id.tvAlbumLabel)
        val tvAlbumValue = findViewById<TextView>(R.id.tvAlbumValue)

        val tvYearLabel = findViewById<TextView>(R.id.tvYearLabel)
        val tvYearValue = findViewById<TextView>(R.id.tvYearValue)

        val tvGenreLabel = findViewById<TextView>(R.id.tvGenreLabel)
        val tvGenreValue = findViewById<TextView>(R.id.tvGenreValue)

        val tvCountryLabel = findViewById<TextView>(R.id.tvCountryLabel)
        val tvCountryValue = findViewById<TextView>(R.id.tvCountryValue)

        btnBack.setOnClickListener {
            finish()
        }

        tvTrackName.text = track?.trackName.orEmpty()
        tvArtist.text = track?.artistName.orEmpty()
        tvCurrentTime.text = "0:00"

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

        var isPlaying = false
        btnPlay.setOnClickListener {
            isPlaying = !isPlaying
            btnPlay.setImageResource(
                if (isPlaying) R.drawable.ic_playlist_pause
                else R.drawable.ic_playlist_play
            )
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

    companion object {
        const val TRACK_KEY = "track"
    }
}