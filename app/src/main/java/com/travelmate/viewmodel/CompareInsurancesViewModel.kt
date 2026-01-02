package com.travelmate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.ComparisonResult
import com.travelmate.data.models.Insurance
import com.travelmate.data.repository.InsuranceRepository
import com.travelmate.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompareInsurancesViewModel @Inject constructor(
    private val insuranceRepository: InsuranceRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _selectedInsurances = MutableStateFlow<List<Insurance>>(emptyList())
    val selectedInsurances: StateFlow<List<Insurance>> = _selectedInsurances.asStateFlow()
    
    private val _comparisonResult = MutableStateFlow<ComparisonResult?>(null)
    val comparisonResult: StateFlow<ComparisonResult?> = _comparisonResult.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun toggleInsuranceSelection(insurance: Insurance) {
        val currentList = _selectedInsurances.value.toMutableList()
        val index = currentList.indexOfFirst { it._id == insurance._id }
        
        if (index >= 0) {
            // Déjà sélectionné, on le retire
            currentList.removeAt(index)
        } else {
            // Pas sélectionné
            if (currentList.size >= 3) {
                _error.value = "Vous ne pouvez comparer que 3 assurances maximum"
                return
            }
            currentList.add(insurance)
        }
        
        _selectedInsurances.value = currentList
        _error.value = null
    }
    
    fun isSelected(insuranceId: String): Boolean {
        return _selectedInsurances.value.any { it._id == insuranceId }
    }
    
    fun clearSelection() {
        _selectedInsurances.value = emptyList()
        _comparisonResult.value = null
        _error.value = null
    }
    
    fun compareInsurances() {
        val selected = _selectedInsurances.value
        
        if (selected.size < 2) {
            _error.value = "Veuillez sélectionner au moins 2 assurances"
            return
        }
        
        if (selected.size > 3) {
            _error.value = "Vous ne pouvez comparer que 3 assurances maximum"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val token = userPreferences.getAccessToken() ?: run {
                    _error.value = "Token non disponible"
                    _isLoading.value = false
                    return@launch
                }
                
                val insuranceIds = selected.map { it._id }
                Log.d("CompareInsurancesVM", "Comparing insurances: $insuranceIds")
                
                val result = insuranceRepository.compareInsurances(token, insuranceIds)
                
                result.onSuccess { comparison ->
                    Log.d("CompareInsurancesVM", "Comparison successful: ${comparison.summary}")
                    _comparisonResult.value = comparison
                }.onFailure { exception ->
                    Log.e("CompareInsurancesVM", "Comparison failed", exception)
                    _error.value = exception.message ?: "Erreur lors de la comparaison"
                }
                
            } catch (e: Exception) {
                Log.e("CompareInsurancesVM", "Unexpected error", e)
                _error.value = e.message ?: "Erreur inattendue"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
