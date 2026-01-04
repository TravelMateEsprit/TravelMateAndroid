package com.travelmate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.*
import com.travelmate.data.repository.AuthRepository
import com.travelmate.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompleteProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _age = MutableStateFlow<Int?>(null)
    val age: StateFlow<Int?> = _age.asStateFlow()

    private val _travelFrequency = MutableStateFlow<TravelFrequency?>(null)
    val travelFrequency: StateFlow<TravelFrequency?> = _travelFrequency.asStateFlow()

    private val _preferredDestinations = MutableStateFlow<List<String>>(emptyList())
    val preferredDestinations: StateFlow<List<String>> = _preferredDestinations.asStateFlow()

    private val _budgetRange = MutableStateFlow<BudgetRange?>(null)
    val budgetRange: StateFlow<BudgetRange?> = _budgetRange.asStateFlow()

    private val _travelPurpose = MutableStateFlow<TravelPurpose?>(null)
    val travelPurpose: StateFlow<TravelPurpose?> = _travelPurpose.asStateFlow()

    private val _companionType = MutableStateFlow<CompanionType?>(null)
    val companionType: StateFlow<CompanionType?> = _companionType.asStateFlow()

    private val _hasHealthConditions = MutableStateFlow<Boolean?>(null)
    val hasHealthConditions: StateFlow<Boolean?> = _hasHealthConditions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()
    
    // Mode édition
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    val canProceedToNextStep: StateFlow<Boolean> = kotlinx.coroutines.flow.combine(
        _currentStep,
        _age,
        _travelFrequency,
        _preferredDestinations,
        _budgetRange,
        _travelPurpose,
        _companionType,
        _hasHealthConditions
    ) { values ->
        val step = values[0] as Int
        val age = values[1] as Int?
        val frequency = values[2] as TravelFrequency?
        val destinations = values[3] as List<*>
        val budget = values[4] as BudgetRange?
        val purpose = values[5] as TravelPurpose?
        val companion = values[6] as CompanionType?
        val health = values[7] as Boolean?
        
        when (step) {
            0 -> age != null && frequency != null
            1 -> destinations.isNotEmpty()
            2 -> budget != null && purpose != null
            3 -> companion != null && health != null
            else -> false
        }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), false)

    fun setAge(age: Int?) {
        _age.value = age
    }

    fun setTravelFrequency(frequency: TravelFrequency?) {
        _travelFrequency.value = frequency
    }

    fun setPreferredDestinations(destinations: List<String>) {
        _preferredDestinations.value = destinations
    }

    fun addDestination(destination: String) {
        if (destination.isNotBlank() && !_preferredDestinations.value.contains(destination)) {
            _preferredDestinations.value = _preferredDestinations.value + destination
        }
    }

    fun removeDestination(destination: String) {
        _preferredDestinations.value = _preferredDestinations.value - destination
    }

    fun setBudgetRange(budget: BudgetRange?) {
        _budgetRange.value = budget
    }

    fun setTravelPurpose(purpose: TravelPurpose?) {
        _travelPurpose.value = purpose
    }

    fun setCompanionType(companion: CompanionType?) {
        _companionType.value = companion
    }

    fun setHasHealthConditions(hasConditions: Boolean?) {
        _hasHealthConditions.value = hasConditions
    }

    fun nextStep() {
        if (_currentStep.value < 3) {
            _currentStep.value++
        }
    }

    fun previousStep() {
        if (_currentStep.value > 0) {
            _currentStep.value--
        }
    }

    fun getCompletionPercentage(): Int {
        val fields = listOfNotNull(
            _age.value,
            _travelFrequency.value,
            _preferredDestinations.value.takeIf { it.isNotEmpty() },
            _budgetRange.value,
            _travelPurpose.value,
            _companionType.value,
            _hasHealthConditions.value
        )
        return ((fields.size.toFloat() / 7) * 100).toInt()
    }

    fun submitProfile() {
        viewModelScope.launch {
            android.util.Log.d("CompleteProfileVM", "=== DEBUT submitProfile ===")
            _isLoading.value = true
            _errorMessage.value = null

            val travelProfile = TravelProfile(
                age = _age.value,
                travelFrequency = _travelFrequency.value,
                preferredDestinations = _preferredDestinations.value.takeIf { it.isNotEmpty() },
                budgetRange = _budgetRange.value,
                travelPurpose = _travelPurpose.value,
                companionType = _companionType.value,
                hasHealthConditions = _hasHealthConditions.value
            )

            android.util.Log.d("CompleteProfileVM", "TravelProfile créé:")
            android.util.Log.d("CompleteProfileVM", "  age: ${travelProfile.age}")
            android.util.Log.d("CompleteProfileVM", "  travelFrequency: ${travelProfile.travelFrequency}")
            android.util.Log.d("CompleteProfileVM", "  preferredDestinations: ${travelProfile.preferredDestinations}")
            android.util.Log.d("CompleteProfileVM", "  budgetRange: ${travelProfile.budgetRange}")
            android.util.Log.d("CompleteProfileVM", "  travelPurpose: ${travelProfile.travelPurpose}")
            android.util.Log.d("CompleteProfileVM", "  companionType: ${travelProfile.companionType}")
            android.util.Log.d("CompleteProfileVM", "  hasHealthConditions: ${travelProfile.hasHealthConditions}")

            android.util.Log.d("CompleteProfileVM", "Appel de authRepository.updateTravelProfile()...")
            val result = authRepository.updateTravelProfile(travelProfile)
            
            _isLoading.value = false
            
            result.onSuccess { response ->
                android.util.Log.d("CompleteProfileVM", "✅ SUCCESS: $response")
                android.util.Log.d("CompleteProfileVM", "  profileCompletionPercentage: ${response.profileCompletionPercentage}")
                
                // Rafraîchir le profil utilisateur pour mettre à jour profileCompletionPercentage
                viewModelScope.launch {
                    android.util.Log.d("CompleteProfileVM", "Rafraîchissement du profil utilisateur...")
                    val token = userPreferences.getAccessToken()
                    if (token != null) {
                        val profileResult = authRepository.getUserProfile(token)
                        profileResult.onSuccess { user ->
                            android.util.Log.d("CompleteProfileVM", "✅ Profil rafraîchi: profileCompletionPercentage=${user.profileCompletionPercentage}")
                            // Sauvegarder le profil mis à jour
                            userPreferences.saveAuthResponse(
                                token,
                                userPreferences.getRefreshToken() ?: "",
                                user
                            )
                        }.onFailure { e ->
                            android.util.Log.e("CompleteProfileVM", "❌ Erreur rafraîchissement profil: ${e.message}")
                        }
                    }
                }
                
                _isSuccess.value = true
            }.onFailure { error ->
                android.util.Log.e("CompleteProfileVM", "❌ FAILURE: ${error.message}")
                android.util.Log.e("CompleteProfileVM", "Stack trace:", error)
                _errorMessage.value = error.message ?: "Erreur lors de la mise à jour du profil"
            }
            
            android.util.Log.d("CompleteProfileVM", "=== FIN submitProfile ===")
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
    
    // Charger le profil existant pour édition
    fun loadExistingProfile() {
        viewModelScope.launch {
            val token = userPreferences.getAccessToken()
            if (token != null) {
                val result = authRepository.getUserProfile(token)
                result.onSuccess { user ->
                    user.travelProfile?.let { profile ->
                        android.util.Log.d("CompleteProfileVM", "📥 Chargement du profil existant pour édition")
                        _isEditMode.value = true
                        _age.value = profile.age
                        _travelFrequency.value = profile.travelFrequency
                        _preferredDestinations.value = profile.preferredDestinations ?: emptyList()
                        _budgetRange.value = profile.budgetRange
                        _travelPurpose.value = profile.travelPurpose
                        _companionType.value = profile.companionType
                        _hasHealthConditions.value = profile.hasHealthConditions
                        android.util.Log.d("CompleteProfileVM", "✅ Profil chargé pour modification")
                    }
                }.onFailure { e ->
                    android.util.Log.e("CompleteProfileVM", "❌ Erreur chargement profil: ${e.message}")
                }
            }
        }
    }
}
