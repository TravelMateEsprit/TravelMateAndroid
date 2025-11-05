package com.travelmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.Insurance
import com.travelmate.data.service.InsuranceService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsurancesUserViewModel @Inject constructor(
    private val insuranceService: InsuranceService
) : ViewModel() {
    
    val insurances = insuranceService.insurances
    val mySubscriptions = insuranceService.mySubscriptions
    val isLoading = insuranceService.isLoading
    val error = insuranceService.error
    
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
