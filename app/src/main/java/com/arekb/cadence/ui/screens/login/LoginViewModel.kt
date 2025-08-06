package com.arekb.cadence.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.cadence.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<LoginViewEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onLoginClicked() {
        viewModelScope.launch {
            _eventFlow.emit(LoginViewEvent.StartSdkLogin)
        }
    }

    fun onAuthCodeReceived(code: String) {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val result = authRepository.exchangeCodeForToken(code)
            if (result.isSuccess) {
                _eventFlow.emit(LoginViewEvent.NavigateToHome)
            } else {
                _uiState.update {
                    it.copy(isLoading = false, error = result.exceptionOrNull()?.message)
                }
            }
        }
    }

    sealed interface LoginViewEvent {
        data object StartSdkLogin : LoginViewEvent
        data object NavigateToHome : LoginViewEvent
    }

    data class LoginUiState(
        val isLoading: Boolean = false,
        val error: String? = null
    )
}