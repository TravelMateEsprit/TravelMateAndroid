package com.travelmate.data.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.travelmate.data.api.ReservationApi
import com.travelmate.data.models.CreateReservationRequest
import com.travelmate.data.models.Reservation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReservationService @Inject constructor(
    private val reservationApi: ReservationApi,
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("travelmate_prefs", Context.MODE_PRIVATE)
    
    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    val reservations: StateFlow<List<Reservation>> = _reservations.asStateFlow()
    
    private val _myReservations = MutableStateFlow<List<Reservation>>(emptyList())
    val myReservations: StateFlow<List<Reservation>> = _myReservations.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private fun getAuthToken(): String {
        val token = prefs.getString("access_token", "") ?: ""
        return "Bearer $token"
    }
    
    suspend fun getAllReservations(): Result<List<Reservation>> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val response = reservationApi.getAllReservations(getAuthToken())
            
            if (response.isSuccessful && response.body() != null) {
                val reservations = response.body()!!
                _reservations.value = reservations
                Result.success(reservations)
            } else {
                val errorMsg = "Erreur lors du chargement des réservations - HTTP ${response.code()}"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Erreur réseau"
            _error.value = errorMsg
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun getMyReservations(): Result<List<Reservation>> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val token = getAuthToken()
            Log.d("ReservationService", "=== GET MY RESERVATIONS ===")
            
            val response = reservationApi.getMyReservations(token)
            Log.d("ReservationService", "Response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val reservations = response.body()!!
                Log.d("ReservationService", "Received ${reservations.size} reservations")
                _myReservations.value = reservations
                Result.success(reservations)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors du chargement de vos réservations - HTTP ${response.code()}"
                Log.e("ReservationService", errorMsg)
                Log.e("ReservationService", "Error body: $errorBody")
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Exception: ${e.javaClass.simpleName} - ${e.message}"
            Log.e("ReservationService", errorMsg, e)
            _error.value = errorMsg
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun getReservationById(reservationId: String): Result<Reservation> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val response = reservationApi.getReservationById(reservationId, getAuthToken())
            
            if (response.isSuccessful && response.body() != null) {
                val reservation = response.body()!!
                Result.success(reservation)
            } else {
                val errorMsg = "Erreur lors du chargement de la réservation - HTTP ${response.code()}"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Erreur réseau"
            _error.value = errorMsg
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun createReservation(request: CreateReservationRequest): Result<Reservation> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            Log.d("ReservationService", "=== CREATE RESERVATION ===")
            Log.d("ReservationService", "Voyage ID: ${request.id_voyage}, Prix: ${request.prix}")
            
            val response = reservationApi.createReservation(getAuthToken(), request)
            Log.d("ReservationService", "Response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val reservation = response.body()!!
                Log.d("ReservationService", "Successfully created reservation: ${reservation.id_reservation}")
                // Refresh lists
                getMyReservations()
                Result.success(reservation)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la création de la réservation - HTTP ${response.code()}"
                Log.e("ReservationService", errorMsg)
                Log.e("ReservationService", "Error body: $errorBody")
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Exception: ${e.javaClass.simpleName} - ${e.message}"
            Log.e("ReservationService", errorMsg, e)
            _error.value = errorMsg
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun updateReservationStatus(reservationId: String, status: String): Result<Reservation> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val response = reservationApi.updateReservationStatus(
                reservationId,
                getAuthToken(),
                mapOf("statut" to status)
            )
            
            if (response.isSuccessful && response.body() != null) {
                val reservation = response.body()!!
                // Refresh lists
                getMyReservations()
                Result.success(reservation)
            } else {
                val errorMsg = "Erreur lors de la modification du statut - HTTP ${response.code()}"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Erreur réseau"
            _error.value = errorMsg
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun deleteReservation(reservationId: String): Result<Unit> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val response = reservationApi.deleteReservation(reservationId, getAuthToken())
            
            if (response.isSuccessful) {
                // Refresh lists
                getMyReservations()
                Result.success(Unit)
            } else {
                val errorMsg = "Erreur lors de la suppression de la réservation - HTTP ${response.code()}"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Erreur réseau"
            _error.value = errorMsg
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}

