package com.travelmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.Insurance
import com.travelmate.data.models.InsuranceRecommendation
import com.travelmate.data.repository.InsuranceRepository
import com.travelmate.data.service.InsuranceService
import com.travelmate.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsurancesUserViewModel @Inject constructor(
    private val insuranceService: InsuranceService,
    private val insuranceRepository: InsuranceRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    val insurances = insuranceService.insurances
    val mySubscriptions = insuranceService.mySubscriptions
    val isLoading = insuranceService.isLoading
    val error = insuranceService.error
    
    // AI Recommendations
    private val _recommendations = MutableStateFlow<List<InsuranceRecommendation>>(emptyList())
    val recommendations: StateFlow<List<InsuranceRecommendation>> = _recommendations.asStateFlow()
    
    private val _isLoadingRecommendations = MutableStateFlow(false)
    val isLoadingRecommendations: StateFlow<Boolean> = _isLoadingRecommendations.asStateFlow()
    
    private val _recommendationsError = MutableStateFlow<String?>(null)
    val recommendationsError: StateFlow<String?> = _recommendationsError.asStateFlow()
    
    fun loadAllInsurances() {
        viewModelScope.launch {
            insuranceService.getAllInsurances()
        }
    }
    
    fun loadMySubscriptions() {
        viewModelScope.launch {
            insuranceService.getMySubscriptions()
        }
    }
    
    fun loadRecommendations() {
        viewModelScope.launch {
            _isLoadingRecommendations.value = true
            _recommendationsError.value = null
            val token = userPreferences.getAccessToken()
            if (token != null) {
                val result = insuranceRepository.getRecommendations(token)
                result.onSuccess { response ->
                    android.util.Log.d("InsurancesUserVM", "✅ Recommandations chargées: ${response.recommendations.size}")
                    _recommendations.value = response.recommendations
                    
                    // NOUVEAU: Vider les anciennes recommandations avant de sauvegarder les nouvelles
                    userPreferences.saveRecommendedInsuranceNames(emptySet())
                    android.util.Log.d("InsurancesUserVM", "🗑️ Anciennes recommandations effacées")
                    
                    // Sauvegarder les noms des assurances recommandées pour afficher le badge
                    val recommendedNames = response.recommendations.map { it.insuranceName }.toSet()
                    userPreferences.saveRecommendedInsuranceNames(recommendedNames)
                    android.util.Log.d("InsurancesUserVM", "💾 Nouveaux noms d'assurances sauvegardés: $recommendedNames")
                }.onFailure { e ->
                    android.util.Log.e("InsurancesUserVM", "❌ Erreur chargement recommandations: ${e.message}")
                    _recommendationsError.value = e.message
                }
            } else {
                android.util.Log.w("InsurancesUserVM", "⚠️ Pas de token - impossible de charger les recommandations")
            }
            _isLoadingRecommendations.value = false
        }
    }
    
    fun subscribeToInsurance(insuranceId: String) {
        viewModelScope.launch {
            insuranceService.subscribeToInsurance(insuranceId)
        }
    }
    
    fun unsubscribeFromInsurance(insuranceId: String) {
        viewModelScope.launch {
            insuranceService.unsubscribeFromInsurance(insuranceId)
        }
    }
}
