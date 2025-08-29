package com.arekb.cadence.ui.screens.genres

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.cadence.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GenresViewModel @Inject constructor(
    private val userRepository: UserRepository
): ViewModel() {

    /**
     * Called by the UI to fetch top artists (genres).
     */
    // This declarative StateFlow is the single source of truth for the UI.
    val uiState: StateFlow<AnalyticsUiState> =
        userRepository.getTopArtists(timeRange = "long_term", limit = 50)
            .map { result ->
                result.fold(
                    onSuccess = { artists ->
                        if (artists.isNullOrEmpty()) {
                            // If the cache is empty, stay in the loading state.
                            AnalyticsUiState(isLoading = true)
                        } else {
                            val genreToArtistsMap = mutableMapOf<String, MutableList<Artist>>()

                            artists.forEach { artistEntity ->
                                val genres = artistEntity.genres.split(", ").filter { it.isNotBlank() && it != "Unknown" }
                                genres.forEach { genre ->
                                    val artist = Artist(name = artistEntity.artistName, imageUrl = artistEntity.imageUrl)
                                    genreToArtistsMap.getOrPut(genre) { mutableListOf() }.add(artist)
                                }
                            }

                            val rankedGenres = genreToArtistsMap.map { (genreName, artistList) ->
                                GenreWithArtists(
                                    name = genreName,
                                    count = artistList.size,
                                    artists = artistList
                                )
                            }.sortedByDescending { it.count }

                            AnalyticsUiState(isLoading = false, topGenresWithArtists = rankedGenres)
                        }
                    },
                    onFailure = {
                        AnalyticsUiState(isLoading = false, error = "Failed to load analytics.")
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
 * @param topGenresWithArtists A list of genres with their corresponding artists.
 * @param error An error message if something went wrong.
 */
data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val topGenresWithArtists: List<GenreWithArtists> = emptyList(),
    val error: String? = null
)

data class Artist(
    val name: String,
    val imageUrl: String?
)

data class GenreWithArtists(
    val name: String,
    val count: Int,
    val artists: List<Artist>
)
