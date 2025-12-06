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

sealed class ResetPasswordUiState {
    object Idle : ResetPasswordUiState()
    object Loading : ResetPasswordUiState()
    data class Success(val message: String) : ResetPasswordUiState()
    data class Error(val message: String) : ResetPasswordUiState()
}

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResetPasswordUiState>(ResetPasswordUiState.Idle)
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    fun resetPassword(token: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = ResetPasswordUiState.Loading
            
            val result = authRepository.resetPassword(token, newPassword)
            
            _uiState.value = if (result.isSuccess) {
                val message = result.getOrNull()?.message ?: "Mot de passe réinitialisé avec succès"
                ResetPasswordUiState.Success(message)
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Erreur lors de la réinitialisation du mot de passe"
                ResetPasswordUiState.Error(errorMessage)
            }
        }
    }

    fun resetState() {
        _uiState.value = ResetPasswordUiState.Idle
    }

    fun validatePassword(password: String): Pair<Boolean, String?> {
        return when {
            password.length < 6 -> false to "Le mot de passe doit contenir au moins 6 caractères"
            !password.any { it.isDigit() } -> false to "Le mot de passe doit contenir au moins un chiffre"
            !password.any { it.isUpperCase() } -> false to "Le mot de passe doit contenir au moins une majuscule"
            else -> true to null
        }
    }
}
