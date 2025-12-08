package com.travelmate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.AuthResponse
import com.travelmate.data.models.UserRegistrationRequest
import com.travelmate.data.repository.AuthRepository
import com.travelmate.utils.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UserRegistrationUiState {
    object Idle : UserRegistrationUiState()
    object Loading : UserRegistrationUiState()
    data class Success(val response: AuthResponse) : UserRegistrationUiState()
    data class Error(val message: String) : UserRegistrationUiState()
}

@HiltViewModel
class UserRegistrationViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UserRegistrationUiState>(UserRegistrationUiState.Idle)
    val uiState: StateFlow<UserRegistrationUiState> = _uiState.asStateFlow()
    
    fun registerUser(request: UserRegistrationRequest) {
        viewModelScope.launch {
            _uiState.value = UserRegistrationUiState.Loading
            
            try {
                val result = authRepository.registerUser(request)
                
                result.fold(
                    onSuccess = { authResponse ->
                        Log.d("UserRegistrationVM", "Registration success: ${authResponse.user?.email}")
                        _uiState.value = UserRegistrationUiState.Success(authResponse)
                    },
                    onFailure = { error ->
                        Log.e("UserRegistrationVM", "Registration failed: ${error.message}")
                        _uiState.value = UserRegistrationUiState.Error(error.message ?: "Erreur d'inscription")
                    }
                )
            } catch (e: Exception) {
                Log.e("UserRegistrationVM", "Registration error: ${e.message}")
                _uiState.value = UserRegistrationUiState.Error("Erreur: ${e.message}")
            }
        }
    }
    
    fun validateEmail(email: String): Boolean {
        return ValidationUtils.validateEmail(email)
    }
    
    fun validatePassword(password: String): Boolean {
        return ValidationUtils.validatePassword(password)
    }
    
    fun getEmailError(email: String): String? {
        return ValidationUtils.getEmailError(email)
    }
    
    fun getPasswordError(password: String): String? {
        return ValidationUtils.getPasswordError(password)
    }
    
    fun resetState() {
        _uiState.value = UserRegistrationUiState.Idle
    }
    
    override fun onCleared() {
        super.onCleared()
    }
}
