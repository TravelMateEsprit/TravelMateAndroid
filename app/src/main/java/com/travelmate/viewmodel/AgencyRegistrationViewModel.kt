package com.travelmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.AgencyRegistrationRequest
import com.travelmate.data.models.AuthResponse
import com.travelmate.data.repository.AuthRepository
import com.travelmate.data.socket.SocketService
import com.travelmate.utils.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AgencyRegistrationUiState {
    object Idle : AgencyRegistrationUiState()
    object Loading : AgencyRegistrationUiState()
    data class Success(val response: AuthResponse) : AgencyRegistrationUiState()
    data class Error(val message: String) : AgencyRegistrationUiState()
}

data class AgencyFormData(
    var email: String = "",
    var password: String = "",
    var confirmPassword: String = "",
    var agencyName: String = "",
    var siret: String = "",
    var address: String = "",
    var city: String = "",
    var postalCode: String = "",
    var country: String = "France",
    var phone: String = "",
    var websiteUrl: String = "",
    var description: String = "",
    var legalRepFirstName: String = "",
    var legalRepLastName: String = "",
    var kbisDocument: String? = null
)

@HiltViewModel
class AgencyRegistrationViewModel @Inject constructor(
    private val socketService: SocketService,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<AgencyRegistrationUiState>(AgencyRegistrationUiState.Idle)
    val uiState: StateFlow<AgencyRegistrationUiState> = _uiState.asStateFlow()
    
    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()
    
    private val _formData = MutableStateFlow(AgencyFormData())
    val formData: StateFlow<AgencyFormData> = _formData.asStateFlow()
    
    val socketMessages = socketService.messages
    val connectionState = socketService.connectionState
    val registrationSuccess = socketService.registrationSuccess
    val registrationError = socketService.registrationError
    
    companion object {
        const val TOTAL_STEPS = 5
    }
    
    fun connectSocket() {
        socketService.connect()
    }
    
    fun updateFormData(update: AgencyFormData.() -> AgencyFormData) {
        _formData.value = _formData.value.update()
    }
    
    fun nextStep() {
        if (_currentStep.value < TOTAL_STEPS) {
            _currentStep.value++
        }
    }
    
    fun previousStep() {
        if (_currentStep.value > 1) {
            _currentStep.value--
        }
    }
    
    fun goToStep(step: Int) {
        if (step in 1..TOTAL_STEPS) {
            _currentStep.value = step
        }
    }
    
    fun registerAgency() {
        viewModelScope.launch {
            _uiState.value = AgencyRegistrationUiState.Loading
            
            try {
                val data = _formData.value
                val fullName = "${data.legalRepFirstName.trim()} ${data.legalRepLastName.trim()}"
                val request = AgencyRegistrationRequest(
                    name = fullName,
                    email = data.email.trim(),
                    password = data.password,
                    agencyName = data.agencyName.trim(),
                    agencyLicense = data.siret.trim(),
                    address = data.address.trim(),
                    city = data.city.trim(),
                    country = data.country.trim(),
                    phone = data.phone.trim(),
                    agencyWebsite = data.websiteUrl.trim().takeIf { it.isNotEmpty() },
                    agencyDescription = data.description.trim().takeIf { it.isNotEmpty() }
                )
                
                // Call HTTP REST API for agency registration
                val result = authRepository.registerAgency(request)
                
                result.fold(
                    onSuccess = { response ->
                        _uiState.value = AgencyRegistrationUiState.Success(response)
                    },
                    onFailure = { error ->
                        _uiState.value = AgencyRegistrationUiState.Error(
                            error.message ?: "Erreur lors de l'inscription"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = AgencyRegistrationUiState.Error(
                    e.message ?: "Erreur d'inscription"
                )
            }
        }
    }
    
    fun validateEmail(email: String) = ValidationUtils.validateEmail(email)
    fun validatePassword(password: String) = ValidationUtils.validatePassword(password)
    fun validateSiret(siret: String) = ValidationUtils.validateSiret(siret)
    fun validatePhone(phone: String) = ValidationUtils.validatePhone(phone)
    fun validatePostalCode(postalCode: String) = ValidationUtils.validatePostalCode(postalCode)
    fun validateUrl(url: String) = ValidationUtils.validateUrl(url)
    
    fun isStep1Valid(): Boolean {
        val data = _formData.value
        return data.email.isNotBlank() && validateEmail(data.email) &&
               data.password.isNotBlank() && validatePassword(data.password) &&
               data.confirmPassword.isNotBlank() && data.password == data.confirmPassword
    }
    
    fun isStep2Valid(): Boolean {
        val data = _formData.value
        return data.agencyName.isNotBlank() &&
               data.siret.isNotBlank() && validateSiret(data.siret) &&
               data.phone.isNotBlank() && validatePhone(data.phone)
    }
    
    fun isStep3Valid(): Boolean {
        val data = _formData.value
        return data.address.isNotBlank() &&
               data.city.isNotBlank() &&
               data.postalCode.isNotBlank() && validatePostalCode(data.postalCode) &&
               data.country.isNotBlank()
    }
    
    fun isStep4Valid(): Boolean {
        val data = _formData.value
        return data.legalRepFirstName.isNotBlank() &&
               data.legalRepLastName.isNotBlank()
    }
    
    fun resetState() {
        _uiState.value = AgencyRegistrationUiState.Idle
        _currentStep.value = 1
        _formData.value = AgencyFormData()
        socketService.resetRegistrationState()
    }
    
    override fun onCleared() {
        super.onCleared()
        socketService.resetRegistrationState()
    }
}
