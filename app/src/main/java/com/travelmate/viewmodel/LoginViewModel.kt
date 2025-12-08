package com.travelmate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.AuthResponse
import com.travelmate.data.repository.AuthRepository
import com.travelmate.utils.UserPreferences
import com.travelmate.utils.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val response: AuthResponse) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            
            try {
                val result = authRepository.login(email, password)
                
                result.fold(
                    onSuccess = { authResponse ->
                        Log.d("LoginViewModel", "=== LOGIN SUCCESS ===")
                        Log.d("LoginViewModel", "Access Token: ${authResponse.accessToken.take(20)}...")
                        Log.d("LoginViewModel", "Refresh Token: ${authResponse.refreshToken.take(20)}...")
                        Log.d("LoginViewModel", "User ID: ${authResponse.user?._id}")
                        Log.d("LoginViewModel", "User Type: ${authResponse.user?.userType}")
                        Log.d("LoginViewModel", "User Email: ${authResponse.user?.email}")
                        
                        // Save user data to preferences
                        userPreferences.saveAuthResponse(
                            authResponse.accessToken,
                            authResponse.refreshToken,
                            authResponse.user
                        )
                        
                        // Verify saved data
                        Log.d("LoginViewModel", "=== VERIFYING SAVED DATA ===")
                        Log.d("LoginViewModel", "Saved Access Token: ${userPreferences.getAccessToken()?.take(20)}...")
                        Log.d("LoginViewModel", "Saved User Type: ${userPreferences.getUserType()}")
                        Log.d("LoginViewModel", "Saved User ID: ${userPreferences.getUserId()}")
                        
                        _uiState.value = LoginUiState.Success(authResponse)
                    },
                    onFailure = { error ->
                        Log.e("LoginViewModel", "Login failed: ${error.message}")
                        _uiState.value = LoginUiState.Error(error.message ?: "Erreur de connexion")
                    }
                )
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Login error: ${e.message}")
                _uiState.value = LoginUiState.Error("Erreur: ${e.message}")
            }
        }
    }
    
    fun validateEmail(email: String): Boolean {
        return ValidationUtils.validateEmail(email)
    }
    
    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
    
    override fun onCleared() {
        super.onCleared()
    }
}
