package com.travelmate.ui.user.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.CreateInsuranceRequestRequest
import com.travelmate.data.models.InsuranceRequest
import com.travelmate.data.models.User
import com.travelmate.data.repository.InsuranceRequestRepository
import com.travelmate.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CreateRequestState {
    object Idle : CreateRequestState()
    object Loading : CreateRequestState()
    data class Success(val request: InsuranceRequest) : CreateRequestState()
    data class Error(val message: String) : CreateRequestState()
}

@HiltViewModel
class CreateInsuranceRequestViewModel @Inject constructor(
    private val repository: InsuranceRequestRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _state = MutableStateFlow<CreateRequestState>(CreateRequestState.Idle)
    val state: StateFlow<CreateRequestState> = _state.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    init {
        loadCurrentUser()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val userJson = userPreferences.getUserData()
                if (userJson != null) {
                    val user = kotlinx.serialization.json.Json { 
                        ignoreUnknownKeys = true 
                    }.decodeFromString<User>(userJson)
                    _currentUser.value = user
                } else {
                    val email = userPreferences.getUserEmail()
                    val name = userPreferences.getUserName()
                    val userId = userPreferences.getUserId()
                    val userType = userPreferences.getUserType()
                    val phone = userPreferences.getUserPhone()
                    
                    if (email != null && userId != null && userType != null) {
                        _currentUser.value = User(
                            _id = userId,
                            email = email,
                            name = name,
                            userType = userType,
                            phone = phone
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CreateInsuranceRequestViewModel", "Error loading user", e)
            }
        }
    }
    
    fun createRequest(
        insuranceId: String,
        travelerName: String,
        travelerEmail: String,
        travelerPhone: String,
        dateOfBirth: String,
        passportNumber: String,
        nationality: String,
        destination: String,
        departureDate: String,
        returnDate: String,
        travelPurpose: String? = null,
        message: String? = null
    ) {
        viewModelScope.launch {
            _state.value = CreateRequestState.Loading
            
            try {
                val request = CreateInsuranceRequestRequest(
                    insuranceId = insuranceId,
                    travelerName = travelerName,
                    travelerEmail = travelerEmail,
                    travelerPhone = travelerPhone,
                    dateOfBirth = dateOfBirth,
                    passportNumber = passportNumber,
                    nationality = nationality,
                    destination = destination,
                    departureDate = departureDate,
                    returnDate = returnDate,
                    travelPurpose = travelPurpose,
                    message = message
                )
                
                val token = userPreferences.getAccessToken() ?: return@launch
                val response = repository.createRequest(token, request)
                
                if (response.isSuccessful && response.body() != null) {
                    _state.value = CreateRequestState.Success(response.body()!!)
                } else {
                    _state.value = CreateRequestState.Error(
                        response.errorBody()?.string() ?: "Erreur lors de la création de la demande"
                    )
                }
            } catch (e: Exception) {
                _state.value = CreateRequestState.Error(
                    e.message ?: "Erreur réseau"
                )
            }
        }
    }
    
    fun resetState() {
        _state.value = CreateRequestState.Idle
    }
}
