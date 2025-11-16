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

sealed class ForgotPasswordUiState {
    object Idle : ForgotPasswordUiState()
    object Loading : ForgotPasswordUiState()
    data class Success(val message: String) : ForgotPasswordUiState()
    data class Error(val message: String) : ForgotPasswordUiState()
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun sendResetEmail(email: String) {
        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState.Loading
            
            val result = authRepository.forgotPassword(email)
            
            _uiState.value = if (result.isSuccess) {
                val message = result.getOrNull()?.message ?: "Email de réinitialisation envoyé avec succès"
                ForgotPasswordUiState.Success(message)
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Erreur lors de l'envoi de l'email"
                ForgotPasswordUiState.Error(errorMessage)
            }
        }
    }

    fun resetState() {
        _uiState.value = ForgotPasswordUiState.Idle
    }

    fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
