package com.arekb.cadence.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.cadence.core.data.repository.UserRepository
import com.arekb.cadence.core.model.Album
import com.arekb.cadence.core.model.Track
import com.arekb.cadence.core.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<HomeViewEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        initialise()
    }

    private fun initialise() {
        // Set the initial loading state
        _uiState.update { it.copy(isLoading = true, error = null) }

        // Launch all the observers. They will run concurrently.
        observeProfile()
        observePopularityScore()
        observeNewReleases()
        fetchRecentlyPlayed()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            userRepository.getProfile().collect { result ->
                result.fold(
                    onSuccess = { user ->
                        _uiState.update {
                            it.copy(isLoading = (user == null), userProfile = user)
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isLoading = false, error = "Failed to load profile: ${error.message}") }
                    }
                )
            }
        }
    }

    private fun observeNewReleases() {
        viewModelScope.launch {
            userRepository.getNewReleases(limit = 20).collect { result ->
                result.fold(
                    onSuccess = { releases ->
                        _uiState.update { it.copy(newReleases = releases ?: emptyList()) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(error = "Failed to load new releases: ${error.message}") }
                    }
                )
            }
        }
    }

    private fun fetchRecentlyPlayed() {
        viewModelScope.launch {
            userRepository.getRecentlyPlayed().onSuccess { tracks ->
                _uiState.update { it.copy(recentlyPlayed = tracks) }
            }.onFailure { error ->
                _uiState.update { it.copy(error = "Failed to load recent tracks: ${error.message}") }
            }
        }
    }

    private fun observePopularityScore() {
        viewModelScope.launch {
            userRepository.getUserPopularityScore().collect { result ->
                result.onSuccess { score ->
                    _uiState.update { it.copy(popularityScore = score) }
                }.onFailure { error ->
                    _uiState.update { it.copy(error = "Failed to load score: ${error.message}") }
                }
            }
        }
    }

    fun onRetry() { initialise() }
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val userProfile: User? = null,
    val recentlyPlayed: List<Track> = emptyList(),
    val popularityScore: Int? = null,
    val newReleases: List<Album> = emptyList(),
    val error: String? = null
)

sealed interface HomeViewEvent {
    object NavigateToLogin : HomeViewEvent
}