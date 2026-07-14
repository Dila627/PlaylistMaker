package com.example.playlistmaker.presentation.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.player.AudioPlayerFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment(R.layout.fragment_search) {

    private val viewModel: SearchViewModel by viewModel()

    private var searchJob: Job? = null
    private var clickJob: Job? = null

    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var tracksRecyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter

    private lateinit var historyContainer: View
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var btnClearHistory: Button

    private lateinit var placeholderContainer: View
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderTitle: TextView
    private lateinit var placeholderText: TextView
    private lateinit var btnRetry: Button
    private lateinit var progressBar: ProgressBar

    private var searchText = ""
    private var isRestoring = false
    private var lastSearchText = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        initAdapters()
        initListeners()
        observeViewModel()
        restoreSearchText(savedInstanceState)
    }

    private fun initViews(view: View) {
        searchEditText = view.findViewById(R.id.etSearch)
        clearButton = view.findViewById(R.id.ivClear)
        tracksRecyclerView = view.findViewById(R.id.rvTracks)

        historyContainer = view.findViewById(R.id.historyContainer)
        historyRecyclerView = view.findViewById(R.id.rvHistory)
        btnClearHistory = view.findViewById(R.id.btnClearHistory)

        placeholderContainer = view.findViewById(R.id.placeholderContainer)
        placeholderImage = view.findViewById(R.id.placeholderImage)
        placeholderTitle = view.findViewById(R.id.placeholderTitle)
        placeholderText = view.findViewById(R.id.placeholderText)
        btnRetry = view.findViewById(R.id.btnRetry)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun initAdapters() {
        historyAdapter = TrackAdapter(mutableListOf()) { track ->
            viewModel.addTrackToHistory(track)
            viewModel.showHistory()
            openAudioPlayer(track)
        }

        historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyRecyclerView.adapter = historyAdapter

        trackAdapter = TrackAdapter(mutableListOf()) { track ->
            viewModel.addTrackToHistory(track)
            openAudioPlayer(track)
        }

        tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        tracksRecyclerView.adapter = trackAdapter
    }

    private fun initListeners() {
        btnRetry.setOnClickListener {
            if (lastSearchText.isNotBlank()) {
                performSearch(lastSearchText)
            }
        }

        btnClearHistory.setOnClickListener {
            viewModel.clearHistory()
            hideHistory()
        }

        searchEditText.setOnClickListener {
            if (searchEditText.text.isEmpty()) {
                showHistoryIfNeeded()
            }
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = searchEditText.text.toString().trim()

                if (text.isNotEmpty()) {
                    cancelSearchDebounce()
                    hideKeyboard(searchEditText)
                    performSearch(text)
                }

                true
            } else {
                false
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (isRestoring) return

                searchText = s?.toString().orEmpty()
                clearButton.isVisible = searchText.isNotEmpty()

                hideAllContent()

                if (searchText.isNotBlank()) {
                    searchDebounce(searchText)
                } else {
                    cancelSearchDebounce()

                    lastSearchText = ""
                    trackAdapter.updateTracks(emptyList())

                    if (searchEditText.hasFocus()) {
                        showHistoryIfNeeded()
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && searchEditText.text.isEmpty()) {
                showHistoryIfNeeded()
            } else {
                hideHistory()
            }
        }

        clearButton.setOnClickListener {
            cancelSearchDebounce()

            searchEditText.text.clear()
            hideKeyboard(searchEditText)
            searchEditText.clearFocus()

            searchText = ""
            lastSearchText = ""

            clearButton.isVisible = false
            trackAdapter.updateTracks(emptyList())
            hideAllContent()
        }
    }

    private fun observeViewModel() {
        viewModel.observeState().observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchState.Loading -> showLoading()

                is SearchState.Content -> {
                    trackAdapter.updateTracks(state.tracks)
                    showContent()
                }

                is SearchState.Empty -> {
                    trackAdapter.updateTracks(emptyList())
                    showEmptyPlaceholder()
                }

                is SearchState.Error -> {
                    showErrorPlaceholder()
                }

                is SearchState.History -> {
                    showHistory(state.tracks)
                }
            }
        }
    }

    private fun restoreSearchText(savedInstanceState: Bundle?) {
        savedInstanceState ?: return

        isRestoring = true

        val restoredText = savedInstanceState.getString(KEY_SEARCH_TEXT, "")

        searchEditText.setText(restoredText)
        searchEditText.setSelection(restoredText.length)

        clearButton.isVisible = restoredText.isNotEmpty()
        searchText = restoredText

        isRestoring = false
    }

    private fun performSearch(text: String) {
        lastSearchText = text
        viewModel.search(text)
    }

    private fun searchDebounce(changedText: String) {
        searchJob?.cancel()

        if (changedText.isBlank()) return

        searchJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)

            if (searchEditText.text.toString() == changedText) {
                performSearch(changedText)
            }
        }
    }

    private fun cancelSearchDebounce() {
        searchJob?.cancel()
        searchJob = null
    }

    private fun openAudioPlayer(track: Track) {
        if (clickJob?.isActive == true) return

        clickJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(CLICK_DEBOUNCE_DELAY)
        }

        findNavController().navigate(
            R.id.audioPlayerFragment,
            bundleOf(AudioPlayerFragment.TRACK_KEY to track)
        )
    }

    private fun showLoading() {
        historyContainer.isVisible = false
        placeholderContainer.isVisible = false
        tracksRecyclerView.isVisible = false
        progressBar.isVisible = true
    }

    private fun showContent() {
        tracksRecyclerView.isVisible = true
        placeholderContainer.isVisible = false
        historyContainer.isVisible = false
        progressBar.isVisible = false
    }

    private fun showEmptyPlaceholder() {
        tracksRecyclerView.isVisible = false
        historyContainer.isVisible = false
        progressBar.isVisible = false
        placeholderContainer.isVisible = true

        placeholderImage.setImageResource(R.drawable.ic_nothing_found)
        placeholderTitle.text = getString(R.string.nothing_found_title)
        placeholderText.isVisible = false
        btnRetry.isVisible = false
    }

    private fun showErrorPlaceholder() {
        tracksRecyclerView.isVisible = false
        historyContainer.isVisible = false
        progressBar.isVisible = false
        placeholderContainer.isVisible = true

        placeholderImage.setImageResource(R.drawable.ic_connection_error)
        placeholderTitle.text = getString(R.string.connection_error_title)
        placeholderText.text = getString(R.string.connection_error_text)

        placeholderText.isVisible = true
        btnRetry.isVisible = true
    }

    private fun showHistoryIfNeeded() {
        hideHistory()
        viewModel.showHistory()
    }

    private fun showHistory(history: List<Track>) {
        historyAdapter.updateTracks(history)

        historyContainer.isVisible = true
        tracksRecyclerView.isVisible = false
        placeholderContainer.isVisible = false
        progressBar.isVisible = false
    }

    private fun hideHistory() {
        historyContainer.isVisible = false
    }

    private fun hideAllContent() {
        placeholderContainer.isVisible = false
        tracksRecyclerView.isVisible = false
        historyContainer.isVisible = false
        progressBar.isVisible = false
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SEARCH_TEXT, searchText)
    }

    override fun onDestroyView() {
        searchJob?.cancel()
        clickJob?.cancel()

        searchJob = null
        clickJob = null

        tracksRecyclerView.adapter = null
        historyRecyclerView.adapter = null

        super.onDestroyView()
    }

    companion object {
        fun newInstance() = SearchFragment()

        private const val KEY_SEARCH_TEXT = "KEY_SEARCH_TEXT"
        private const val SEARCH_DEBOUNCE_DELAY = 2_000L
        private const val CLICK_DEBOUNCE_DELAY = 1_000L
    }
}