package com.example.playlistmaker.presentation.playlist

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.player.AudioPlayerFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.WindowManager

class PlaylistFragment : Fragment(R.layout.fragment_playlist) {

    private val viewModel: PlaylistViewModel by viewModel()

    private var tracksAdapter: PlaylistTrackAdapter? = null

    private var currentPlaylist: Playlist? = null

    private var currentTracks: List<Track> =
        emptyList()

    private var tvTracksCount: TextView? =
        null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        val playlistId =
            arguments
                ?.getLong(PLAYLIST_ID_KEY)
                ?: INVALID_PLAYLIST_ID

        val btnBack =
            view.findViewById<ImageView>(
                R.id.btnBack
            )

        val ivPlaylistCover =
            view.findViewById<ImageView>(
                R.id.ivPlaylistCover
            )

        val tvPlaylistName =
            view.findViewById<TextView>(
                R.id.tvPlaylistName
            )

        val tvPlaylistDescription =
            view.findViewById<TextView>(
                R.id.tvPlaylistDescription
            )

        tvTracksCount =
            view.findViewById(
                R.id.tvTracksCount
            )

        val btnShare =
            view.findViewById<ImageButton>(
                R.id.btnShare
            )

        val btnMore =
            view.findViewById<ImageButton>(
                R.id.btnMore
            )

        val recyclerView =
            view.findViewById<RecyclerView>(
                R.id.rvPlaylistTracks
            )

        val tracksBottomSheet =
            view.findViewById<View>(
                R.id.playlistTracksBottomSheet
            )

        // =====================================================
        // BOTTOM SHEET
        // =====================================================

        val bottomSheetBehavior =
            BottomSheetBehavior.from(
                tracksBottomSheet
            )

        bottomSheetBehavior.isHideable =
            false

        bottomSheetBehavior.skipCollapsed =
            false

        bottomSheetBehavior.peekHeight =
            dpToPx(
                BOTTOM_SHEET_PEEK_HEIGHT
            )

        bottomSheetBehavior.state =
            BottomSheetBehavior.STATE_COLLAPSED

        // =====================================================
        // СПИСОК ТРЕКОВ
        // =====================================================

        tracksAdapter =
            PlaylistTrackAdapter(

                onTrackClick = { track ->

                    openTrack(
                        track
                    )
                },

                onTrackLongClick = { track ->

                    showDeleteTrackDialog(
                        track
                    )
                }
            )

        recyclerView.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        recyclerView.adapter =
            tracksAdapter

        // =====================================================
        // НАЗАД
        // =====================================================

        btnBack.setOnClickListener {

            returnToMediaLibrary()
        }

        // =====================================================
        // ДАННЫЕ ПЛЕЙЛИСТА
        // =====================================================

        viewModel
            .observePlaylist()
            .observe(
                viewLifecycleOwner
            ) { playlist ->

                playlist
                    ?: return@observe

                currentPlaylist =
                    playlist

                tvPlaylistName.text =
                    playlist.name

                val description =
                    playlist.description
                        .orEmpty()

                tvPlaylistDescription.text =
                    description

                tvPlaylistDescription.isVisible =
                    description.isNotBlank()

                updatePlaylistInfo()

                val imagePath =
                    playlist.imagePath

                if (
                    !imagePath.isNullOrBlank()
                ) {

                    Glide.with(this)
                        .load(
                            Uri.parse(
                                imagePath
                            )
                        )
                        .placeholder(
                            R.drawable.ic_placeholder
                        )
                        .error(
                            R.drawable.ic_placeholder
                        )
                        .into(
                            ivPlaylistCover
                        )

                } else {

                    ivPlaylistCover
                        .setImageResource(
                            R.drawable.ic_placeholder
                        )
                }
            }

        // =====================================================
        // ТРЕКИ
        // =====================================================

        viewModel
            .observeTracks()
            .observe(
                viewLifecycleOwner
            ) { tracks ->

                currentTracks =
                    tracks

                tracksAdapter
                    ?.updateTracks(
                        tracks
                    )

                updatePlaylistInfo()
            }

        // =====================================================
        // УДАЛЕНИЕ ПЛЕЙЛИСТА ЗАВЕРШЕНО
        // =====================================================

        viewModel
            .observePlaylistDeleted()
            .observe(
                viewLifecycleOwner
            ) { deleted ->

                if (!deleted) {
                    return@observe
                }

                viewModel
                    .playlistDeletedHandled()

                returnToMediaLibrary()
            }

