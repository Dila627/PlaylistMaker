package com.example.playlistmaker.presentation.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Playlist
import java.io.File

class PlaylistBottomSheetAdapter(
    private val onPlaylistClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistBottomSheetAdapter.PlaylistViewHolder>() {

    private val playlists = mutableListOf<Playlist>()

    fun updatePlaylists(newPlaylists: List<Playlist>) {
        playlists.clear()
        playlists.addAll(newPlaylists)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlaylistViewHolder {

        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_playlist_bottom_sheet,
                parent,
                false
            )

        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PlaylistViewHolder,
        position: Int
    ) {
        holder.bind(playlists[position])
    }

    override fun getItemCount(): Int = playlists.size

    inner class PlaylistViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val cover: ImageView =
            itemView.findViewById(R.id.ivPlaylistCover)

        private val name: TextView =
            itemView.findViewById(R.id.tvPlaylistName)

        private val count: TextView =
            itemView.findViewById(R.id.tvTracksCount)

        fun bind(playlist: Playlist) {

            name.text = playlist.name

            count.text =
                itemView.resources.getQuantityString(
                    R.plurals.tracks_count,
                    playlist.tracksCount,
                    playlist.tracksCount
                )

            if (playlist.imagePath.isNullOrBlank()) {

                cover.setImageResource(
                    R.drawable.ic_placeholder
                )

            } else {

                Glide.with(itemView)
                    .load(
                        File(playlist.imagePath)
                    )
                    .placeholder(
                        R.drawable.ic_placeholder
                    )
                    .error(
                        R.drawable.ic_placeholder
                    )
                    .into(cover)
            }
        }
    }
}