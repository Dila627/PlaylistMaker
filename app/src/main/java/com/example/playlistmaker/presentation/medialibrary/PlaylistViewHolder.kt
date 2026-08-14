package com.example.playlistmaker.presentation.medialibrary

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Playlist

class PlaylistViewHolder(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    private val cover: ImageView =
        itemView.findViewById(R.id.ivPlaylistCover)

    private val name: TextView =
        itemView.findViewById(R.id.tvPlaylistName)

    private val tracksCount: TextView =
        itemView.findViewById(R.id.tvTracksCount)

    fun bind(playlist: Playlist) {

        name.text = playlist.name

        tracksCount.text =
            "${playlist.tracksCount} треков"

        val imagePath = playlist.imagePath

        if (imagePath.isNullOrBlank()) {
            cover.setImageResource(R.drawable.ic_placeholder)
        } else {
            Glide.with(itemView)
                .load(Uri.parse(imagePath))
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(cover)
        }
    }
}