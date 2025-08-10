package com.arekb.cadence.ui.screens.home

import androidx.datastore.core.IOException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.cadence.data.local.database.entity.UserProfileEntity
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

    /**
     * Fetches the user profile from the repository and updates the UI state.
     **/
    fun fetchUserProfile() {
        viewModelScope.launch {
            userRepository.getProfile()
                .collect { result ->
                    result.fold(
                        onSuccess = { user ->
                            _uiState.update {
                                if (user != null) {
                                    HomeUiState(isLoading = false, userProfile = user)
                                } else {
                                    HomeUiState(isLoading = true)
                                }
                            }
                        },
                        onFailure = {
                            _uiState.update {
                                HomeUiState(isLoading = false, error = "Failed to load profile.")
                            }
                        }
                    )
                }
        }
    }

    data class HomeUiState(
        val isLoading: Boolean = true,
        val userProfile: UserProfileEntity? = null,
        val error: String? = null
    )
}

sealed interface HomeViewEvent {
    object NavigateToLogin : HomeViewEvent
}
