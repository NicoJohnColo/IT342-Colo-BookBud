package com.example.bookbud.shared.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

abstract class BaseViewModel<UIState, UIEvent> : ViewModel() {
    private val _uiState = MutableStateFlow<UIState>(getInitialState())
    val uiState: Flow<UIState> = _uiState.asStateFlow()
    
    private val _uiEvent = Channel<UIEvent>()
    val uiEvent: Flow<UIEvent> = _uiEvent.receiveAsFlow()
    
    protected val currentState: UIState
        get() = _uiState.value
    
    protected fun updateState(state: UIState) {
        _uiState.value = state
    }
    
    protected suspend fun sendEvent(event: UIEvent) {
        _uiEvent.send(event)
    }
    
    abstract fun getInitialState(): UIState
}
