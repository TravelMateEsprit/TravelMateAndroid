package com.travelmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.CreateVoyageRequest
import com.travelmate.data.models.Voyage
import com.travelmate.data.service.VoyageService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoyagesViewModel @Inject constructor(
    private val voyageService: VoyageService
) : ViewModel() {
    
    val voyages = voyageService.voyages
    val isLoading = voyageService.isLoading
    val error = voyageService.error
    
    fun loadAllVoyages() {
        viewModelScope.launch {
            voyageService.getAllVoyages()
        }
    }
    
    fun getVoyageById(voyageId: String, onResult: (Result<Voyage>) -> Unit) {
        viewModelScope.launch {
            val result = voyageService.getVoyageById(voyageId)
            onResult(result)
        }
    }
    
    fun createVoyage(request: CreateVoyageRequest, onResult: (Result<Voyage>) -> Unit) {
        viewModelScope.launch {
            val result = voyageService.createVoyage(request)
            onResult(result)
        }
    }
    
    fun updateVoyage(voyageId: String, request: CreateVoyageRequest, onResult: (Result<Voyage>) -> Unit) {
        viewModelScope.launch {
            val result = voyageService.updateVoyage(voyageId, request)
            onResult(result)
        }
    }
    
    fun deleteVoyage(voyageId: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = voyageService.deleteVoyage(voyageId)
            onResult(result)
        }
    }
    
    fun clearError() {
        voyageService.clearError()
    }
}

