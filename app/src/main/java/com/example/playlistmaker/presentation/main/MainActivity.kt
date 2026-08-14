package com.example.playlistmaker.presentation.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.playlistmaker.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_main
        )

        val navHostFragment =
            supportFragmentManager
                .findFragmentById(
                    R.id.navHostFragment
                ) as NavHostFragment

        val navController =
            navHostFragment.navController

        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigation
            )

        // Обычная навигация:
        // Search / Media Library / Settings
        bottomNavigation
            .setupWithNavController(
                navController
            )

        // Если пользователь повторно нажимает
        // на уже выбранную вкладку Media Library,
        // возвращаемся на корневой экран медиатеки.
        bottomNavigation
            .setOnItemReselectedListener { item ->

                when (item.itemId) {

                    R.id.mediaLibraryFragment -> {

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

                    R.id.searchFragment -> {

                        navController
                            .popBackStack(
                                R.id.searchFragment,
                                false
                            )
                    }

                    R.id.settingsFragment -> {

                        navController
                            .popBackStack(
                                R.id.settingsFragment,
                                false
                            )
                    }
                }
            }

        // Показываем / скрываем BottomNavigation
        navController
            .addOnDestinationChangedListener {
                    _,
                    destination,
                    _ ->

                bottomNavigation.isVisible =
                    when (destination.id) {

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
}