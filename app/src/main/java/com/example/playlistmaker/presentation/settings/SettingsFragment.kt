package com.example.playlistmaker.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import com.google.android.material.switchmaterial.SwitchMaterial
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModel()

    companion object {
        fun newInstance() = SettingsFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val switchTheme = view.findViewById<SwitchMaterial>(R.id.switchTheme)

        viewModel.observeTheme().observe(viewLifecycleOwner) { isDark ->
            switchTheme.isChecked = isDark
        }

        viewModel.loadTheme()

        switchTheme.setOnCheckedChangeListener { _, checked ->
            viewModel.switchTheme(checked)
        }

        view.findViewById<TextView>(R.id.tvShare).setOnClickListener {
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

        view.findViewById<TextView>(R.id.tvSupport).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_subject))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.support_body))
            }

            startActivity(intent)
        }

        view.findViewById<TextView>(R.id.tvAgreement).setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.agreement_url))
            )
            startActivity(intent)
        }
    }
}