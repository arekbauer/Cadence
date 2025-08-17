package com.arekb.cadence.ui.screens.stats.artists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.cadence.data.local.database.entity.TopArtistsEntity
import com.arekb.cadence.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopArtistsUiState())
    val uiState = _uiState.asStateFlow()

    private var fetchJob: Job? = null

    /**
     * Called by the UI to fetch top artists.
     * @param timeRange The time range to fetch tracks for.
     */
    fun fetchTopArtists(timeRange: String = "short_term") {
        // Cancel any previous fetch job to avoid running multiple collectors at once.
        fetchJob?.cancel()

        fetchJob = viewModelScope.launch {
            userRepository.getTopArtists(timeRange = timeRange, limit = 20)
                .collect { result ->
                    result.fold(
                        onSuccess = { artists ->
                            _uiState.update {
                                if (artists.isNullOrEmpty()) {
                                    it.copy(isLoading = true)
                                } else {
                                    it.copy(isLoading = false, topArtists = artists)
                                }
                            }
                        },
                        onFailure = {
                            _uiState.update {
                                it.copy(isLoading = false, error = "Failed to load top artists.")
                            }
                        }
                    )
                }
        }
    }

    /**
     * Called by the UI to trigger a forced refresh.
     */
    fun onRefresh(timeRange: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userRepository.forceRefreshTopArtists(timeRange)
        }
    }
}

data class TopArtistsUiState(
    val isLoading: Boolean = true,
    val topArtists: List<TopArtistsEntity> = emptyList(),
    val error: String? = null
)