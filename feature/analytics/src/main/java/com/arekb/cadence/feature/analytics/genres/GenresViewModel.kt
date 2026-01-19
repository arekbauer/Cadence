package com.arekb.cadence.feature.analytics.genres

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.cadence.core.data.repository.UserRepository
import com.arekb.cadence.core.model.Genre
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GenresViewModel @Inject constructor(
    userRepository: UserRepository
): ViewModel() {

    /**
     * Called by the UI to fetch top artists (genres).
     */
    val uiState: StateFlow<AnalyticsUiState> = userRepository.getTopGenresStream()
        .map { result ->
            result.fold(
                onSuccess = { genres ->
                    AnalyticsUiState(
                        isLoading = false,
                        topGenres = genres
                    )
                },
                onFailure = {
                    AnalyticsUiState(
                        isLoading = false,
                        error = "Failed to load analytics."
                    )
                }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = AnalyticsUiState(isLoading = true)
        )
}

/**
 * The UI state for the Analytics screen.
 * @param isLoading True if the initial data is being loaded.
 * @param topGenres A list of genres with their corresponding artists.
 * @param error An error message if something went wrong.
 */
data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val topGenres: List<Genre> = emptyList(),
    val error: String? = null
)