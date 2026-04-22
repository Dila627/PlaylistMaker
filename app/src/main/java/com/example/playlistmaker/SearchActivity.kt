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
import android.widget.*
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
import java.util.*

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

    private lateinit var progressBar: ProgressBar

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

        val prefs = getSharedPreferences("playlist_maker_prefs", MODE_PRIVATE)
        searchHistory = SearchHistory(prefs)
    }

    private fun initAdapters() {
        historyAdapter = TrackAdapter(mutableListOf()) { track ->
            searchHistory.addTrack(track)
            openAudioPlayer(track)
        }

        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

        trackAdapter = TrackAdapter(mutableListOf()) { track ->
            searchHistory.addTrack(track)
            openAudioPlayer(track)
        }

        tracksRecyclerView.layoutManager = LinearLayoutManager(this)
        tracksRecyclerView.adapter = trackAdapter
    }

    private fun initListeners() {

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        btnRetry.setOnClickListener {
            if (lastSearchText.isNotBlank()) performSearch(lastSearchText)
        }

        btnClearHistory.setOnClickListener {
            searchHistory.clear()
            hideHistory()
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch(searchEditText.text.toString())
                true
            } else false
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (isRestoring) return

                searchText = s?.toString().orEmpty()
                clearButton.isVisible = searchText.isNotEmpty()

                searchRunnable?.let { handler.removeCallbacks(it) }

                if (searchText.isNotBlank()) {
                    searchDebounce(searchText)
                } else {
                    clearResults()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        clearButton.setOnClickListener {
            searchEditText.text.clear()
            clearResults()
        }
    }

    private fun searchDebounce(text: String) {
        val runnable = Runnable {
            performSearch(text)
        }
        searchRunnable = runnable
        handler.postDelayed(runnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun performSearch(text: String) {
        lastSearchText = text

        showLoading()

        RetrofitClient.itunesApi.search(text)
            .enqueue(object : Callback<SearchResponse> {

                override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                    hideLoading()

                    if (response.isSuccessful) {
                        val tracks = response.body()?.results.orEmpty().map { dto ->
                            Track(
                                trackId = dto.trackId ?: 0L,
                                trackName = dto.trackName ?: "",
                                artistName = dto.artistName ?: "",
                                trackTime = SimpleDateFormat("mm:ss", Locale.getDefault())
                                    .format(dto.trackTimeMillis ?: 0L),
                                artworkUrl100 = dto.artworkUrl100 ?: "",
                                collectionName = dto.collectionName,
                                releaseDate = dto.releaseDate,
                                primaryGenreName = dto.primaryGenreName,
                                country = dto.country,
                                previewUrl = dto.previewUrl
                            )
                        }

                        if (tracks.isEmpty()) {
                            showEmpty()
                        } else {
                            showTracks(tracks)
                        }

                    } else {
                        showError()
                    }
                }

                override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                    hideLoading()
                    showError()
                }
            })
    }

    private fun showLoading() {
        progressBar.isVisible = true
        tracksRecyclerView.isVisible = false
        placeholderContainer.isVisible = false
        historyContainer.isVisible = false
    }

    private fun hideLoading() {
        progressBar.isVisible = false
    }

    private fun showTracks(tracks: List<Track>) {
        trackAdapter.updateTracks(tracks)
        tracksRecyclerView.isVisible = true
        placeholderContainer.isVisible = false
    }

    private fun showEmpty() {
        placeholderContainer.isVisible = true
        placeholderImage.setImageResource(R.drawable.ic_nothing_found)
        placeholderTitle.text = getString(R.string.nothing_found_title)
        placeholderText.isVisible = false
        btnRetry.isVisible = false
    }

    private fun showError() {
        placeholderContainer.isVisible = true
        placeholderImage.setImageResource(R.drawable.ic_connection_error)
        placeholderTitle.text = getString(R.string.connection_error_title)
        placeholderText.isVisible = true
        btnRetry.isVisible = true
    }

    private fun clearResults() {
        trackAdapter.updateTracks(emptyList())
        tracksRecyclerView.isVisible = false
        placeholderContainer.isVisible = false
        progressBar.isVisible = false
    }

    private fun hideHistory() {
        historyContainer.isVisible = false
    }

    private fun openAudioPlayer(track: Track) {
        val intent = Intent(this, AudioPlayerActivity::class.java)
        intent.putExtra(AudioPlayerActivity.TRACK_KEY, track)
        startActivity(intent)
    }

    override fun onDestroy() {
        searchRunnable?.let { handler.removeCallbacks(it) }
        super.onDestroy()
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}