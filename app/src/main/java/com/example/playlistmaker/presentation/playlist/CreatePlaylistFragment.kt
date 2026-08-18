package com.example.playlistmaker.presentation.playlist

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class CreatePlaylistFragment :
    Fragment(R.layout.fragment_create_playlist) {

    private val viewModel:
            CreatePlaylistViewModel by viewModel()

    // =========================================================
    // VIEW
    // =========================================================

    private var etName: EditText? = null

    private var etDescription: EditText? = null

    private var ivCover: ImageView? = null

    private var btnCreate: Button? = null

    // =========================================================
    // DATA
    // =========================================================

    private var selectedImagePath: String? = null

    /*
     * Если playlistId != -1,
     * экран открыт в режиме редактирования.
     */
    private var playlistId: Long =
        INVALID_PLAYLIST_ID

    private var isEditMode =
        false

    /*
     * Используется при создании
     * плейлиста из AudioPlayer.
     */
    private var trackToAdd: Track? =
        null

    /*
     * Не даём повторной эмиссии Flow
     * снова перезаписать введённые данные.
     */
    private var formInitialized =
        false

    // =========================================================
    // INITIAL VALUES
    // =========================================================

    private var initialName =
        ""

    private var initialDescription =
        ""

    private var initialImagePath: String? =
        null

    // =========================================================
    // IMAGE PICKER
    // =========================================================

    private val imagePicker =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->

            if (uri == null) {
                return@registerForActivityResult
            }

            val copiedImagePath =
                copyImageToPrivateStorage(
                    uri
                )

            if (copiedImagePath != null) {

                selectedImagePath =
                    copiedImagePath

                showSelectedImage()
            }
        }

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

        // =====================================================
        // ARGUMENTS
        // =====================================================

        playlistId =
            arguments
                ?.getLong(
                    PLAYLIST_ID_KEY
                )
                ?: INVALID_PLAYLIST_ID

        trackToAdd =
            arguments
                ?.getSerializable(
                    TRACK_KEY
                ) as? Track

        isEditMode =
            playlistId !=
                    INVALID_PLAYLIST_ID

        // =====================================================
        // FIND VIEW
        // =====================================================

        val btnBack =
            view.findViewById<ImageView>(
                R.id.btnBack
            )

        val tvTitle =
            view.findViewById<TextView>(
                R.id.tvTitle
            )

        ivCover =
            view.findViewById(
                R.id.ivCover
            )

        etName =
            view.findViewById(
                R.id.etName
            )

        etDescription =
            view.findViewById(
                R.id.etDescription
            )

        btnCreate =
            view.findViewById(
                R.id.btnCreate
            )

        // =====================================================
        // TITLE
        // =====================================================

        tvTitle.text =
            if (isEditMode) {

                getString(
                    R.string.edit_playlist_title
                )

            } else {

                getString(
                    R.string.new_playlist
                )
            }

        // =====================================================
        // BUTTON TEXT
        // =====================================================

        btnCreate?.text =
            if (isEditMode) {

                getString(
                    R.string.save
                )

            } else {

                getString(
                    R.string.create
                )
            }

        // =====================================================
        // RESTORE / LOAD
        // =====================================================

        when {

            savedInstanceState != null -> {

                restoreState(
                    savedInstanceState
                )

                formInitialized =
                    true
            }

            isEditMode -> {

                observePlaylist()

                viewModel.loadPlaylist(
                    playlistId
                )
            }

            else -> {

                initialName =
                    ""

                initialDescription =
                    ""

                initialImagePath =
                    null

                formInitialized =
                    true

                updateCreateButton()
            }
        }

        // =====================================================
        // NAME
        // =====================================================

        etName
            ?.doAfterTextChanged {

                updateCreateButton()
            }

        // =====================================================
        // COVER
        // =====================================================

        ivCover
            ?.setOnClickListener {

                imagePicker.launch(
                    "image/*"
                )
            }

        // =====================================================
        // CREATE / SAVE
        // =====================================================

        btnCreate
            ?.setOnClickListener {

                savePlaylist()
            }

        // =====================================================
        // BACK BUTTON
        // =====================================================

        btnBack.setOnClickListener {

            handleBackPressed()
        }

        // =====================================================
        // SYSTEM BACK / GESTURE
        // =====================================================

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                object :
                    OnBackPressedCallback(
                        true
                    ) {

                    override fun handleOnBackPressed() {

                        handleBackPressed()
                    }
                }
            )
    }

    // =========================================================
    // OBSERVE PLAYLIST
    // =========================================================

    private fun observePlaylist() {

        viewModel
            .observePlaylist()
            .observe(
                viewLifecycleOwner
            ) { playlist ->

                if (
                    playlist == null ||
                    formInitialized
                ) {
                    return@observe
                }

                // =================================================
                // ORIGINAL VALUES
                // =================================================

                initialName =
                    playlist.name

                initialDescription =
                    playlist.description
                        .orEmpty()

                initialImagePath =
                    playlist.imagePath

                // =================================================
                // CURRENT VALUES
                // =================================================

                selectedImagePath =
                    playlist.imagePath

                etName
                    ?.setText(
                        playlist.name
                    )

                etDescription
                    ?.setText(
                        playlist.description
                            .orEmpty()
                    )

                showSelectedImage()

                formInitialized =
                    true

                updateCreateButton()
            }
    }

    // =========================================================
    // SAVE
    // =========================================================

    private fun savePlaylist() {

        val name =
            etName
                ?.text
                ?.toString()
                ?.trim()
                .orEmpty()

        if (name.isBlank()) {
            return
        }

        val description =
            etDescription
                ?.text
                ?.toString()
                .orEmpty()

        if (isEditMode) {

            updatePlaylist(
                name = name,
                description = description
            )

        } else {

            createPlaylist(
                name = name,
                description = description
            )
        }
    }

    // =========================================================
    // CREATE PLAYLIST
    // =========================================================

    private fun createPlaylist(
        name: String,
        description: String
    ) {

        viewModel.createPlaylist(

            name = name,

            description =
                description,

            imagePath =
                selectedImagePath,

            trackToAdd =
                trackToAdd,

            // =================================================
            // DUPLICATE NAME
            // =================================================

            onNameExists = {

                if (!isAdded) {
                    return@createPlaylist
                }

                Toast.makeText(
                    requireContext(),
                    getString(
                        R.string.playlist_name_exists
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            },

            // =================================================
            // CREATED
            // =================================================

            onCreated = {

                if (!isAdded) {
                    return@createPlaylist
                }

                Toast.makeText(
                    requireContext(),
                    getString(
                        R.string.playlist_created,
                        name
                    ),
                    Toast.LENGTH_SHORT
                ).show()

                findNavController()
                    .popBackStack()
            }
        )
    }

    // =========================================================
    // UPDATE PLAYLIST
    // =========================================================

    private fun updatePlaylist(
        name: String,
        description: String
    ) {

        viewModel.updatePlaylist(

            playlistId =
                playlistId,

            name =
                name,

            description =
                description,

            imagePath =
                selectedImagePath,

            // =================================================
            // DUPLICATE NAME
            // =================================================

            onNameExists = {

                if (!isAdded) {
                    return@updatePlaylist
                }

                Toast.makeText(
                    requireContext(),
                    getString(
                        R.string.playlist_name_exists
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            },

            // =================================================
            // UPDATED
            // =================================================

            onUpdated = {

                if (!isAdded) {
                    return@updatePlaylist
                }

                Toast.makeText(
                    requireContext(),
                    getString(
                        R.string.playlist_saved,
                        name
                    ),
                    Toast.LENGTH_SHORT
                ).show()

                /*
                 * Возвращаемся на экран
                 * этого же плейлиста.
                 */
                findNavController()
                    .popBackStack()
            }
        )
    }

    // =========================================================
    // BUTTON STATE
    // =========================================================

    private fun updateCreateButton() {

        val isEnabled =
            etName
                ?.text
                ?.toString()
                ?.trim()
                ?.isNotEmpty() ==
                    true

        btnCreate
            ?.isEnabled =
            isEnabled

        btnCreate
            ?.backgroundTintList =
            android.content.res
                .ColorStateList
                .valueOf(
                    android.graphics.Color
                        .parseColor(
                            if (isEnabled) {

                                ENABLED_BUTTON_COLOR

                            } else {

                                DISABLED_BUTTON_COLOR
                            }
                        )
                )
    }

    // =========================================================
    // SHOW SELECTED IMAGE
    // =========================================================

    private fun showSelectedImage() {

        val cover =
            ivCover
                ?: return

        val path =
            selectedImagePath

        /*
         * Если обложки нет —
         * показываем стандартную кнопку
         * добавления обложки.
         */
        if (
            path.isNullOrBlank()
        ) {

            cover.scaleType =
                ImageView.ScaleType.CENTER

            cover.setImageResource(
                R.drawable.ic_add_playlist_cover
            )

            return
        }

        cover.scaleType =
            ImageView.ScaleType.CENTER_CROP

        Glide.with(this)
            .load(
                File(path)
            )
            .placeholder(
                R.drawable.ic_placeholder
            )
            .error(
                R.drawable.ic_placeholder
            )
            .into(
                cover
            )
    }

    // =========================================================
    // BACK
    // =========================================================

    private fun handleBackPressed() {

        /*
         * Sprint 23:
         *
         * В режиме редактирования
         * несохранённые изменения просто
         * отменяются.
         */
        if (isEditMode) {

            findNavController()
                .popBackStack()

            return
        }

        /*
         * Для создания нового плейлиста
         * сохраняем старое поведение.
         */
        if (
            hasUnsavedData()
        ) {

            showExitDialog()

        } else {

            findNavController()
                .popBackStack()
        }
    }

    // =========================================================
    // UNSAVED DATA
    // =========================================================

    private fun hasUnsavedData(): Boolean {

        val currentName =
            etName
                ?.text
                ?.toString()
                .orEmpty()

        val currentDescription =
            etDescription
                ?.text
                ?.toString()
                .orEmpty()

        return if (isEditMode) {

            currentName !=
                    initialName ||
                    currentDescription !=
                    initialDescription ||
                    selectedImagePath !=
                    initialImagePath

        } else {

            currentName
                .isNotBlank() ||
                    currentDescription
                        .isNotBlank() ||
                    selectedImagePath !=
                    null
        }
    }

    // =========================================================
    // EXIT DIALOG
    // =========================================================

    private fun showExitDialog() {

        AlertDialog.Builder(
            requireContext()
        )
            .setTitle(
                getString(
                    R.string.finish_playlist_creation
                )
            )
            .setMessage(
                getString(
                    R.string.unsaved_playlist_data
                )
            )
            .setNegativeButton(
                getString(
                    R.string.cancel
                ),
                null
            )
            .setPositiveButton(
                getString(
                    R.string.finish
                )
            ) { _, _ ->

                findNavController()
                    .popBackStack()
            }
            .show()
    }

    // =========================================================
    // COPY IMAGE
    // =========================================================

    private fun copyImageToPrivateStorage(
        sourceUri: Uri
    ): String? {

        return try {

            val directory =
                File(
                    requireContext()
                        .filesDir,
                    PLAYLIST_COVERS_DIRECTORY
                )

            if (
                !directory.exists()
            ) {

                directory.mkdirs()
            }

            val destinationFile =
                File(
                    directory,
                    "${UUID.randomUUID()}.jpg"
                )

            requireContext()
                .contentResolver
                .openInputStream(
                    sourceUri
                )
                ?.use { inputStream ->

                    FileOutputStream(
                        destinationFile
                    ).use { outputStream ->

                        inputStream
                            .copyTo(
                                outputStream
                            )
                    }
                }
                ?: return null

            destinationFile
                .absolutePath

        } catch (
            exception: Exception
        ) {

            null
        }
    }

    // =========================================================
    // SAVE INSTANCE STATE
    // =========================================================

    override fun onSaveInstanceState(
        outState: Bundle
    ) {

        super.onSaveInstanceState(
            outState
        )

        outState.putString(
            KEY_IMAGE_PATH,
            selectedImagePath
        )

        outState.putString(
            KEY_NAME,
            etName
                ?.text
                ?.toString()
                .orEmpty()
        )

        outState.putString(
            KEY_DESCRIPTION,
            etDescription
                ?.text
                ?.toString()
                .orEmpty()
        )

        outState.putString(
            KEY_INITIAL_NAME,
            initialName
        )

        outState.putString(
            KEY_INITIAL_DESCRIPTION,
            initialDescription
        )

        outState.putString(
            KEY_INITIAL_IMAGE_PATH,
            initialImagePath
        )
    }

    // =========================================================
    // RESTORE STATE
    // =========================================================

    private fun restoreState(
        savedInstanceState: Bundle
    ) {

        selectedImagePath =
            savedInstanceState
                .getString(
                    KEY_IMAGE_PATH
                )

        initialName =
            savedInstanceState
                .getString(
                    KEY_INITIAL_NAME
                )
                .orEmpty()

        initialDescription =
            savedInstanceState
                .getString(
                    KEY_INITIAL_DESCRIPTION
                )
                .orEmpty()

        initialImagePath =
            savedInstanceState
                .getString(
                    KEY_INITIAL_IMAGE_PATH
                )

        etName
            ?.setText(
                savedInstanceState
                    .getString(
                        KEY_NAME
                    )
                    .orEmpty()
            )

        etDescription
            ?.setText(
                savedInstanceState
                    .getString(
                        KEY_DESCRIPTION
                    )
                    .orEmpty()
            )

        showSelectedImage()

        updateCreateButton()
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    override fun onDestroyView() {

        etName =
            null

        etDescription =
            null

        ivCover =
            null

        btnCreate =
            null

        super.onDestroyView()
    }

    companion object {

        // Для режима редактирования
        const val PLAYLIST_ID_KEY =
            "playlistId"

        // Для передачи трека из AudioPlayer
        const val TRACK_KEY =
            "trackToAdd"

        private const val INVALID_PLAYLIST_ID =
            -1L

        private const val KEY_IMAGE_PATH =
            "KEY_IMAGE_PATH"

        private const val KEY_NAME =
            "KEY_NAME"

        private const val KEY_DESCRIPTION =
            "KEY_DESCRIPTION"

        private const val KEY_INITIAL_NAME =
            "KEY_INITIAL_NAME"

        private const val KEY_INITIAL_DESCRIPTION =
            "KEY_INITIAL_DESCRIPTION"

        private const val KEY_INITIAL_IMAGE_PATH =
            "KEY_INITIAL_IMAGE_PATH"

        private const val PLAYLIST_COVERS_DIRECTORY =
            "playlist_covers"

        private const val ENABLED_BUTTON_COLOR =
            "#3772E7"

        private const val DISABLED_BUTTON_COLOR =
            "#AEAFB4"

        fun newInstance() =
            CreatePlaylistFragment()
    }
}