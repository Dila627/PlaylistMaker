package com.example.playlistmaker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {



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
        val app = applicationContext as App

        switchTheme.isChecked = app.darkTheme

        switchTheme.setOnCheckedChangeListener { _, checked ->
            app.switchTheme(checked)
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


}
