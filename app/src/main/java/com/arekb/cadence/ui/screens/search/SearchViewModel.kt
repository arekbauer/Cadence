package com.arekb.cadence.ui.screens.search

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arekb.cadence.core.model.Artist
import com.arekb.cadence.data.remote.paging.SearchResult
import com.arekb.cadence.data.repository.SearchRepository
import com.arekb.cadence.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchFilter(val label: String, val icon: ImageVector) {
    BOTH("Both", Icons.Default.Search),
    ARTIST("Artist", Icons.Default.Person),
    ALBUM("Album", Icons.Default.Album)
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(SearchFilter.BOTH)
    val selectedFilter: StateFlow<SearchFilter> = _selectedFilter.asStateFlow()

    private val _artistSuggestions = MutableStateFlow<List<Artist>>(emptyList())
    val artistSuggestions: StateFlow<List<Artist>> = _artistSuggestions.asStateFlow()

    init {
        // Fetch the suggestions when the ViewModel is created.
        fetchTopArtistSuggestions()
    }

    /**
     * Fetches the top artists for the "short_term" time range IF in cache already
     */
    private fun fetchTopArtistSuggestions() {
        viewModelScope.launch {
            val result = userRepository.getTopArtists("short_term", 10).first()
            result.onSuccess { artists ->
                _artistSuggestions.value = artists ?: emptyList()
            }
        }
    }

    fun onSuggestionClicked(artistName: String) {
        _searchQuery.value = artistName
    }

    @OptIn(FlowPreview::class)
    val searchResults: Flow<PagingData<SearchResult>> =
        combine(_searchQuery, _selectedFilter) { query, filter ->
            Pair(query, filter)
        }
        .debounce(300)
        .filter { (query, _) -> query.isNotBlank() }
        .flatMapLatest { (query, filter) ->
            val typeString = when (filter) {
                SearchFilter.BOTH -> "artist,album"
                SearchFilter.ARTIST -> "artist"
                SearchFilter.ALBUM -> "album"
            }
            searchRepository.getSearchResultsStream(query, typeString)
        }
        .cachedIn(viewModelScope)

    fun onQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(filter: SearchFilter) {
        _selectedFilter.value = filter
    }

}