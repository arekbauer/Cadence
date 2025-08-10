package com.arekb.cadence.ui.screens.stats

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

    fun fetchTopTracks(timeRange: String = "short_term") {
        // Cancel any previous fetch job to avoid running multiple collectors at once.
        fetchJob?.cancel()

        fetchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            userRepository.getTopTracks(timeRange = timeRange, limit = 20)
                .collect { result ->
                    result.fold(
                        onSuccess = { tracks ->
                            _uiState.update {
                                if (tracks != null) {
                                    it.copy(isLoading = false, topTracks = tracks)
                                } else {
                                    it.copy(isLoading = true)
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
}

data class StatsUiState(
    val isLoading: Boolean = true,
    val topTracks: List<TopTracksEntity> = emptyList(),
    val error: String? = null
)