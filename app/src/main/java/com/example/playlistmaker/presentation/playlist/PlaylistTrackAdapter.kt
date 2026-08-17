package com.example.playlistmaker.presentation.playlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track

class PlaylistTrackAdapter(
    private val tracks: MutableList<Track> = mutableListOf(),
    private val onTrackClick: (Track) -> Unit,
    private val onTrackLongClick: (Track) -> Unit
) : RecyclerView.Adapter<PlaylistTrackViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlaylistTrackViewHolder {

        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_playlist_track,
                parent,
                false
            )

        return PlaylistTrackViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PlaylistTrackViewHolder,
        position: Int
    ) {

        val track = tracks[position]

        holder.bind(track)

        // Обычное нажатие
        holder.itemView.setOnClickListener {

            onTrackClick(track)
        }

        // Долгое нажатие
        holder.itemView.setOnLongClickListener {

            onTrackLongClick(track)

            true
        }
    }

    override fun getItemCount(): Int {
        return tracks.size
    }

    fun updateTracks(
        newTracks: List<Track>
    ) {

        tracks.clear()
        tracks.addAll(newTracks)

        notifyDataSetChanged()
    }
}