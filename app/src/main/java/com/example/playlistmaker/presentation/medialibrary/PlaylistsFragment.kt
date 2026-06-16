package com.example.playlistmaker.presentation.medialibrary

import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment(R.layout.fragment_playlists) {

    private val viewModel: PlaylistsViewModel by viewModel()

    companion object {
        fun newInstance() = PlaylistsFragment()
    }
}