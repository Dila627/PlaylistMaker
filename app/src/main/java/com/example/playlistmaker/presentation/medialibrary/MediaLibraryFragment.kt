package com.example.playlistmaker.presentation.medialibrary

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.playlistmaker.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MediaLibraryFragment :
    Fragment(R.layout.fragment_media_library) {

    private var tabLayoutMediator:
            TabLayoutMediator? = null

    private var viewPager:
            ViewPager2? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        // =====================================================
        // VIEW PAGER
        // =====================================================

        viewPager =
            view.findViewById(
                R.id.viewPager
            )

        // =====================================================
        // TAB LAYOUT
        // =====================================================

        val tabLayout =
            view.findViewById<TabLayout>(
                R.id.tabLayout
            )

        // =====================================================
        // ADAPTER
        // =====================================================

        viewPager?.adapter =
            MediaLibraryPagerAdapter(
                this
            )

        // =====================================================
        // TAB LAYOUT MEDIATOR
        // =====================================================

        tabLayoutMediator =
            TabLayoutMediator(
                tabLayout,
                requireNotNull(
                    viewPager
                )
            ) { tab, position ->

                tab.text =
                    when (
                        position
                    ) {

                        FAVORITES_POSITION -> {

                            getString(
                                R.string.favorite_tracks
                            )
                        }

                        else -> {

                            getString(
                                R.string.playlists
                            )
                        }
                    }
            }

        tabLayoutMediator
            ?.attach()

        // =====================================================
        // RETURN FROM PLAYLIST
        // =====================================================

        /*
         * Если мы вернулись именно
         * с экрана конкретного плейлиста,
         * открываем вкладку "Плейлисты".
         */
        val savedStateHandle =
            findNavController()
                .currentBackStackEntry
                ?.savedStateHandle

        val shouldOpenPlaylists =
            savedStateHandle
                ?.get<Boolean>(
                    OPEN_PLAYLISTS_TAB_KEY
                ) == true

        if (
            shouldOpenPlaylists
        ) {

            viewPager
                ?.setCurrentItem(
                    PLAYLISTS_POSITION,
                    false
                )

            /*
             * Сбрасываем флаг,
             * чтобы при обычном открытии
             * Медиатеки вкладка не переключалась
             * автоматически.
             */
            savedStateHandle
                ?.set(
                    OPEN_PLAYLISTS_TAB_KEY,
                    false
                )
        }
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    override fun onDestroyView() {

        tabLayoutMediator
            ?.detach()

        tabLayoutMediator =
            null

        viewPager
            ?.adapter =
            null

        viewPager =
            null

        super.onDestroyView()
    }

    companion object {

        const val OPEN_PLAYLISTS_TAB_KEY =
            "openPlaylistsTab"

        private const val FAVORITES_POSITION =
            0

        private const val PLAYLISTS_POSITION =
            1

        fun newInstance() =
            MediaLibraryFragment()
    }
}