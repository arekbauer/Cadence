package com.arekb.cadence.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.cadence.data.local.database.entity.TopArtistsEntity
import com.arekb.cadence.data.local.database.entity.UserProfileEntity
import com.arekb.cadence.data.remote.dto.PlayHistoryObject
import com.arekb.cadence.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
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

    // A SharedFlow for one-time events like forcing a logout
    private val _eventFlow = MutableSharedFlow<HomeViewEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    /**
     * Orchestrator function to load all data for the home screen.
     * It delegates the actual work to smaller, private functions for better
     * maintainability and reusability.
     */
    fun loadHomeScreenData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // 1. Fetch all data in parallel for maximum efficiency
                val profileDeferred = async { fetchUserProfile() }
                val recentlyPlayedDeferred = async { fetchRecentlyPlayed() }
                val topArtistsDeferred = async { fetchTopArtists() }

                // 2. Await all results
                val profileResult = profileDeferred.await()
                val recentlyPlayedResult = recentlyPlayedDeferred.await()
                val topArtistsResult = topArtistsDeferred.await()

                // 3. Process results, throwing an exception if any call failed
                val userProfile = profileResult.getOrThrow()
                val recentlyPlayed = recentlyPlayedResult.getOrThrow()
                val topArtists = topArtistsResult.getOrThrow()

                // 4. Perform business logic using a pure function
                val popularityScore = calculatePopularityScore(topArtists)

                // 5. Update state once with all the new data
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userProfile = userProfile,
                        recentlyPlayed = recentlyPlayed,
                        popularityScore = popularityScore
                    )
                }

            } catch (e: Exception) {
                // 6. Handle any failures from the parallel calls
                _uiState.update { it.copy(isLoading = false, error = "Failed to load data." + e.message) }
            }
        }
    }

    // --- Private, Single-Responsibility Functions ---

    private suspend fun fetchUserProfile(): Result<UserProfileEntity?> {
        // .first() collects the first emission from the Flow and cancels it.
        return userRepository.getProfile().first()
    }

    private suspend fun fetchRecentlyPlayed(): Result<List<PlayHistoryObject>> {
        return userRepository.getRecentlyPlayed()
    }

    private suspend fun fetchTopArtists(): Result<List<TopArtistsEntity>?> {
        return userRepository.getTopArtists("long_term", 50).first()
    }

    /**
     * A pure function for calculating the popularity score.
     */
    private fun calculatePopularityScore(artists: List<TopArtistsEntity>?): Int? {
        if (artists.isNullOrEmpty()) return null

        val maxWeight = artists.size
        // Sum of arithmetic series: n * (n + 1) / 2
        val totalWeight = (maxWeight * (maxWeight + 1)) / 2.0
        if (totalWeight == 0.0) return 0

        val weightedPopularitySum = artists.withIndex().sumOf { (index, artist) ->
            // Weight is based on index (item at index 0 gets max weight)
            val weight = maxWeight - index
            (artist.popularity * weight).toDouble()
        }

        return (weightedPopularitySum / totalWeight).toInt()
    }

    data class HomeUiState(
        val isLoading: Boolean = true,
        val userProfile: UserProfileEntity? = null,
        val recentlyPlayed: List<PlayHistoryObject> = emptyList(),
        val popularityScore: Int? = null,
        val error: String? = null
    )
}

sealed interface HomeViewEvent {
    object NavigateToLogin : HomeViewEvent
}
