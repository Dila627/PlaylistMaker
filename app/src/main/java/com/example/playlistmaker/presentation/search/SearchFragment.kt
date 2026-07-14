package com.example.playlistmaker.presentation.search

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.player.AudioPlayerActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment(R.layout.fragment_search) {

    private val viewModel: SearchViewModel by viewModel()

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var isClickAllowed = true

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

    private var searchText: String = ""
    private var isRestoring = false
    private var lastSearchText: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        initAdapters()
        initListeners()
        observeViewModel()

        savedInstanceState?.let {
            isRestoring = true
            val value = it.getString(KEY_SEARCH_TEXT, "")
            searchEditText.setText(value)
            searchEditText.setSelection(value.length)
            clearButton.visibility = if (value.isEmpty()) View.GONE else View.VISIBLE
            searchText = value
            isRestoring = false
        }
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
                    searchRunnable?.let { handler.removeCallbacks(it) }
                    searchRunnable = null
                    hideKeyboard(searchEditText)
                    performSearch(text)
                }
                true
            } else {
                false
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isRestoring) return

                searchText = s?.toString().orEmpty()
                clearButton.visibility = if (searchText.isEmpty()) View.GONE else View.VISIBLE

                placeholderContainer.visibility = View.GONE
                tracksRecyclerView.visibility = View.GONE
                historyContainer.visibility = View.GONE
                progressBar.visibility = View.GONE

                if (searchText.isNotBlank()) {
                    searchDebounce(searchText)
                } else {
                    searchRunnable?.let { handler.removeCallbacks(it) }
                    searchRunnable = null
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
            searchEditText.text.clear()
            hideKeyboard(searchEditText)
            searchEditText.clearFocus()

            clearButton.visibility = View.GONE
            placeholderContainer.visibility = View.GONE
            tracksRecyclerView.visibility = View.GONE
            historyContainer.visibility = View.GONE
            progressBar.visibility = View.GONE

            searchText = ""
            lastSearchText = ""
            trackAdapter.updateTracks(emptyList())

            searchRunnable?.let { handler.removeCallbacks(it) }
            searchRunnable = null
        }
    }

    private fun observeViewModel() {
        viewModel.observeState().observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchState.Loading -> showLoading()
                is SearchState.Content -> {
                    trackAdapter.updateTracks(state.tracks)
                    showContent(true)
                }
                is SearchState.Empty -> {
                    trackAdapter.updateTracks(emptyList())
                    showEmptyPlaceholder()
                }
                is SearchState.Error -> showErrorPlaceholder()
                is SearchState.History -> showHistory(state.tracks)
            }
        }
    }

    private fun performSearch(text: String) {
        lastSearchText = text
        viewModel.search(text)
    }

    private fun openAudioPlayer(track: Track) {
        if (!clickDebounce()) return

        findNavController().navigate(
            R.id.audioPlayerFragment,
            bundleOf(AudioPlayerActivity.TRACK_KEY to track)
        )
    }

    private fun showLoading() {
        historyContainer.visibility = View.GONE
        placeholderContainer.visibility = View.GONE
        tracksRecyclerView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    private fun showContent(isVisible: Boolean) {
        tracksRecyclerView.visibility = if (isVisible) View.VISIBLE else View.GONE
        placeholderContainer.visibility = View.GONE
        historyContainer.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun showEmptyPlaceholder() {
        tracksRecyclerView.visibility = View.GONE
        historyContainer.isVisible = false
        progressBar.isVisible = false
        placeholderContainer.isVisible = true

        placeholderImage.setImageResource(R.drawable.ic_nothing_found)
        placeholderTitle.text = getString(R.string.nothing_found_title)
        placeholderText.visibility = View.GONE
        btnRetry.visibility = View.GONE
    }

    private fun showHistoryIfNeeded() {
        hideHistory()
        viewModel.showHistory()
    }

    private fun showHistory(history: List<Track>) {
        historyAdapter.updateTracks(history)
        historyContainer.visibility = View.VISIBLE
        tracksRecyclerView.visibility = View.GONE
        placeholderContainer.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun hideHistory() {
        historyContainer.visibility = View.GONE
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun searchDebounce(changedText: String) {
        searchRunnable?.let { handler.removeCallbacks(it) }
        if (changedText.isBlank()) return

        val runnable = Runnable { performSearch(changedText) }
        searchRunnable = runnable
        handler.postDelayed(runnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun showErrorPlaceholder() {
        tracksRecyclerView.visibility = View.GONE
        historyContainer.visibility = View.GONE
        progressBar.visibility = View.GONE
        placeholderContainer.visibility = View.VISIBLE

        placeholderImage.setImageResource(R.drawable.ic_connection_error)
        placeholderTitle.text = getString(R.string.connection_error_title)

        placeholderText.visibility = View.VISIBLE
        placeholderText.text = getString(R.string.connection_error_text)

        btnRetry.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        searchRunnable?.let { handler.removeCallbacks(it) }
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SEARCH_TEXT, searchText)
    }

    companion object {
        fun newInstance() = SearchFragment()

        private const val KEY_SEARCH_TEXT = "KEY_SEARCH_TEXT"
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}