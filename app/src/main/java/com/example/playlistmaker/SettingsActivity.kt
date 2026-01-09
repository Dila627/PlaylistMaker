package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
        findViewById<TextView>(R.id.tvShare).setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Playlist Maker — ссылка на приложение")
            }
            startActivity(Intent.createChooser(shareIntent, "Поделиться приложением"))
        }
        findViewById<TextView>(R.id.tvAgreement).setOnClickListener {
            startActivity(Intent(this, AgreementActivity::class.java))
        }

    }

}

