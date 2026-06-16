package com.example.playlistmaker.presentation.medialibrary

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import com.google.android.material.tabs.TabLayoutMediator

class MediaLibraryFragment : Fragment(R.layout.fragment_media_library) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout =
            view.findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabLayout)

        val viewPager =
            view.findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.viewPager)

        viewPager.adapter = MediaLibraryPagerAdapter(requireActivity())

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.favorite_tracks)
                1 -> tab.text = getString(R.string.playlists)
            }
        }.attach()
    }

    companion object {
        fun newInstance() = MediaLibraryFragment()
    }
}