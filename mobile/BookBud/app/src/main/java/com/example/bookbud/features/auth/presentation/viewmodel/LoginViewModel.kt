package com.example.bookbud.features.auth.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.bookbud.features.auth.domain.usecase.LoginUseCase
import com.example.bookbud.shared.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

sealed class LoginUiEvent {
    data class ShowError(val message: String) : LoginUiEvent()
    object NavigateToDashboard : LoginUiEvent()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : BaseViewModel<LoginUiState, LoginUiEvent>() {
    
    override fun getInitialState() = LoginUiState()
    
    fun login(email: String, password: String) {
        updateState(currentState.copy(isLoading = true, error = null))
        viewModelScope.launch {
            try {
                loginUseCase(email, password)
                updateState(currentState.copy(isLoading = false, success = true))
                sendEvent(LoginUiEvent.NavigateToDashboard)
            } catch (e: Exception) {
                updateState(currentState.copy(isLoading = false, error = e.message))
                sendEvent(LoginUiEvent.ShowError(e.message ?: "Login failed"))
            }
        }
    }
    
    fun googleLogin(idToken: String) {
        updateState(currentState.copy(isLoading = true, error = null))
        viewModelScope.launch {
            try {
                loginUseCase.googleLogin(idToken)
                updateState(currentState.copy(isLoading = false, success = true))
                sendEvent(LoginUiEvent.NavigateToDashboard)
            } catch (e: Exception) {
                updateState(currentState.copy(isLoading = false, error = e.message))
                sendEvent(LoginUiEvent.ShowError(e.message ?: "Google Sign-In failed"))
            }
        }
    }
}
