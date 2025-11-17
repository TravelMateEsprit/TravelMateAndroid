package com.travelmate.ui.agency.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.InsuranceRequest
import com.travelmate.data.models.InsuranceRequestStats
import com.travelmate.data.models.RequestStatus
import com.travelmate.data.repository.InsuranceRequestRepository
import com.travelmate.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AgencyRequestsState {
    object Loading : AgencyRequestsState()
    data class Success(
        val requests: List<InsuranceRequest>,
        val stats: InsuranceRequestStats? = null
    ) : AgencyRequestsState()
    data class Error(val message: String) : AgencyRequestsState()
}

sealed class ReviewRequestState {
    object Idle : ReviewRequestState()
    object Loading : ReviewRequestState()
    data class Success(val request: InsuranceRequest) : ReviewRequestState()
    data class Error(val message: String) : ReviewRequestState()
}

@HiltViewModel
class AgencyInsuranceRequestsViewModel @Inject constructor(
    private val repository: InsuranceRequestRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _state = MutableStateFlow<AgencyRequestsState>(AgencyRequestsState.Loading)
    val state: StateFlow<AgencyRequestsState> = _state.asStateFlow()
    
    private val _reviewState = MutableStateFlow<ReviewRequestState>(ReviewRequestState.Idle)
    val reviewState: StateFlow<ReviewRequestState> = _reviewState.asStateFlow()
    
    private val _selectedStatus = MutableStateFlow<RequestStatus?>(null)
    val selectedStatus: StateFlow<RequestStatus?> = _selectedStatus.asStateFlow()
    
    init {
        loadRequests()
        loadStats()
    }
    
    fun loadRequests(status: RequestStatus? = null) {
        viewModelScope.launch {
            _state.value = AgencyRequestsState.Loading
            _selectedStatus.value = status
            
            try {
                val token = userPreferences.getAccessToken() ?: run {
                    _state.value = AgencyRequestsState.Error("Non authentifié")
                    return@launch
                }
                
                val response = repository.getAgencyRequests(token, status)
                
                if (response.isSuccessful && response.body() != null) {
                    val currentState = _state.value
                    val stats = if (currentState is AgencyRequestsState.Success) {
                        currentState.stats
                    } else {
                        null
                    }
                    
                    _state.value = AgencyRequestsState.Success(
                        requests = response.body()!!,
                        stats = stats
                    )
                } else {
                    _state.value = AgencyRequestsState.Error(
                        "Erreur lors du chargement des demandes"
                    )
                }
            } catch (e: Exception) {
                _state.value = AgencyRequestsState.Error(
                    e.message ?: "Erreur réseau"
                )
            }
        }
    }
    
    fun loadStats() {
        viewModelScope.launch {
            try {
                val token = userPreferences.getAccessToken() ?: return@launch
                
                val response = repository.getAgencyStats(token)
                
                if (response.isSuccessful && response.body() != null) {
                    val currentState = _state.value
                    if (currentState is AgencyRequestsState.Success) {
                        _state.value = currentState.copy(stats = response.body())
                    }
                }
            } catch (e: Exception) {
                // Gérer l'erreur silencieusement
            }
        }
    }
    
    fun markAsRead(requestId: String) {
        viewModelScope.launch {
            try {
                val token = userPreferences.getAccessToken() ?: return@launch
                repository.markAsRead(token, requestId)
            } catch (e: Exception) {
                // Gérer l'erreur
            }
        }
    }
    
    fun approveRequest(requestId: String, response: String) {
        reviewRequest(requestId, RequestStatus.APPROVED, response)
    }
    
    fun rejectRequest(requestId: String, response: String) {
        reviewRequest(requestId, RequestStatus.REJECTED, response)
    }
    
    private fun reviewRequest(requestId: String, status: RequestStatus, response: String) {
        viewModelScope.launch {
            _reviewState.value = ReviewRequestState.Loading
            
            try {
                val token = userPreferences.getAccessToken() ?: run {
                    _reviewState.value = ReviewRequestState.Error("Non authentifié")
                    return@launch
                }
                
                val apiResponse = repository.reviewRequest(token, requestId, status, response)
                
                if (apiResponse.isSuccessful && apiResponse.body() != null) {
                    _reviewState.value = ReviewRequestState.Success(apiResponse.body()!!)
                    loadRequests(_selectedStatus.value) // Recharger la liste
                    loadStats() // Recharger les stats
                } else {
                    _reviewState.value = ReviewRequestState.Error(
                        "Erreur lors du traitement de la demande"
                    )
                }
            } catch (e: Exception) {
                _reviewState.value = ReviewRequestState.Error(
                    e.message ?: "Erreur réseau"
                )
            }
        }
    }
    
    fun resetReviewState() {
        _reviewState.value = ReviewRequestState.Idle
    }
}
