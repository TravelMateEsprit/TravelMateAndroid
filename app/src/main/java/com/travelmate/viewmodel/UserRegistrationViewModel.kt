package com.travelmate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.AuthResponse
import com.travelmate.data.models.UserRegistrationRequest
import com.travelmate.data.repository.AuthRepository
import com.travelmate.data.socket.SocketService
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
    private val socketService: SocketService,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UserRegistrationUiState>(UserRegistrationUiState.Idle)
    val uiState: StateFlow<UserRegistrationUiState> = _uiState.asStateFlow()
    
    val socketMessages = socketService.messages
    val connectionState = socketService.connectionState
    val registrationSuccess = socketService.registrationSuccess
    val registrationError = socketService.registrationError
    
    fun connectSocket() {
        socketService.connect()
    }
    
    fun disconnectSocket() {
        socketService.disconnect()
    }
    
    fun registerUser(request: UserRegistrationRequest) {
        viewModelScope.launch {
            _uiState.value = UserRegistrationUiState.Loading
            
            // Reset les valeurs précédentes
            socketService.resetRegistrationState()
            
            // Émettre l'inscription via Socket.IO
            socketService.emitUserRegistration(request)
            
            // Écouter les réponses avec timeout
            launch {
                socketService.registrationSuccess
                    .filterNotNull()
                    .first()
                    .let { response ->
                        try {
                            Log.d("UserRegistrationVM", "Registration success response: $response")
                            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                            val authResponse = json.decodeFromString<AuthResponse>(response)
                            _uiState.value = UserRegistrationUiState.Success(authResponse)
                        } catch (e: Exception) {
                            Log.e("UserRegistrationVM", "Parse error: ${e.message}")
                            _uiState.value = UserRegistrationUiState.Error("Erreur de format: ${e.message}")
                        }
                    }
            }
            
            launch {
                socketService.registrationError
                    .filterNotNull()
                    .first()
                    .let { error ->
                        _uiState.value = UserRegistrationUiState.Error(
                            error.message ?: "Erreur lors de l'inscription"
                        )
                    }
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
        socketService.resetRegistrationState()
    }
    
    override fun onCleared() {
        super.onCleared()
        socketService.resetRegistrationState()
    }
}
