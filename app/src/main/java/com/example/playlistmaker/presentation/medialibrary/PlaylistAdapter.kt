package com.example.playlistmaker.presentation.medialibrary

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Playlist

class PlaylistAdapter(
    private val playlists: MutableList<Playlist> = mutableListOf(),
    private val onItemClick: ((Playlist) -> Unit)? = null
) : RecyclerView.Adapter<PlaylistViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlaylistViewHolder {

        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_playlist,
                parent,
                false
            )

        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PlaylistViewHolder,
        position: Int
    ) {
        val playlist = playlists[position]

        holder.bind(playlist)

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(playlist)
        }
    }

    override fun getItemCount(): Int = playlists.size

    fun updatePlaylists(newPlaylists: List<Playlist>) {
        playlists.clear()
        playlists.addAll(newPlaylists)
        notifyDataSetChanged()
    }
}