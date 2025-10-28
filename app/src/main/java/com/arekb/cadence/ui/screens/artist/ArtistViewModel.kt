package com.arekb.cadence.ui.screens.artist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.cadence.data.remote.dto.SimpleAlbumObject
import com.arekb.cadence.data.remote.dto.TopArtistObject
import com.arekb.cadence.data.remote.dto.TrackObject
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
        fetchArtistPageData()
    }

    private fun fetchArtistPageData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = searchRepository.getArtistPageData(artistId)

            result.onSuccess { pageData ->
                // On success, update the state with all the data from the bundle
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        artistDetails = pageData.details,
                        topTracks = pageData.topTracks?.items ?: emptyList(),
                        albums = pageData.albums?.items ?: emptyList()
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, error = error.message)
                }
            }
        }
    }
}

data class ArtistUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val artistDetails: TopArtistObject? = null,
    val topTracks: List<TrackObject> = emptyList(),
    val albums: List<SimpleAlbumObject> = emptyList()
)