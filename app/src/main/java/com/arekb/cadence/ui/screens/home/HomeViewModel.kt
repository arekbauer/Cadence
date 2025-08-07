package com.arekb.cadence.ui.screens.home

import androidx.datastore.core.IOException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.cadence.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
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

    init {
        fetchUserProfile()
    }

    /**
     * Fetches the user profile from the repository and updates the UI state.
     */
    private fun fetchUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = userRepository.getProfile()
                result.onSuccess { userProfile ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            username = userProfile.displayName,
                            profileImageUrl = userProfile.images.firstOrNull()?.url
                        )
                    }
                }.onFailure { exception ->
                    // Check for a 401 error, which means the refresh failed and we need to log out
                    if (exception is HttpException && exception.code() == 401) {
                        _eventFlow.emit(HomeViewEvent.NavigateToLogin)
                    } else {
                        // Handle other errors (no internet, server down)
                        _uiState.update {
                            it.copy(isLoading = false, error = "Could not load profile. Please try again.")
                        }
                    }
                }
            } catch (e: IOException) {
                // Handle network errors (no internet, server down)
                _uiState.update {
                    it.copy(isLoading = false, error = "Could not load profile. Please try again. $e")
                }
            }
        }
    }
}

/**
 * Represents the UI state of the home screen.
 *
 * @param isLoading True if the profile is being loaded, false otherwise.
 * @param username The display name of the user.
 * @param profileImageUrl The URL of the user's profile image.
 * @param error A message describing an error that occurred, or null if there was no error.
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val username: String? = null,
    val profileImageUrl: String? = null,
    val error: String? = null
)

sealed interface HomeViewEvent {
    object NavigateToLogin : HomeViewEvent
}
