package com.example.bookbud.features.auth.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.bookbud.features.auth.domain.usecase.RegisterUseCase
import com.example.bookbud.shared.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

sealed class RegisterUiEvent {
    data class ShowError(val message: String) : RegisterUiEvent()
    object NavigateToDashboard : RegisterUiEvent()
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : BaseViewModel<RegisterUiState, RegisterUiEvent>() {
    
    override fun getInitialState() = RegisterUiState()
    
    fun register(firstName: String, lastName: String, email: String, password: String) {
        updateState(currentState.copy(isLoading = true, error = null))
        viewModelScope.launch {
            try {
                registerUseCase(firstName, lastName, email, password)
                updateState(currentState.copy(isLoading = false, success = true))
                sendEvent(RegisterUiEvent.NavigateToDashboard)
            } catch (e: Exception) {
                updateState(currentState.copy(isLoading = false, error = e.message))
                sendEvent(RegisterUiEvent.ShowError(e.message ?: "Registration failed"))
            }
        }
    }
}
