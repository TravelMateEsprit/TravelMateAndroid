package com.travelmate.ui.user.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.InsuranceRequest
import com.travelmate.data.models.RequestStatus
import com.travelmate.data.repository.InsuranceRequestRepository
import com.travelmate.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MyRequestsState {
    object Loading : MyRequestsState()
    data class Success(val requests: List<InsuranceRequest>) : MyRequestsState()
    data class Error(val message: String) : MyRequestsState()
}

@HiltViewModel
class MyInsuranceRequestsViewModel @Inject constructor(
    private val repository: InsuranceRequestRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _state = MutableStateFlow<MyRequestsState>(MyRequestsState.Loading)
    val state: StateFlow<MyRequestsState> = _state.asStateFlow()
    
    private val _selectedStatus = MutableStateFlow<RequestStatus?>(null)
    val selectedStatus: StateFlow<RequestStatus?> = _selectedStatus.asStateFlow()
    
    init {
        loadRequests()
    }
    
    fun loadRequests() {
        viewModelScope.launch {
            _state.value = MyRequestsState.Loading
            
            try {
                val token = userPreferences.getAccessToken() ?: run {
                    _state.value = MyRequestsState.Error("Non authentifié")
                    return@launch
                }
                
                val response = repository.getMyRequests(token)
                
                if (response.isSuccessful && response.body() != null) {
                    val requests = response.body()!!
                    _state.value = MyRequestsState.Success(
                        filterByStatus(requests, _selectedStatus.value)
                    )
                } else {
                    _state.value = MyRequestsState.Error(
                        "Erreur lors du chargement des demandes"
                    )
                }
            } catch (e: Exception) {
                _state.value = MyRequestsState.Error(
                    e.message ?: "Erreur réseau"
                )
            }
        }
    }
    
    fun filterByStatus(status: RequestStatus?) {
        _selectedStatus.value = status
        
        val currentState = _state.value
        if (currentState is MyRequestsState.Success) {
            val allRequests = currentState.requests
            _state.value = MyRequestsState.Success(
                filterByStatus(allRequests, status)
            )
        } else {
            loadRequests()
        }
    }
    
    private fun filterByStatus(requests: List<InsuranceRequest>, status: RequestStatus?): List<InsuranceRequest> {
        return if (status == null) {
            requests
        } else {
            requests.filter { it.status == status }
        }
    }
    
    fun cancelRequest(requestId: String) {
        viewModelScope.launch {
            try {
                val token = userPreferences.getAccessToken() ?: return@launch
                
                val response = repository.cancelRequest(token, requestId)
                
                if (response.isSuccessful) {
                    loadRequests() // Recharger la liste
                }
            } catch (e: Exception) {
                // Gérer l'erreur
            }
        }
    }
}