        // =====================================================
        // ЗАГРУЗКА
        // =====================================================

        viewModel.loadPlaylist(
            playlistId
        )

        // =====================================================
        // SHARE
        // =====================================================

        btnShare.setOnClickListener {

            currentPlaylist
                ?.let { playlist ->

                    sharePlaylist(
                        playlist
                    )
                }
        }

        // =====================================================
        // ⋮
        // =====================================================

        btnMore.setOnClickListener {

            currentPlaylist
                ?.let { playlist ->

                    showPlaylistMenu(
                        playlist
                    )
                }
        }
    }

    // =========================================================
    // ОТКРЫТЬ ТРЕК
    // =========================================================

    private fun openTrack(
        track: Track
    ) {

        findNavController()
            .navigate(
                R.id.audioPlayerFragment,
                bundleOf(
                    AudioPlayerFragment
                        .TRACK_KEY
                            to track
                )
            )
    }

    // =========================================================
    // НАЗАД В MEDIA LIBRARY
    // =========================================================

    private fun returnToMediaLibrary() {

        val navController =
            findNavController()

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
    // УДАЛИТЬ ТРЕК
    // =========================================================

    private fun showDeleteTrackDialog(
        track: Track
    ) {

        val dialog =
            Dialog(requireContext())

        val dialogView =
            layoutInflater.inflate(
                R.layout.dialog_confirm_delete,
                null
            )

        dialog.setContentView(dialogView)

        val message =
            dialogView.findViewById<TextView>(
                R.id.tvDialogMessage
            )

        val btnNo =
            dialogView.findViewById<TextView>(
                R.id.btnDialogNo
            )

        val btnYes =
            dialogView.findViewById<TextView>(
                R.id.btnDialogYes
            )

        message.text =
            getString(
                R.string.delete_track_question
            )

        btnNo.setOnClickListener {

            dialog.dismiss()
        }

        btnYes.setOnClickListener {

            viewModel.deleteTrack(track)

            dialog.dismiss()
        }

        dialog.setOnShowListener {

            dialog.window?.apply {

                setBackgroundDrawable(
                    ColorDrawable(
                        Color.TRANSPARENT
                    )
                )

                setLayout(
                    dpToPx(280),
                    dpToPx(123)
                )

                setGravity(
                    Gravity.CENTER
                )

                addFlags(
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND
                )

                attributes =
                    attributes.apply {

                        dimAmount = 0.55f
                    }
            }
        }

        dialog.show()
    }

    // =========================================================
    // МЕНЮ ⋮
    // =========================================================

    private fun showPlaylistMenu(
        playlist: Playlist
    ) {

        val dialog =
            BottomSheetDialog(
                requireContext()
            )

        val contentView =
            layoutInflater.inflate(
                R.layout.bottom_sheet_playlist_options,
                null
            )

        dialog.setContentView(
            contentView
        )

        val playlistCover =
            contentView.findViewById<ImageView>(
                R.id.ivMenuPlaylistCover
            )

        val playlistName =
            contentView.findViewById<TextView>(
                R.id.tvMenuPlaylistName
            )

        val tracksCount =
            contentView.findViewById<TextView>(
                R.id.tvMenuTracksCount
            )

        val btnShare =
            contentView.findViewById<TextView>(
                R.id.btnMenuShare
            )

        val btnEdit =
            contentView.findViewById<TextView>(
                R.id.btnMenuEdit
            )

        val btnDelete =
            contentView.findViewById<TextView>(
                R.id.btnMenuDelete
            )

        // =====================================================
        // ИНФОРМАЦИЯ О ПЛЕЙЛИСТЕ
        // =====================================================

        playlistName.text =
            playlist.name

        tracksCount.text =
            getTracksCountText(
                playlist.tracksCount
            )

        val imagePath =
            playlist.imagePath

        if (
            !imagePath.isNullOrBlank()
        ) {

            Glide.with(this)
                .load(
                    Uri.parse(
                        imagePath
                    )
                )
                .placeholder(
                    R.drawable.ic_placeholder
                )
                .error(
                    R.drawable.ic_placeholder
                )
                .into(
                    playlistCover
                )

        } else {

            playlistCover
                .setImageResource(
                    R.drawable.ic_placeholder
                )
        }

        // =====================================================
        // LIGHT / DARK
        // =====================================================

        val darkMode =
            isDarkMode()

        val backgroundColor =
            if (darkMode) {

                Color.parseColor(
                    "#1A1B22"
                )

            } else {

                Color.WHITE
            }

        val primaryTextColor =
            if (darkMode) {

                Color.WHITE

            } else {

                Color.parseColor(
                    "#1A1B22"
                )
            }

        val secondaryTextColor =
            Color.parseColor(
                "#AEAFB4"
            )

        playlistName.setTextColor(
            primaryTextColor
        )

        tracksCount.setTextColor(
            secondaryTextColor
        )

        btnShare.setTextColor(
            primaryTextColor
        )

        btnEdit.setTextColor(
            primaryTextColor
        )

        btnDelete.setTextColor(
            primaryTextColor
        )

        val radius =
            dpToPx(
                BOTTOM_SHEET_RADIUS
            ).toFloat()

        contentView.background =
            GradientDrawable().apply {

                setColor(
                    backgroundColor
                )

                cornerRadii =
                    floatArrayOf(
                        radius,
                        radius,
                        radius,
                        radius,
                        0f,
                        0f,
                        0f,
                        0f
                    )
            }

        // =====================================================
        // SHARE
        // =====================================================

        btnShare.setOnClickListener {

            dialog.dismiss()

            sharePlaylist(
                playlist
            )
        }

        // =====================================================
        // EDIT
        // =====================================================

        btnEdit.setOnClickListener {

            dialog.dismiss()

            findNavController()
                .navigate(
                    R.id.createPlaylistFragment,
                    bundleOf(
                        CreatePlaylistFragment
                            .PLAYLIST_ID_KEY
                                to playlist.id
                    )
                )
        }

        // =====================================================
        // DELETE
        // =====================================================

        btnDelete.setOnClickListener {

            dialog.dismiss()

            showDeletePlaylistDialog(
                playlist
            )
        }

        dialog.setOnShowListener {

            dialog
                .findViewById<View>(
                    com.google.android.material
                        .R.id.design_bottom_sheet
                )
                ?.setBackgroundColor(
                    Color.TRANSPARENT
                )
        }

        dialog.show()
    }

    // =========================================================
    // УДАЛИТЬ ПЛЕЙЛИСТ
    // =========================================================

    private fun showDeletePlaylistDialog(
        playlist: Playlist
    ) {

        val dialog =
            Dialog(requireContext())

        val dialogView =
            layoutInflater.inflate(
                R.layout.dialog_confirm_delete,
                null
            )

        dialog.setContentView(dialogView)

        val message =
            dialogView.findViewById<TextView>(
                R.id.tvDialogMessage
            )

        val btnNo =
            dialogView.findViewById<TextView>(
                R.id.btnDialogNo
            )

        val btnYes =
            dialogView.findViewById<TextView>(
                R.id.btnDialogYes
            )

        message.text =
            getString(
                R.string.delete_playlist_question,
                playlist.name
            )

        btnNo.setOnClickListener {

            dialog.dismiss()
        }

        btnYes.setOnClickListener {

            viewModel.deletePlaylist()

            dialog.dismiss()
        }

        dialog.setOnShowListener {

            dialog.window?.apply {

                setBackgroundDrawable(
                    ColorDrawable(
                        Color.TRANSPARENT
                    )
                )

                setLayout(
                    dpToPx(280),
                    dpToPx(123)
                )

                setGravity(
                    Gravity.CENTER
                )

                addFlags(
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND
                )

                attributes =
                    attributes.apply {

                        dimAmount = 0.55f
                    }
            }
        }

        dialog.show()
    }
    // =========================================================
    // СТИЛЬ ALERT DIALOG
    // =========================================================

    private fun applyLightDialogStyle(
        dialog: AlertDialog
    ) {

        val blue =
            Color.parseColor(
                "#3772E7"
            )

        val darkText =
            Color.parseColor(
                "#1A1B22"
            )

        dialog
            .getButton(
                AlertDialog.BUTTON_NEGATIVE
            )
            ?.setTextColor(
                blue
            )

        dialog
            .getButton(
                AlertDialog.BUTTON_POSITIVE
            )
            ?.setTextColor(
                blue
            )

        dialog
            .findViewById<TextView>(
                android.R.id.message
            )
            ?.setTextColor(
                darkText
            )

        val background =
            GradientDrawable().apply {

                setColor(
                    Color.WHITE
                )

                cornerRadius =
                    dpToPx(
                        ALERT_DIALOG_RADIUS
                    ).toFloat()
            }

        dialog.window
            ?.setBackgroundDrawable(
                background
            )

        // Размер окна ближе к Figma

        dialog.window
            ?.setLayout(
                dpToPx(
                    ALERT_DIALOG_WIDTH
                ),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
    }

    // =========================================================
    // SHARE
    // =========================================================

    private fun sharePlaylist(
        playlist: Playlist
    ) {

        if (
            currentTracks.isEmpty()
        ) {

            Toast.makeText(
                requireContext(),
                getString(
                    R.string.playlist_share_empty
                ),
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val shareText =
            buildString {

                append(
                    playlist.name
                )

                if (
                    !playlist.description
                        .isNullOrBlank()
                ) {

                    append("\n")

                    append(
                        playlist.description
                    )
                }

                append("\n")

                append(
                    getTracksCountText(
                        currentTracks.size
                    )
                )

                append("\n\n")

                currentTracks
                    .forEachIndexed {
                            index,
                            track ->

                        val duration =
                            SimpleDateFormat(
                                TRACK_TIME_FORMAT,
                                Locale.getDefault()
                            ).format(
                                track.trackTimeMillis
                            )

                        append(
                            "${index + 1}. " +
                                    "${track.artistName} - " +
                                    "${track.trackName} " +
                                    "($duration)"
                        )

                        if (
                            index !=
                            currentTracks.lastIndex
                        ) {

                            append("\n")
                        }
                    }
            }

        val shareIntent =
            Intent(
                Intent.ACTION_SEND
            ).apply {

                type =
                    "text/plain"

                putExtra(
                    Intent.EXTRA_TEXT,
                    shareText
                )
            }

        startActivity(
            Intent.createChooser(
                shareIntent,
                null
            )
        )
    }

    // =========================================================
    // МИНУТЫ + ТРЕКИ
    // =========================================================

    private fun updatePlaylistInfo() {

        val textView =
            tvTracksCount
                ?: return

        if (
            currentTracks.isEmpty()
        ) {

            val count =
                currentPlaylist
                    ?.tracksCount
                    ?: 0

            textView.text =
                getTracksCountText(
                    count
                )

            return
        }

        val totalMilliseconds =
            currentTracks.sumOf { track ->

                track.trackTimeMillis
            }

        val totalMinutes =
            totalMilliseconds /
                    MILLISECONDS_IN_MINUTE

        textView.text =
            "${getMinutesText(totalMinutes)} • " +
                    getTracksCountText(
                        currentTracks.size
                    )
    }

    private fun getTracksCountText(
        count: Int
    ): String {

        return resources
            .getQuantityString(
                R.plurals.tracks_count,
                count,
                count
            )
    }

    private fun getMinutesText(
        minutes: Long
    ): String {

        val value =
            minutes.toInt()

        return resources
            .getQuantityString(
                R.plurals.minutes_count,
                value,
                value
            )
    }

    // =========================================================
    // DARK MODE
    // =========================================================

    private fun isDarkMode(): Boolean {

        val nightMode =
            resources
                .configuration
                .uiMode and
                    Configuration.UI_MODE_NIGHT_MASK

        return nightMode ==
                Configuration.UI_MODE_NIGHT_YES
    }

    // =========================================================
    // DP -> PX
    // =========================================================

    private fun dpToPx(
        dp: Int
    ): Int {

        return (
                dp *
                        resources
                            .displayMetrics
                            .density
                ).toInt()
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    override fun onDestroyView() {

        tracksAdapter =
            null

        currentPlaylist =
            null

        currentTracks =
            emptyList()

        tvTracksCount =
            null

        super.onDestroyView()
    }

    companion object {

        const val PLAYLIST_ID_KEY =
            "playlistId"

        private const val INVALID_PLAYLIST_ID =
            -1L

        private const val TRACK_TIME_FORMAT =
            "mm:ss"

        private const val MILLISECONDS_IN_MINUTE =
            60_000L


        private const val BOTTOM_SHEET_PEEK_HEIGHT = 275

        private const val BOTTOM_SHEET_RADIUS =
            16

        private const val ALERT_DIALOG_RADIUS =
            4

        private const val ALERT_DIALOG_WIDTH =
            280
    }
}