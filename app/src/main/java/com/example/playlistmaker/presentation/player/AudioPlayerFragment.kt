package com.example.playlistmaker.presentation.player

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import org.koin.androidx.viewmodel.ext.android.viewModel

class AudioPlayerFragment : Fragment(R.layout.fragment_audio_player) {

    private val viewModel: AudioPlayerViewModel by viewModel()

    private var btnPlay: ImageButton? = null
    private var tvCurrentTime: TextView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val track = arguments?.getSerializable(TRACK_KEY) as? Track

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        btnPlay = view.findViewById(R.id.btnPlay)
        val btnLike = view.findViewById<ImageButton>(R.id.btnLike)
        val cover = view.findViewById<ImageView>(R.id.cover)

        val tvTrackName = view.findViewById<TextView>(R.id.tvTrackName)
        val tvArtist = view.findViewById<TextView>(R.id.tvArtist)
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime)

        val tvDurationValue = view.findViewById<TextView>(R.id.tvDurationValue)
        val tvAlbumLabel = view.findViewById<TextView>(R.id.tvAlbumLabel)
        val tvAlbumValue = view.findViewById<TextView>(R.id.tvAlbumValue)
        val tvYearLabel = view.findViewById<TextView>(R.id.tvYearLabel)
        val tvYearValue = view.findViewById<TextView>(R.id.tvYearValue)
        val tvGenreValue = view.findViewById<TextView>(R.id.tvGenreValue)
        val tvCountryValue = view.findViewById<TextView>(R.id.tvCountryValue)

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        tvTrackName.text = track?.trackName.orEmpty()
        tvArtist.text = track?.artistName.orEmpty()
        tvCurrentTime?.text = "00:00"

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

        viewModel.observeState().observe(viewLifecycleOwner) { state ->
            tvCurrentTime?.text = state.currentTime

            btnPlay?.setImageResource(
                if (state.isPlaying) R.drawable.ic_playlist_pause
                else R.drawable.ic_playlist_play
            )
        }

        viewModel.preparePlayer(track?.previewUrl)

        btnPlay?.setOnClickListener {
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
    override fun onDestroyView() {
        btnPlay?.setOnClickListener(null)

        btnPlay = null
        tvCurrentTime = null

        super.onDestroyView()
    }
    companion object {
        const val TRACK_KEY = "track"

        fun newInstance() = AudioPlayerFragment()
    }
}