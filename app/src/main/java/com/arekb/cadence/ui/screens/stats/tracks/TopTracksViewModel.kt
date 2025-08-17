package com.arekb.cadence.ui.screens.stats.tracks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.cadence.data.local.database.entity.TopTracksEntity
import com.arekb.cadence.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState = _uiState.asStateFlow()

    private var fetchJob: Job? = null

    /**
     * Called by the UI to fetch top tracks.
     * @param timeRange The time range to fetch tracks for.
     */
    fun fetchTopTracks(timeRange: String = "short_term") {
        // Cancel any previous fetch job to avoid running multiple collectors at once.
        fetchJob?.cancel()

        fetchJob = viewModelScope.launch {
            userRepository.getTopTracks(timeRange = timeRange, limit = 20)
                .collect { result ->
                    result.fold(
                        onSuccess = { tracks ->
                            _uiState.update {
                                if (tracks.isNullOrEmpty()) {
                                    it.copy(isLoading = true)
                                } else {
                                    it.copy(isLoading = false, topTracks = tracks)
                                }
                            }
                        },
                        onFailure = {
                            _uiState.update {
                                it.copy(isLoading = false, error = "Failed to load top tracks.")
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
            userRepository.forceRefreshTopTracks(timeRange)
        }
    }
}

data class StatsUiState(
    val isLoading: Boolean = true,
    val topTracks: List<TopTracksEntity> = emptyList(),
    val error: String? = null
)