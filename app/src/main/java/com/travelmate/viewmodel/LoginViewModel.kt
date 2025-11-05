package com.travelmate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.AuthResponse
import com.travelmate.data.repository.AuthRepository
import com.travelmate.data.socket.SocketService
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
    private val socketService: SocketService,
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    val connectionState = socketService.connectionState
    val loginSuccess = socketService.loginSuccess
    val loginError = socketService.loginError
    
    fun connectSocket() {
        socketService.connect()
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            
            // Reset les valeurs précédentes
            socketService.resetLoginState()
            
            // Émettre la connexion via Socket.IO
            socketService.emitLogin(email, password)
            
            // Écouter les réponses avec timeout
            launch {
                socketService.loginSuccess
                    .filterNotNull()
                    .first()
                    .let { response ->
                        try {
                            Log.d("LoginViewModel", "Login success response: $response")
                            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                            val authResponse = json.decodeFromString<AuthResponse>(response)
                            
                            Log.d("LoginViewModel", "=== SAVING AUTH DATA ===")
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
                        } catch (e: Exception) {
                            Log.e("LoginViewModel", "Parse error: ${e.message}")
                            _uiState.value = LoginUiState.Error("Erreur de format: ${e.message}")
                        }
                    }
            }
            
            launch {
                socketService.loginError
                    .filterNotNull()
                    .first()
                    .let { error ->
                        _uiState.value = LoginUiState.Error(error)
                    }
            }
        }
    }
    
    fun validateEmail(email: String): Boolean {
        return ValidationUtils.validateEmail(email)
    }
    
    fun resetState() {
        _uiState.value = LoginUiState.Idle
        socketService.resetLoginState()
    }
    
    override fun onCleared() {
        super.onCleared()
        socketService.resetLoginState()
    }
}
