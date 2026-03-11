package com.example.playlistmaker

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
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.model.Track
import com.example.playlistmaker.network.RetrofitClient
import com.example.playlistmaker.network.SearchResponse
import com.example.playlistmaker.ui.search.SearchHistory
import com.example.playlistmaker.ui.search.TrackAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class SearchActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var tracksRecyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter

    private lateinit var historyContainer: View
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var btnClearHistory: Button

    private lateinit var searchHistory: SearchHistory

    private lateinit var placeholderContainer: View
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderTitle: TextView
    private lateinit var placeholderText: TextView
    private lateinit var btnRetry: Button

    private var searchText: String = ""
    private var isRestoring = false
    private var lastSearchText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }

        val backButton = findViewById<ImageView>(R.id.btnBack)
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

        val prefs = getSharedPreferences("playlist_maker_prefs", MODE_PRIVATE)
        searchHistory = SearchHistory(prefs)

        historyAdapter = TrackAdapter(mutableListOf()) { track ->
            searchHistory.addTrack(track)
            showHistoryIfNeeded()
        }
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

        trackAdapter = TrackAdapter(mutableListOf()) { track ->
            searchHistory.addTrack(track)
        }
        tracksRecyclerView.layoutManager = LinearLayoutManager(this)
        tracksRecyclerView.adapter = trackAdapter

        backButton.setOnClickListener { finish() }

        btnRetry.setOnClickListener {
            if (lastSearchText.isNotBlank()) performSearch(lastSearchText)
        }

        btnClearHistory.setOnClickListener {
            searchHistory.clear()
            hideHistory()
        }

        // ВАЖНО: если фокус уже стоит и поле пустое — по клику покажем историю
        searchEditText.setOnClickListener {
            if (searchEditText.text.isEmpty()) {
                showHistoryIfNeeded()
            }
        }

        // Done -> поиск
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = searchEditText.text.toString().trim()
                if (text.isNotEmpty()) {
                    hideKeyboard(searchEditText)
                    performSearch(text)
                }
                true
            } else false
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isRestoring) return

                searchText = s?.toString().orEmpty()
                clearButton.visibility = if (searchText.isEmpty()) View.GONE else View.VISIBLE

                placeholderContainer.visibility = View.GONE
                tracksRecyclerView.visibility = View.GONE
                historyContainer.visibility = View.GONE

                searchRunnable?.let { handler.removeCallbacks(it) }

                if (searchText.isNotBlank()) {
                    val runnable = Runnable { performSearch(searchText) }
                    searchRunnable = runnable
                    handler.postDelayed(runnable, SEARCH_DEBOUNCE_DELAY)
                } else {
                    searchRunnable = null
                    lastSearchText = ""
                    trackAdapter.updateTracks(emptyList())

                    if (searchEditText.hasFocus()) {
                        showHistoryIfNeeded()
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
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

            searchText = ""
            lastSearchText = ""
            trackAdapter.updateTracks(emptyList())

            searchRunnable?.let { handler.removeCallbacks(it) }
            searchRunnable = null
        }

        // Если экран открылся с фокусом и пустым полем — покажем историю сразу
        if (searchEditText.hasFocus() && searchEditText.text.isEmpty()) {
            showHistoryIfNeeded()
        }
    }

    override fun onDestroy() {
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

        RetrofitClient.itunesApi.search(text)
            .enqueue(object : Callback<SearchResponse> {
                override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                    if (response.code() == 200) {
                        val results = response.body()?.results.orEmpty()
                        val mappedTracks = results.map { dto ->
                            Track(
                                trackId = dto.trackId ?: 0L,
                                trackName = dto.trackName ?: "",
                                artistName = dto.artistName ?: "",
                                trackTime = SimpleDateFormat("mm:ss", Locale.getDefault())
                                    .format(dto.trackTimeMillis ?: 0L),
                                artworkUrl100 = dto.artworkUrl100 ?: ""
                            )
                        }

                        if (mappedTracks.isEmpty()) {
                            trackAdapter.updateTracks(emptyList())
                            showEmptyPlaceholder()
                        } else {
                            trackAdapter.updateTracks(mappedTracks)
                            showContent(true)
                        }
                    } else {
                        showErrorPlaceholder()
                    }
                }

                override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                    showErrorPlaceholder()
                }
            })
    }

    private fun showContent(isVisible: Boolean) {
        tracksRecyclerView.visibility = if (isVisible) View.VISIBLE else View.GONE
        placeholderContainer.visibility = View.GONE
        historyContainer.visibility = View.GONE
    }

    private fun showEmptyPlaceholder() {
        tracksRecyclerView.visibility = View.GONE
        historyContainer.isVisible = false
        placeholderContainer.isVisible = true

        placeholderImage.setImageResource(R.drawable.ic_nothing_found)
        placeholderTitle.text = getString(R.string.nothing_found_title)

        placeholderText.visibility = View.GONE
        btnRetry.visibility = View.GONE
    }

    private fun showErrorPlaceholder() {
        tracksRecyclerView.visibility = View.GONE
        historyContainer.isVisible = false
        placeholderContainer.isVisible = true

        placeholderImage.setImageResource(R.drawable.ic_connection_error)
        placeholderTitle.text = getString(R.string.connection_error_title)

        placeholderText.visibility = View.VISIBLE
        placeholderText.text = getString(R.string.connection_error_text)

        btnRetry.visibility = View.VISIBLE
    }

    private fun showHistoryIfNeeded() {
        val history = searchHistory.getHistory()
        if (history.isNotEmpty()) {
            historyAdapter.updateTracks(history)
            historyContainer.visibility = View.VISIBLE

            tracksRecyclerView.visibility = View.GONE
            placeholderContainer.visibility = View.GONE
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

    companion object {
        private const val KEY_SEARCH_TEXT = "KEY_SEARCH_TEXT"
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}