package com.arekb.cadence.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.cadence.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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

    init {
        fetchUserProfile()
    }

    /**
     * Fetches the user profile from the repository and updates the UI state.
     */
    private fun fetchUserProfile() {
        viewModelScope.launch {
            val result = userRepository.getProfile()
            result.onSuccess { userProfile ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        username = userProfile.displayName,
                        profileImageUrl = userProfile.images.firstOrNull()?.url,
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = it.error,
                    )
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
