package com.travelmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.CreateReservationRequest
import com.travelmate.data.models.Reservation
import com.travelmate.data.service.ReservationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReservationsViewModel @Inject constructor(
    private val reservationService: ReservationService
) : ViewModel() {
    
    val reservations = reservationService.reservations
    val myReservations = reservationService.myReservations
    val isLoading = reservationService.isLoading
    val error = reservationService.error
    
    fun loadAllReservations() {
        viewModelScope.launch {
            reservationService.getAllReservations()
        }
    }
    
    fun loadMyReservations() {
        viewModelScope.launch {
            reservationService.getMyReservations()
        }
    }
    
    fun getReservationById(reservationId: String, onResult: (Result<Reservation>) -> Unit) {
        viewModelScope.launch {
            val result = reservationService.getReservationById(reservationId)
            onResult(result)
        }
    }
    
    fun createReservation(request: CreateReservationRequest, onResult: (Result<Reservation>) -> Unit) {
        viewModelScope.launch {
            val result = reservationService.createReservation(request)
            onResult(result)
        }
    }
    
    fun updateReservationStatus(reservationId: String, status: String, onResult: (Result<Reservation>) -> Unit) {
        viewModelScope.launch {
            val result = reservationService.updateReservationStatus(reservationId, status)
            onResult(result)
        }
    }
    
    fun deleteReservation(reservationId: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = reservationService.deleteReservation(reservationId)
            onResult(result)
        }
    }
    
    fun clearError() {
        reservationService.clearError()
    }
}

