package com.example.playlistmaker.presentation.medialibrary

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.player.AudioPlayerFragment
import com.example.playlistmaker.presentation.search.TrackAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoriteTracksFragment : Fragment(R.layout.fragment_favorite_tracks) {

    private val viewModel: FavoriteTracksViewModel by viewModel()

    private var recyclerView: RecyclerView? = null
    private var emptyContainer: View? = null

    private var adapter: TrackAdapter? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvFavoriteTracks)
        emptyContainer = view.findViewById(R.id.emptyContainer)

        adapter = TrackAdapter(
            tracks = mutableListOf()
        ) { track ->
            findNavController().navigate(
                R.id.audioPlayerFragment,
                bundleOf(
                    AudioPlayerFragment.TRACK_KEY to track
                )
            )
        }

        recyclerView?.layoutManager =
            LinearLayoutManager(requireContext())

        recyclerView?.adapter = adapter

        viewModel.observeState()
            .observe(viewLifecycleOwner) { state ->
                when (state) {
                    is FavoriteTracksState.Content -> {
                        adapter?.updateTracks(state.tracks)

                        recyclerView?.isVisible = true
                        emptyContainer?.isVisible = false
                    }

                    FavoriteTracksState.Empty -> {
                        adapter?.updateTracks(emptyList())

                        recyclerView?.isVisible = false
                        emptyContainer?.isVisible = true
                    }
                }
            }
    }

    override fun onDestroyView() {
        recyclerView?.adapter = null

        recyclerView = null
        emptyContainer = null
        adapter = null

        super.onDestroyView()
    }

    companion object {
        fun newInstance() = FavoriteTracksFragment()
    }
}