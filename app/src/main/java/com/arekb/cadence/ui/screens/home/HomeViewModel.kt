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
import kotlinx.coroutines.flow.first
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
        fetchInitialData()
    }

    private fun fetchInitialData() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        // This parent job will now complete because all its children will complete.
        viewModelScope.launch {

            // Fetch user profile
            launch {
                try {
                    // Use .first() to get the first result and allow the coroutine to finish.
                    val result = userRepository.getProfile().first()
                    result.fold(
                        onSuccess = { user -> _uiState.update { it.copy(userProfile = user) } },
                        onFailure = { _uiState.update { it.copy(error = "Failed to load profile.") } }
                    )
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = "Failed to load profile. Exception: ${e.message}") }
                }
            }

            // Fetch recently played tracks (this was already correct as it's a suspend function)
            launch {
                val recentlyPlayedResult = userRepository.getRecentlyPlayed()
                recentlyPlayedResult.onSuccess { tracks ->
                    _uiState.update { it.copy(recentlyPlayed = tracks) }
                }.onFailure {
                    _uiState.update { it.copy(error = "Failed to load recent tracks.") }
                }
            }

            // Fetch top artists to calculate popularity score
            launch {
                try {
                    // Use .first() to get the first result and allow the coroutine to finish.
                    val result = userRepository.getTopArtists("long_term", 50).first()
                    result.fold(
                        onSuccess = { artists ->
                            val score = calculatePopularityScore(artists)
                            _uiState.update { it.copy(popularityScore = score) }
                        },
                        onFailure = { _uiState.update { it.copy(error = "Failed to calculate score.") } }
                    )
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = "Failed to calculate score. Exception: ${e.message}") }
                }
            }

            // Fetch new releases
            launch {
                try {
                    // Use .first() to get the first result and allow the coroutine to finish.
                    val result = userRepository.getNewReleases(limit = 20).first()
                    result.fold(
                        onSuccess = { releases ->
                            _uiState.update { it.copy(newReleases = releases ?: emptyList()) }
                        },
                        onFailure = { _uiState.update { it.copy(error = "Failed to load new releases.") } }
                    )
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = "Failed to load new releases. Exception: ${e.message}") }
                }
            }

        }.invokeOnCompletion {
            // This block will now execute correctly, hiding the loading indicator.
            _uiState.update { it.copy(isLoading = false) }
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

    fun onRetry() {
        fetchInitialData()
    }
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