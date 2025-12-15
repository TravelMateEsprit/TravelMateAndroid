package com.travelmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.Reservation
import com.travelmate.data.models.ReservationStatus
import com.travelmate.data.repository.ReservationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AgencyReservationsViewModel @Inject constructor(
    private val reservationsRepository: ReservationsRepository
) : ViewModel() {

    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    val reservations: StateFlow<List<Reservation>> = _reservations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Load all reservations for agency's packs
     */
    fun loadReservations() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _reservations.value = reservationsRepository.getAgencyReservations()
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors du chargement"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Accept a reservation
     */
    fun acceptReservation(reservationId: String) {
        updateStatus(reservationId) {
            reservationsRepository.acceptReservation(reservationId)
        }
    }

    /**
     * Reject a reservation
     */
    fun rejectReservation(reservationId: String) {
        updateStatus(reservationId) {
            reservationsRepository.rejectReservation(reservationId)
        }
    }

    private fun updateStatus(
        reservationId: String,
        action: suspend () -> Result<Reservation>
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = action()
                if (result.isSuccess) {
                    val updated = result.getOrNull()
                    if (updated != null) {
                        _reservations.value = _reservations.value.map {
                            if (it.id == reservationId) updated else it
                        }
                    }
                } else {
                    _error.value = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear error
     */
    fun clearError() {
        _error.value = null
    }
}
