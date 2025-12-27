package com.travelmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.AuthResetCodeRequest
import com.travelmate.data.models.MessageResponse
import com.travelmate.data.models.ResetPasswordWithCodeRequest
import com.travelmate.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ResetPasswordCodeUiState {
    object Idle : ResetPasswordCodeUiState()
    object Loading : ResetPasswordCodeUiState()
    data class CodeVerified(val message: String) : ResetPasswordCodeUiState()
    data class PasswordChanged(val message: String) : ResetPasswordCodeUiState()
    data class Error(val message: String) : ResetPasswordCodeUiState()
}

@HiltViewModel
class ResetPasswordCodeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<ResetPasswordCodeUiState>(ResetPasswordCodeUiState.Idle)
    val uiState: StateFlow<ResetPasswordCodeUiState> = _uiState.asStateFlow()

    fun verifyCode(email: String, code: String) {
        viewModelScope.launch {
            _uiState.value = ResetPasswordCodeUiState.Loading
            val result = try {
                authRepository.verifyResetCode(email, code)
            } catch (e: Exception) {
                Result.failure<MessageResponse>(e)
            }
            _uiState.value = if (result.isSuccess) {
                ResetPasswordCodeUiState.CodeVerified(result.getOrNull()?.message ?: "Code vérifié")
            } else {
                ResetPasswordCodeUiState.Error(result.exceptionOrNull()?.message ?: "Erreur lors de la vérification du code")
            }
        }
    }

    fun changePassword(email: String, code: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = ResetPasswordCodeUiState.Loading
            val result = try {
                authRepository.resetPasswordWithCode(email, code, newPassword)
            } catch (e: Exception) {
                Result.failure<MessageResponse>(e)
            }
            _uiState.value = if (result.isSuccess) {
                ResetPasswordCodeUiState.PasswordChanged(result.getOrNull()?.message ?: "Mot de passe changé")
            } else {
                ResetPasswordCodeUiState.Error(result.exceptionOrNull()?.message ?: "Erreur lors du changement de mot de passe")
            }
        }
    }

    fun resetState() {
        _uiState.value = ResetPasswordCodeUiState.Idle
    }
}
