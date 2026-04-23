package com.example.playlistmaker

import android.content.Context
import android.content.Intent
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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.search.SearchHistoryInteractor
import com.example.playlistmaker.domain.search.TracksInteractor
import com.example.playlistmaker.presentation.search.ui.TrackAdapter

class SearchActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var isClickAllowed = true

    private lateinit var tracksInteractor: TracksInteractor

    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var tracksRecyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter

    private lateinit var historyContainer: View
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var btnClearHistory: Button

    private lateinit var searchHistoryInteractor: SearchHistoryInteractor

    private lateinit var placeholderContainer: View
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderTitle: TextView
    private lateinit var placeholderText: TextView
    private lateinit var btnRetry: Button
    private lateinit var progressBar: ProgressBar

    private var searchText: String = ""
    private var isRestoring = false
    private var lastSearchText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        searchHistoryInteractor = Creator.provideSearchHistoryInteractor(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        tracksInteractor = Creator.provideTracksInteractor()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }

        initViews()
        initAdapters()
        initListeners()
    }

    private fun initViews() {
        searchEditText = findViewById(R.id.etSearch)
        clearButton = findViewById(R.id.ivClear)
        tracksRecyclerView = findViewById(R.id.rvTracks)

        historyContainer = findViewById(R.id.historyContainer)
        historyRecyclerView = findViewById(R.id.rvHistory)
        btnClearHistory = findViewById(R.id.btnClearHistory)

        placeholderContainer = findViewById(R.id.placeholderContainer)
        placeholderImage = findViewById(R.id.placeholderImage)
        placeholderTitle = findViewById(R.id.placeholderTitle)
        placeholderText = findViewById(R.id.placeholderText)
        btnRetry = findViewById(R.id.btnRetry)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun initAdapters() {
        historyAdapter = TrackAdapter(mutableListOf()) { track ->
            searchHistoryInteractor.addTrack(track)
            showHistoryIfNeeded()
            openAudioPlayer(track)
        }
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

        trackAdapter = TrackAdapter(mutableListOf()) { track ->
            searchHistoryInteractor.addTrack(track)
            openAudioPlayer(track)
        }
        tracksRecyclerView.layoutManager = LinearLayoutManager(this)
        tracksRecyclerView.adapter = trackAdapter
    }

    private fun initListeners() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        btnRetry.setOnClickListener {
            if (lastSearchText.isNotBlank()) {
                performSearch(lastSearchText)
            }
        }

        btnClearHistory.setOnClickListener {
            searchHistoryInteractor.clearHistory()
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

        if (searchEditText.hasFocus() && searchEditText.text.isEmpty()) {
            showHistoryIfNeeded()
        }
    }

    override fun onDestroy() {
        searchRunnable?.let { handler.removeCallbacks(it) }
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SEARCH_TEXT, searchText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isRestoring = true
        val value = savedInstanceState.getString(KEY_SEARCH_TEXT, "")
        searchEditText.setText(value)
        searchEditText.setSelection(value.length)
        clearButton.visibility = if (value.isEmpty()) View.GONE else View.VISIBLE
        searchText = value
        isRestoring = false
    }

    private fun performSearch(text: String) {
        lastSearchText = text

        historyContainer.visibility = View.GONE
        placeholderContainer.visibility = View.GONE
        tracksRecyclerView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        tracksInteractor.searchTracks(text) { result ->

            progressBar.visibility = View.GONE

            if (result.isError) {
                showErrorPlaceholder()
            } else if (result.tracks.isEmpty()) {
                trackAdapter.updateTracks(emptyList())
                showEmptyPlaceholder()
            } else {
                trackAdapter.updateTracks(result.tracks)
                showContent(true)
            }
        }
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
        val history = searchHistoryInteractor.getHistory()
        if (history.isNotEmpty()) {
            historyAdapter.updateTracks(history)
            historyContainer.visibility = View.VISIBLE
            tracksRecyclerView.visibility = View.GONE
            placeholderContainer.visibility = View.GONE
            progressBar.visibility = View.GONE
        } else {
            hideHistory()
        }
    }

    private fun hideHistory() {
        historyContainer.visibility = View.GONE
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun openAudioPlayer(track: Track) {
        if (!clickDebounce()) return

        val intent = Intent(this, AudioPlayerActivity::class.java)
        intent.putExtra(AudioPlayerActivity.TRACK_KEY, track)
        startActivity(intent)
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
        placeholderContainer.visibility = View.VISIBLE

        placeholderImage.setImageResource(R.drawable.ic_connection_error)
        placeholderTitle.text = getString(R.string.connection_error_title)

        placeholderText.visibility = View.VISIBLE
        placeholderText.text = getString(R.string.connection_error_text)

        btnRetry.visibility = View.VISIBLE
    }
    companion object {
        private const val KEY_SEARCH_TEXT = "KEY_SEARCH_TEXT"
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}