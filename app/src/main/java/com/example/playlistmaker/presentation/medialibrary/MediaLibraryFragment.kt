package com.example.playlistmaker.presentation.medialibrary

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.playlistmaker.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MediaLibraryFragment : Fragment(R.layout.fragment_media_library) {

    companion object {
        fun newInstance() = MediaLibraryFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)

        viewPager.adapter = MediaLibraryPagerAdapter(requireActivity())

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.favorite_tracks)
                else -> getString(R.string.playlists)
            }
        }.attach()
    }
}