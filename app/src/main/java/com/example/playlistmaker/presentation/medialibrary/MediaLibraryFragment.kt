package com.example.playlistmaker.presentation.medialibrary

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.playlistmaker.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MediaLibraryFragment : Fragment(R.layout.fragment_media_library) {

    private var tabLayoutMediator: TabLayoutMediator? = null
    private var viewPager: ViewPager2? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = view.findViewById(R.id.viewPager)
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)

        viewPager?.adapter = MediaLibraryPagerAdapter(requireActivity())

        tabLayoutMediator = TabLayoutMediator(
            tabLayout,
            requireNotNull(viewPager)
        ) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.favorite_tracks)
                else -> getString(R.string.playlists)
            }
        }

        tabLayoutMediator?.attach()
    }

    override fun onDestroyView() {
        tabLayoutMediator?.detach()
        tabLayoutMediator = null

        viewPager?.adapter = null
        viewPager = null

        super.onDestroyView()
    }

    companion object {
        fun newInstance() = MediaLibraryFragment()
    }
}