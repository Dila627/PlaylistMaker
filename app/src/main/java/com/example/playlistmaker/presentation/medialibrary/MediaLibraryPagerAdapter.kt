package com.example.playlistmaker.presentation.medialibrary

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MediaLibraryPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int =
        PAGE_COUNT

    override fun createFragment(
        position: Int
    ): Fragment {

        return when (position) {

            FAVORITES_POSITION -> {
                FavoriteTracksFragment.newInstance()
            }

            PLAYLISTS_POSITION -> {
                PlaylistsFragment.newInstance()
            }

            else -> {
                throw IllegalArgumentException(
                    "Unknown ViewPager position: $position"
                )
            }
        }
    }

    companion object {

        private const val PAGE_COUNT = 2

        private const val FAVORITES_POSITION = 0
        private const val PLAYLISTS_POSITION = 1
    }
}