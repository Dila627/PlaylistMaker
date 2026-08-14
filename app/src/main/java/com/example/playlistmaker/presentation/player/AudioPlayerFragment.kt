package com.example.playlistmaker.presentation.player

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.core.os.bundleOf
import com.example.playlistmaker.presentation.playlist.CreatePlaylistFragment

class AudioPlayerFragment : Fragment(R.layout.fragment_audio_player) {

    private val viewModel: AudioPlayerViewModel by viewModel()

    private var btnPlay: ImageButton? = null
    private var btnPlaylist: ImageButton? = null
    private var btnLike: ImageButton? = null
    private var tvCurrentTime: TextView? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        val track =
            arguments?.getSerializable(TRACK_KEY) as? Track

        val btnBack =
            view.findViewById<ImageView>(
                R.id.btnBack
            )

        btnPlay =
            view.findViewById(
                R.id.btnPlay
            )

        btnPlaylist =
            view.findViewById(
                R.id.btnPlaylist
            )

        btnLike =
            view.findViewById(
                R.id.btnLike
            )

        val cover =
            view.findViewById<ImageView>(
                R.id.cover
            )

        val tvTrackName =
            view.findViewById<TextView>(
                R.id.tvTrackName
            )

        val tvArtist =
            view.findViewById<TextView>(
                R.id.tvArtist
            )

        tvCurrentTime =
            view.findViewById(
                R.id.tvCurrentTime
            )

        val tvDurationValue =
            view.findViewById<TextView>(
                R.id.tvDurationValue
            )

        val tvAlbumLabel =
            view.findViewById<TextView>(
                R.id.tvAlbumLabel
            )

        val tvAlbumValue =
            view.findViewById<TextView>(
                R.id.tvAlbumValue
            )

        val tvYearLabel =
            view.findViewById<TextView>(
                R.id.tvYearLabel
            )

        val tvYearValue =
            view.findViewById<TextView>(
                R.id.tvYearValue
            )

        val tvGenreValue =
            view.findViewById<TextView>(
                R.id.tvGenreValue
            )

        val tvCountryValue =
            view.findViewById<TextView>(
                R.id.tvCountryValue
            )

        // ============================================
        // НАЗАД
        // ============================================

        btnBack.setOnClickListener {

            findNavController()
                .popBackStack()
        }

        // ============================================
        // ИНФОРМАЦИЯ О ТРЕКЕ
        // ============================================

        tvTrackName.text =
            track?.trackName.orEmpty()

        tvArtist.text =
            track?.artistName.orEmpty()

        tvCurrentTime?.text =
            DEFAULT_TIME

        tvDurationValue.text =
            track
                ?.trackTimeMillis
                ?.let { duration ->

                    SimpleDateFormat(
                        TRACK_TIME_FORMAT,
                        Locale.getDefault()
                    ).format(
                        duration
                    )
                }
                .orEmpty()

        tvGenreValue.text =
            track?.primaryGenreName.orEmpty()

        tvCountryValue.text =
            track?.country.orEmpty()

        val album =
            track
                ?.collectionName
                .orEmpty()

        val year =
            track
                ?.releaseDate
                ?.take(
                    YEAR_LENGTH
                )
                .orEmpty()

        // Альбом

        tvAlbumLabel.isVisible =
            album.isNotBlank()

        tvAlbumValue.isVisible =
            album.isNotBlank()

        tvAlbumValue.text =
            album

        // Год

        tvYearLabel.isVisible =
            year.isNotBlank()

        tvYearValue.isVisible =
            year.isNotBlank()

        tvYearValue.text =
            year

        // ============================================
        // ОБЛОЖКА
        // ============================================

        Glide.with(this)
            .load(
                track?.getCoverArtwork()
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

        // ============================================
        // СОСТОЯНИЕ ПЛЕЕРА
        // ============================================

        observePlayerState()

        /*
         * ВАЖНО:
         *
         * observePlaylistAddResult()
         * здесь больше НЕ вызываем.
         *
         * Результат добавления обрабатывается
         * только внутри открытого BottomSheet.
         */

        // ============================================
        // PREPARE PLAYER
        // ============================================

        viewModel.preparePlayer(
            track?.previewUrl
        )

        // ============================================
        // PLAY / PAUSE
        // ============================================

        btnPlay?.setOnClickListener {

            viewModel.playbackControl()
        }

        // ============================================
        // ДОБАВИТЬ В ПЛЕЙЛИСТ
        // ============================================

        btnPlaylist?.setOnClickListener {

            track?.let { selectedTrack ->

                showPlaylistsBottomSheet(
                    selectedTrack
                )
            }
        }

        // ============================================
        // LIKE
        // ============================================

        var isLiked =
            false

        btnLike?.setOnClickListener {

            isLiked =
                !isLiked

            btnLike?.setImageResource(
                if (isLiked) {

                    R.drawable.filled_like_icon

                } else {

                    R.drawable.ic_playlist_like
                }
            )
        }
    }

