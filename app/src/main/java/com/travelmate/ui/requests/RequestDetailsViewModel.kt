package com.travelmate.ui.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.InsuranceRequest
import com.travelmate.data.repository.InsuranceRequestRepository
import com.travelmate.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RequestDetailsState {
    object Loading : RequestDetailsState()
    data class Success(val request: InsuranceRequest) : RequestDetailsState()
    data class Error(val message: String) : RequestDetailsState()
}

sealed class CancelRequestState {
    object Idle : CancelRequestState()
    object Loading : CancelRequestState()
    object Success : CancelRequestState()
    data class Error(val message: String) : CancelRequestState()
}

@HiltViewModel
class RequestDetailsViewModel @Inject constructor(
    private val repository: InsuranceRequestRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _state = MutableStateFlow<RequestDetailsState>(RequestDetailsState.Loading)
    val state: StateFlow<RequestDetailsState> = _state.asStateFlow()
    
    private val _cancelState = MutableStateFlow<CancelRequestState>(CancelRequestState.Idle)
    val cancelState: StateFlow<CancelRequestState> = _cancelState.asStateFlow()
    
    fun loadRequestDetails(requestId: String) {
        viewModelScope.launch {
            _state.value = RequestDetailsState.Loading
            
            try {
                val token = userPreferences.getAccessToken() ?: return@launch
                val response = repository.getRequestById(token, requestId)
                
                if (response.isSuccessful && response.body() != null) {
                    _state.value = RequestDetailsState.Success(response.body()!!)
                } else {
                    _state.value = RequestDetailsState.Error(
                        "Erreur lors du chargement des détails"
                    )
                }
            } catch (e: Exception) {
                _state.value = RequestDetailsState.Error(
                    e.message ?: "Erreur réseau"
                )
            }
        }
    }
    
    fun cancelRequest(requestId: String) {
        viewModelScope.launch {
            _cancelState.value = CancelRequestState.Loading
            
            try {
                val token = userPreferences.getAccessToken() ?: return@launch
                val response = repository.cancelRequest(token, requestId)
                
                if (response.isSuccessful && response.body() != null) {
                    _cancelState.value = CancelRequestState.Success
                    _state.value = RequestDetailsState.Success(response.body()!!)
                } else {
                    _cancelState.value = CancelRequestState.Error(
                        "Erreur lors de l'annulation"
                    )
                }
            } catch (e: Exception) {
                _cancelState.value = CancelRequestState.Error(
                    e.message ?: "Erreur réseau"
                )
            }
        }
    }
    
    fun resetCancelState() {
        _cancelState.value = CancelRequestState.Idle
    }
}
