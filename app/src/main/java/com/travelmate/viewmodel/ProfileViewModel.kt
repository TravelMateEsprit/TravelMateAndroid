package com.travelmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.UpdateAgencyProfileRequest
import com.travelmate.data.models.UpdateProfileRequest
import com.travelmate.data.models.User
import com.travelmate.data.repository.AuthRepository
import com.travelmate.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val token = userPreferences.getAccessToken()
            if (token != null) {
                val userType = userPreferences.getUserType()
                val result = if (userType == "agence") {
                    authRepository.getAgencyProfile(token)
                } else {
                    authRepository.getUserProfile(token)
                }
                
                result.onSuccess { user ->
                    _userProfile.value = user
                    // Update local prefs if needed
                    userPreferences.saveAuthResponse(token, userPreferences.getRefreshToken() ?: "", user)
                }.onFailure { e ->
                    _error.value = e.message
                }
            } else {
                _error.value = "Not logged in"
            }
            _isLoading.value = false
        }
    }

    fun updateProfile(request: UpdateProfileRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val token = userPreferences.getAccessToken()
            if (token != null) {
                val result = authRepository.updateUserProfile(token, request)
                result.onSuccess { user ->
                    _userProfile.value = user
                    userPreferences.saveAuthResponse(token, userPreferences.getRefreshToken() ?: "", user)
                }.onFailure { e ->
                    _error.value = e.message
                }
            }
            _isLoading.value = false
        }
    }

    fun updateAgencyProfile(request: UpdateAgencyProfileRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val token = userPreferences.getAccessToken()
            if (token != null) {
                val result = authRepository.updateAgencyProfile(token, request)
                result.onSuccess { user ->
                    _userProfile.value = user
                    userPreferences.saveAuthResponse(token, userPreferences.getRefreshToken() ?: "", user)
                }.onFailure { e ->
                    _error.value = e.message
                }
            }
            _isLoading.value = false
        }
    }
}
