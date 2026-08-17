package com.example.playlistmaker.presentation.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.playlistmaker.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavigation: BottomNavigationView

    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_main
        )

        initNavigation()
        setupBottomNavigation()
        setupBottomNavigationReselection()
        setupBottomNavigationVisibility()
    }

    // =========================================================
    // INIT NAVIGATION
    // =========================================================

    private fun initNavigation() {

        val navHostFragment =
            supportFragmentManager
                .findFragmentById(
                    R.id.navHostFragment
                ) as NavHostFragment

        navController =
            navHostFragment.navController

        bottomNavigation =
            findViewById(
                R.id.bottomNavigation
            )
    }

    // =========================================================
    // BOTTOM NAVIGATION
    // =========================================================

    private fun setupBottomNavigation() {

        bottomNavigation
            .setupWithNavController(
                navController
            )
    }

    // =========================================================
    // RESELECTED ITEM
    // =========================================================

    private fun setupBottomNavigationReselection() {

        bottomNavigation
            .setOnItemReselectedListener { item ->

                when (item.itemId) {

                    R.id.mediaLibraryFragment -> {

                        returnToMediaLibrary()
                    }

                    R.id.searchFragment -> {

                        popToDestination(
                            R.id.searchFragment
                        )
                    }

                    R.id.settingsFragment -> {

                        popToDestination(
                            R.id.settingsFragment
                        )
                    }
                }
            }
    }

    // =========================================================
    // RETURN TO MEDIA LIBRARY ROOT
    // =========================================================

    private fun returnToMediaLibrary() {

        val wasPopped =
            navController
                .popBackStack(
                    R.id.mediaLibraryFragment,
                    false
                )

        if (
            !wasPopped &&
            navController
                .currentDestination
                ?.id !=
            R.id.mediaLibraryFragment
        ) {

            navController.navigate(
                R.id.mediaLibraryFragment
            )
        }
    }

    // =========================================================
    // POP TO ROOT DESTINATION
    // =========================================================

    private fun popToDestination(
        destinationId: Int
    ) {

        navController
            .popBackStack(
                destinationId,
                false
            )
    }

    // =========================================================
    // SHOW / HIDE BOTTOM NAVIGATION
    // =========================================================

    private fun setupBottomNavigationVisibility() {

        navController
            .addOnDestinationChangedListener {
                    _,
                    destination,
                    _ ->

                bottomNavigation.isVisible =
                    shouldShowBottomNavigation(
                        destination.id
                    )
            }
    }

    // =========================================================
    // DESTINATION VISIBILITY
    // =========================================================

    private fun shouldShowBottomNavigation(
        destinationId: Int
    ): Boolean {

        return when (destinationId) {

            R.id.audioPlayerFragment,
            R.id.playlistFragment,
            R.id.createPlaylistFragment -> {
                false
            }

            else -> {
                true
            }
        }
    }
}