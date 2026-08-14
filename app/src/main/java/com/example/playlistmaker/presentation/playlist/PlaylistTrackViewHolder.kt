package com.example.playlistmaker.presentation.playlist

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import java.text.SimpleDateFormat
import java.util.Locale

class PlaylistTrackViewHolder(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    private val cover =
        itemView.findViewById<ImageView>(
            R.id.ivTrackCover
        )

    private val trackName =
        itemView.findViewById<TextView>(
            R.id.tvTrackName
        )

    private val artistAndTime =
        itemView.findViewById<TextView>(
            R.id.tvArtistAndTime
        )

    fun bind(
        track: Track
    ) {

        trackName.text =
            track.trackName

        val duration =
            SimpleDateFormat(
                TRACK_TIME_FORMAT,
                Locale.getDefault()
            ).format(
                track.trackTimeMillis
            )

        artistAndTime.text =
            "${track.artistName} • $duration"

        Glide.with(itemView)
            .load(track.getCoverArtwork())
            .placeholder(
                R.drawable.ic_placeholder
            )
            .error(
                R.drawable.ic_placeholder
            )
            .into(cover)
    }

    companion object {
        private const val TRACK_TIME_FORMAT =
            "mm:ss"
    }
}