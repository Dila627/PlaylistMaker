package com.example.playlistmaker.presentation.medialibrary

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.playlist.PlaylistFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment(R.layout.fragment_playlists) {

    private val viewModel: PlaylistsViewModel by viewModel()

    private var recyclerView: RecyclerView? = null
    private var emptyContainer: View? = null
    private var adapter: PlaylistAdapter? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val btnNewPlaylist =
            view.findViewById<View>(R.id.btnNewPlaylist)

        recyclerView =
            view.findViewById(R.id.rvPlaylists)

        emptyContainer =
            view.findViewById(R.id.emptyContainer)

        adapter = PlaylistAdapter { playlist ->

            val bundle = bundleOf(
                PlaylistFragment.PLAYLIST_ID_KEY to playlist.id
            )

            findNavController().navigate(
                R.id.playlistFragment,
                bundle
            )
        }

        recyclerView?.layoutManager =
            GridLayoutManager(
                requireContext(),
                2
            )

        recyclerView?.adapter =
            adapter

        viewModel.observePlaylists()
            .observe(viewLifecycleOwner) { playlists ->

                adapter?.updatePlaylists(playlists)

                val isEmpty =
                    playlists.isEmpty()

                emptyContainer?.isVisible =
                    isEmpty

                recyclerView?.isVisible =
                    !isEmpty
            }

        btnNewPlaylist.setOnClickListener {

            findNavController().navigate(
                R.id.createPlaylistFragment
            )
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

        fun newInstance() =
            PlaylistsFragment()
    }
}