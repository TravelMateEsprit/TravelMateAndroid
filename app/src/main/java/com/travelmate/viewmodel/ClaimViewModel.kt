package com.travelmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.*
import com.travelmate.data.service.ClaimService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClaimViewModel @Inject constructor(
    private val claimService: ClaimService
) : ViewModel() {
    
    val myClaims: StateFlow<List<Claim>> = claimService.myClaims
    val agencyClaims: StateFlow<List<Claim>> = claimService.agencyClaims
    val unreadCount: StateFlow<Int> = claimService.unreadCount
    val isLoading: StateFlow<Boolean> = claimService.isLoading
    val error: StateFlow<String?> = claimService.error
    
    private val _createClaimSuccess = MutableStateFlow(false)
    val createClaimSuccess: StateFlow<Boolean> = _createClaimSuccess.asStateFlow()
    
    private val _updateClaimSuccess = MutableStateFlow(false)
    val updateClaimSuccess: StateFlow<Boolean> = _updateClaimSuccess.asStateFlow()
    
    private val _addMessageSuccess = MutableStateFlow(false)
    val addMessageSuccess: StateFlow<Boolean> = _addMessageSuccess.asStateFlow()
    
    private val _selectedClaim = MutableStateFlow<Claim?>(null)
    val selectedClaim: StateFlow<Claim?> = _selectedClaim.asStateFlow()
    
    fun loadMyClaims() {
        viewModelScope.launch {
            claimService.loadMyClaims()
        }
    }
    
    fun loadAgencyClaims() {
        viewModelScope.launch {
            claimService.loadAgencyClaims()
        }
    }
    
    fun loadUnreadCount() {
        viewModelScope.launch {
            claimService.loadUnreadCount()
        }
    }
    
    fun createClaim(
        insuranceRequestId: String,
        subject: String,
        description: String,
        category: String,
        priority: String = "MOYENNE",
        attachments: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _createClaimSuccess.value = false
            
            val request = CreateClaimRequest(
                insuranceRequestId = insuranceRequestId,
                subject = subject,
                description = description,
                category = category,
                priority = priority,
                attachments = attachments
            )
            
            val result = claimService.createClaim(request)
            _createClaimSuccess.value = result.isSuccess
        }
    }
    
    fun loadClaimById(claimId: String) {
        viewModelScope.launch {
            val result = claimService.getClaimById(claimId)
            if (result.isSuccess) {
                _selectedClaim.value = result.getOrNull()
            }
        }
    }
    
    fun addMessage(
        claimId: String,
        message: String,
        attachments: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _addMessageSuccess.value = false
            
            val request = AddMessageRequest(
                message = message,
                attachments = attachments
            )
            
            val result = claimService.addMessage(claimId, request)
            if (result.isSuccess) {
                _selectedClaim.value = result.getOrNull()
                _addMessageSuccess.value = true
            }
        }
    }
    
    fun updateClaimStatus(
        claimId: String,
        status: String,
        comment: String? = null,
        priority: String? = null
    ) {
        viewModelScope.launch {
            _updateClaimSuccess.value = false
            
            val request = UpdateClaimStatusRequest(
                status = status,
                comment = comment,
                priority = priority
            )
            
            val result = claimService.updateClaimStatus(claimId, request)
            if (result.isSuccess) {
                _selectedClaim.value = result.getOrNull()
                _updateClaimSuccess.value = true
            }
        }
    }
    
    fun resetCreateClaimSuccess() {
        _createClaimSuccess.value = false
    }
    
    fun resetUpdateClaimSuccess() {
        _updateClaimSuccess.value = false
    }
    
    fun resetAddMessageSuccess() {
        _addMessageSuccess.value = false
    }
    
    fun clearError() {
        claimService.clearError()
    }
}
