package com.example.playlistmaker.presentation.playlist

import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class PlaylistFragment :
    Fragment(R.layout.fragment_playlist) {

    private val viewModel:
            PlaylistViewModel by viewModel()

    private var tracksAdapter:
            PlaylistTrackAdapter? = null

    private var currentPlaylist:
            Playlist? = null

    private var currentTracks:
            List<Track> = emptyList()

    private var tvTracksCount:
            TextView? = null

    // =========================================================
    // ON VIEW CREATED
    // =========================================================

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
                ?.getLong(
                    PLAYLIST_ID_KEY
                )
                ?: INVALID_PLAYLIST_ID

        // =====================================================
        // VIEWS
        // =====================================================

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
        // TRACK ADAPTER
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
        // BACK
        // =====================================================

        btnBack.setOnClickListener {

            returnToMediaLibrary()
        }

        // =====================================================
        // PLAYLIST
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

                // =============================================
                // COVER
                // =============================================

                showPlaylistCover(
                    imageView = ivPlaylistCover,
                    imagePath = playlist.imagePath
                )
            }

        // =====================================================
        // TRACKS
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
        // PLAYLIST DELETED
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
        // LOAD
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
        // MORE
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
    // SHOW PLAYLIST COVER
    // =========================================================

    private fun showPlaylistCover(
        imageView: ImageView,
        imagePath: String?
    ) {

        if (
            imagePath.isNullOrBlank()
        ) {

            imageView.setImageResource(
                R.drawable.ic_placeholder
            )

            return
        }

        Glide.with(this)
            .load(
                File(imagePath)
            )
            .placeholder(
                R.drawable.ic_placeholder
            )
            .error(
                R.drawable.ic_placeholder
            )
            .into(
                imageView
            )
    }

    // =========================================================
    // OPEN TRACK
    // =========================================================

    private fun openTrack(
        track: Track
    ) {

        findNavController()
            .navigate(
                R.id.audioPlayerFragment,
                bundleOf(
                    AudioPlayerFragment.TRACK_KEY
                            to track
                )
            )
    }

    // =========================================================
    // RETURN TO MEDIA LIBRARY
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
                ?.id != R.id.mediaLibraryFragment
        ) {

            navController.navigate(
                R.id.mediaLibraryFragment
            )
        }
    }

    // =========================================================
    // DELETE TRACK DIALOG
    // =========================================================

    private fun showDeleteTrackDialog(
        track: Track
    ) {

        val dialog =
            Dialog(
                requireContext()
            )

        val dialogView =
            layoutInflater.inflate(
                R.layout.dialog_confirm_delete,
                null
            )

        dialog.setContentView(
            dialogView
        )

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

            viewModel.deleteTrack(
                track
            )

            dialog.dismiss()
        }

        dialog.setOnShowListener {

            configureConfirmDialog(
                dialog
            )
        }

        dialog.show()
    }

    // =========================================================
    // PLAYLIST MENU
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
        // PLAYLIST INFO
        // =====================================================

        playlistName.text =
            playlist.name

        tracksCount.text =
            getTracksCountText(
                playlist.tracksCount
            )

        // Здесь тоже File(imagePath),
        // а не Uri.parse(imagePath)
        showPlaylistCover(
            imageView = playlistCover,
            imagePath = playlist.imagePath
        )

        // =====================================================
        // LIGHT / DARK MODE
        // =====================================================

        val darkMode =
            isDarkMode()

        val backgroundColor =
            if (darkMode) {

                Color.parseColor(
                    DARK_BACKGROUND
                )

            } else {

                Color.WHITE
            }

        val primaryTextColor =
            if (darkMode) {

                Color.WHITE

            } else {

                Color.parseColor(
                    DARK_TEXT
                )
            }

        val secondaryTextColor =
            Color.parseColor(
                SECONDARY_TEXT
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

        // =====================================================
        // TRANSPARENT MATERIAL CONTAINER
        // =====================================================

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
    // DELETE PLAYLIST DIALOG
    // =========================================================

    private fun showDeletePlaylistDialog(
        playlist: Playlist
    ) {

        val dialog =
            Dialog(
                requireContext()
            )

        val dialogView =
            layoutInflater.inflate(
                R.layout.dialog_confirm_delete,
                null
            )

        dialog.setContentView(
            dialogView
        )

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

            configureConfirmDialog(
                dialog
            )
        }

        dialog.show()
    }

    // =========================================================
    // CONFIRM DIALOG STYLE
    // =========================================================

    private fun configureConfirmDialog(
        dialog: Dialog
    ) {

        dialog.window
            ?.apply {

                setBackgroundDrawable(
                    ColorDrawable(
                        Color.TRANSPARENT
                    )
                )

                setLayout(
                    dpToPx(
                        CONFIRM_DIALOG_WIDTH
                    ),
                    dpToPx(
                        CONFIRM_DIALOG_HEIGHT
                    )
                )

                setGravity(
                    Gravity.CENTER
                )

                addFlags(
                    WindowManager
                        .LayoutParams
                        .FLAG_DIM_BEHIND
                )

                attributes =
                    attributes.apply {

                        dimAmount =
                            DIALOG_DIM_AMOUNT
                    }
            }
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
    // MINUTES + TRACKS
    // =========================================================

    private fun updatePlaylistInfo() {

        val textView =
            tvTracksCount
                ?: return

        /*
         * Пока список треков ещё не загрузился,
         * отображаем хотя бы сохранённый
         * tracksCount из Playlist.
         */
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

    // =========================================================
    // PLURALS: TRACKS
    // =========================================================

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

    // =========================================================
    // PLURALS: MINUTES
    // =========================================================

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

        private const val BOTTOM_SHEET_PEEK_HEIGHT =
            275

        private const val BOTTOM_SHEET_RADIUS =
            16

        private const val CONFIRM_DIALOG_WIDTH =
            280

        private const val CONFIRM_DIALOG_HEIGHT =
            123

        private const val DIALOG_DIM_AMOUNT =
            0.55f

        private const val DARK_BACKGROUND =
            "#1A1B22"

        private const val DARK_TEXT =
            "#1A1B22"

        private const val SECONDARY_TEXT =
            "#AEAFB4"
    }
}