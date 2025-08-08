package com.arekb.cadence.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.cadence.data.remote.dto.TrackObject
import com.arekb.cadence.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun fetchTopTracks(timeRange: String = "short_term") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = userRepository.getTopTracks(timeRange = timeRange, limit = 20)
            result.onSuccess { response ->
                _uiState.update {
                    it.copy(isLoading = false, topTracks = response.items)
                }
            }.onFailure {
                _uiState.update {
                    it.copy(isLoading = false, error = "Failed to load top tracks.")
                }
            }
        }
    }
}

data class StatsUiState(
    val isLoading: Boolean = true,
    val topTracks: List<TrackObject> = emptyList(),
    val error: String? = null
)