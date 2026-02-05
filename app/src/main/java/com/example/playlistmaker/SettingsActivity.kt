package com.example.playlistmaker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private val prefs by lazy {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //  Edge-to-Edge
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //  Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                top = systemBars.top,
                bottom = systemBars.bottom
            )
            insets
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // ---- Switch theme ----
        val switchTheme = findViewById<SwitchMaterial>(R.id.switchTheme)
        val isDarkTheme = prefs.getBoolean(KEY_DARK_THEME, false)
        switchTheme.isChecked = isDarkTheme

        switchTheme.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(KEY_DARK_THEME, checked).apply()

            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // ---- Share ----
        findViewById<TextView>(R.id.tvShare).setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_text))
            }
            startActivity(
                Intent.createChooser(
                    shareIntent,
                    getString(R.string.share_app_chooser_title)
                )
            )
        }

        // ---- Support (Email) ----
        findViewById<TextView>(R.id.tvSupport).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_subject))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.support_body))
            }
            startActivity(intent)
        }

        // ---- Agreement (Browser) ----
        findViewById<TextView>(R.id.tvAgreement).setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.agreement_url))
            )
            startActivity(intent)
        }
    }

    companion object {
        private const val PREFS_NAME = "playlist_maker_prefs"
        private const val KEY_DARK_THEME = "dark_theme"
    }
}
