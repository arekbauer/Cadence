package com.arekb.cadence.ui.screens.artist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.cadence.data.remote.paging.SearchResult
import com.arekb.cadence.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArtistUiState())
    val uiState = _uiState.asStateFlow()

    // Get the artistId from the navigation arguments
    private val artistId: String = savedStateHandle["artistId"]!!

    init {
        // Use the ID to fetch the artist's full details
        fetchArtistDetails()
    }

    private fun fetchArtistDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            findSpecificArtist(artistId)
        }
    }

    //TODO: Need to use a different API endpoint, does not search via IDs
    fun findSpecificArtist(artistId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = searchRepository.findArtistById(artistId)

            result.onSuccess { foundArtist ->
                if (foundArtist != null) {
                    _uiState.update { it.copy(isLoading = false, artistDetails = foundArtist) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Artist not found.") }
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }
}

data class ArtistUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val artistDetails: SearchResult? = null
)