package com.arekb.cadence.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.cadence.data.local.database.entity.NewReleasesEntity
import com.arekb.cadence.data.local.database.entity.TopArtistsEntity
import com.arekb.cadence.data.local.database.entity.UserProfileEntity
import com.arekb.cadence.data.remote.dto.PlayHistoryObject
import com.arekb.cadence.data.repository.UserRepository
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
        observeTopArtists()
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

    private fun observeTopArtists() {
        viewModelScope.launch {
            userRepository.getTopArtists("long_term", 50).collect { result ->
                result.fold(
                    onSuccess = { artists ->
                        val score = calculatePopularityScore(artists)
                        _uiState.update { it.copy(popularityScore = score) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(error = "Failed to calculate score: ${error.message}") }
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

    private fun calculatePopularityScore(artists: List<TopArtistsEntity>?): Int? {
        if (artists.isNullOrEmpty()) return null

        val maxWeight = artists.size
        val totalWeight = (maxWeight * (maxWeight + 1)) / 2.0
        if (totalWeight == 0.0) return 0

        val weightedPopularitySum = artists.withIndex().sumOf { (index, artist) ->
            val weight = maxWeight - index
            (artist.popularity * weight).toDouble()
        }

        return (weightedPopularitySum / totalWeight).toInt()
    }

    fun onRetry() { initialise() }
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val userProfile: UserProfileEntity? = null,
    val recentlyPlayed: List<PlayHistoryObject> = emptyList(),
    val popularityScore: Int? = null,
    val newReleases: List<NewReleasesEntity> = emptyList(),
    val error: String? = null
)

sealed interface HomeViewEvent {
    object NavigateToLogin : HomeViewEvent
}