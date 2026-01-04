package com.travelmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object CodeSent : UiState()
        object PasswordChanged : UiState()
        data class Error(val message: String) : UiState()
    }

    fun requestPasswordChangeCode() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            val result = authRepository.requestPasswordChangeCode()
            
            result.onSuccess {
                _uiState.value = UiState.CodeSent
            }.onFailure { e ->
                _uiState.value = UiState.Error(
                    e.message ?: "Erreur lors de l'envoi du code"
                )
            }
        }
    }

    fun changePasswordWithCode(code: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            val result = authRepository.changePasswordWithCode(code, newPassword)
            
            result.onSuccess {
                _uiState.value = UiState.PasswordChanged
            }.onFailure { e ->
                _uiState.value = UiState.Error(
                    e.message ?: "Erreur lors du changement de mot de passe"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
