package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val btnSearch = findViewById<MaterialButton>(R.id.btnSearch)
        val btnMedia = findViewById<MaterialButton>(R.id.btnMedia)
        val btnSettings = findViewById<MaterialButton>(R.id.btnSettings)

        btnSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }


        btnMedia.setOnClickListener {
            startActivity(Intent(this, MediaLibraryActivity::class.java))
        }


        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        val prefs = getSharedPreferences("playlist_maker_prefs", MODE_PRIVATE)
        val isDarkTheme = prefs.getBoolean("dark_theme", false)

        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

    }

}