    // =========================================================
    // PLAYER STATE
    // =========================================================

    private fun observePlayerState() {

        viewModel
            .observeState()
            .observe(
                viewLifecycleOwner
            ) { state ->

                tvCurrentTime?.text =
                    state.currentTime

                btnPlay?.setImageResource(
                    if (state.isPlaying) {

                        R.drawable.ic_playlist_pause

                    } else {

                        R.drawable.ic_playlist_play
                    }
                )
            }
    }

    // =========================================================
    // BOTTOM SHEET: ADD TO PLAYLIST
    // =========================================================

    private fun showPlaylistsBottomSheet(
        track: Track
    ) {

        val dialog =
            BottomSheetDialog(
                requireContext()
            )

        val contentView =
            layoutInflater.inflate(
                R.layout.bottom_sheet_playlists,
                null
            )

        dialog.setContentView(
            contentView
        )

        val recyclerView =
            contentView
                .findViewById<RecyclerView>(
                    R.id.rvPlaylists
                )

        val btnNewPlaylist =
            contentView
                .findViewById<View>(
                    R.id.btnNewPlaylist
                )

        // ============================================
        // АДАПТЕР
        // ============================================

        val adapter =
            PlaylistBottomSheetAdapter { playlist ->

                viewModel.addTrackToPlaylist(
                    track = track,
                    playlist = playlist
                )
            }

        recyclerView.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        recyclerView.adapter =
            adapter

        // ============================================
        // СПИСОК ПЛЕЙЛИСТОВ
        // ============================================

        val playlistsObserver =
            Observer<List<Playlist>> { playlists ->

                adapter.updatePlaylists(
                    playlists
                )
            }

        viewModel
            .observePlaylists()
            .observe(
                viewLifecycleOwner,
                playlistsObserver
            )

        // ============================================
        // РЕЗУЛЬТАТ ДОБАВЛЕНИЯ
        // ============================================

        val addResultObserver =
            Observer<PlaylistAddResult?> { result ->

                result
                    ?: return@Observer

                val message =
                    if (result.isAdded) {

                        "Добавлено в плейлист ${result.playlistName}"

                    } else {

                        "Трек уже добавлен в плейлист ${result.playlistName}"
                    }

                Toast.makeText(
                    requireContext(),
                    message,
                    Toast.LENGTH_SHORT
                ).show()

                /*
                 * ВАЖНО:
                 *
                 * После выбора любого плейлиста
                 * BottomSheet закрываем.
                 *
                 * И если трек добавился,
                 * и если он уже был добавлен.
                 */
                dialog.dismiss()

                /*
                 * Только после обработки
                 * очищаем событие.
                 */
                viewModel
                    .playlistAddResultHandled()
            }

        viewModel
            .observePlaylistAddResult()
            .observe(
                viewLifecycleOwner,
                addResultObserver
            )

        // ============================================
        // УДАЛЕНИЕ OBSERVERS ПРИ ЗАКРЫТИИ
        // ============================================

        dialog.setOnDismissListener {

            viewModel
                .observePlaylists()
                .removeObserver(
                    playlistsObserver
                )

            viewModel
                .observePlaylistAddResult()
                .removeObserver(
                    addResultObserver
                )
        }

        // ============================================
        // НОВЫЙ ПЛЕЙЛИСТ
        // ============================================

        btnNewPlaylist.setOnClickListener {

            dialog.dismiss()

            findNavController()
                .navigate(
                    R.id.createPlaylistFragment,
                    bundleOf(
                        CreatePlaylistFragment.TRACK_KEY
                                to track
                    )
                )
        }

        // ============================================
        // SHOW
        // ============================================

        dialog.show()
    }

    // =========================================================
    // PAUSE
    // =========================================================

    override fun onPause() {

        super.onPause()

        viewModel.pausePlayer()
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    override fun onDestroyView() {

        btnPlay
            ?.setOnClickListener(
                null
            )

        btnPlaylist
            ?.setOnClickListener(
                null
            )

        btnLike
            ?.setOnClickListener(
                null
            )

        btnPlay =
            null

        btnPlaylist =
            null

        btnLike =
            null

        tvCurrentTime =
            null

        super.onDestroyView()
    }

    companion object {

        const val TRACK_KEY =
            "track"

        private const val DEFAULT_TIME =
            "00:00"

        private const val TRACK_TIME_FORMAT =
            "mm:ss"

        private const val YEAR_LENGTH =
            4

        fun newInstance() =
            AudioPlayerFragment()
    }
}