package com.travelmate.data.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.travelmate.data.api.ReservationApi
import com.travelmate.data.models.ApiErrorResponse
import com.travelmate.data.models.CreateReservationRequest
import com.travelmate.data.models.Reservation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
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
            Log.d("ReservationService", "Voyage ID: ${request.id_voyage}")
            Log.d("ReservationService", "Prix: ${request.prix}")
            Log.d("ReservationService", "Auth Token: ${getAuthToken()?.take(20)}...")
            Log.d("ReservationService", "Note: User ID is retrieved from token by backend")
            
            val response = reservationApi.createReservation(getAuthToken(), request)
            Log.d("ReservationService", "Response code: ${response.code()}")
            
            if (response.isSuccessful) {
                try {
                    val reservation = response.body()
                    if (reservation != null) {
                        Log.d("ReservationService", "Successfully created reservation: ${reservation.id_reservation}")
                        // Refresh lists
                        getMyReservations()
                        Result.success(reservation)
                    } else {
                        // Response body is null, try to get error from response
                        val errorBody = response.errorBody()?.string()
                        val errorMsg = errorBody ?: "Réponse vide du serveur - HTTP ${response.code()}"
                        Log.e("ReservationService", "Response body is null: $errorMsg")
                        _error.value = errorMsg
                        Result.failure(Exception(errorMsg))
                    }
                } catch (e: Exception) {
                    // JSON parsing error - log the raw response
                    val responseBody = try {
                        response.body()?.toString() ?: "No response body"
                    } catch (ex: Exception) {
                        "Error reading response body"
                    }
                    
                    Log.e("ReservationService", "=== JSON PARSING ERROR ===")
                    Log.e("ReservationService", "Error parsing response: ${e.message}")
                    Log.e("ReservationService", "Exception type: ${e.javaClass.simpleName}")
                    Log.e("ReservationService", "Response code: ${response.code()}")
                    Log.e("ReservationService", "Response body: $responseBody")
                    
                    val errorMsg = when {
                        e is kotlinx.serialization.SerializationException || 
                        e.javaClass.simpleName.contains("JsonDecoding") ||
                        e.javaClass.simpleName.contains("Serialization") -> {
                            "Erreur de format JSON du serveur. Le backend retourne des données dans un format inattendu. Vérifiez que l'API retourne des chaînes de caractères pour les IDs au lieu de données hexadécimales."
                        }
                        e.message?.contains("id_utilisateur") == true -> {
                            "Erreur: Le serveur retourne un format d'ID utilisateur invalide. Contactez l'administrateur système."
                        }
                        else -> {
                            "Erreur lors du parsing de la réponse: ${e.message}"
                        }
                    }
                    _error.value = errorMsg
                    Result.failure(e)
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = try {
                    if (errorBody != null) {
                        // Try to parse as JSON error response
                        try {
                            val json = Json { ignoreUnknownKeys = true }
                            val apiError = json.decodeFromString<ApiErrorResponse>(errorBody)
                            apiError.getErrorMessage()
                        } catch (e: Exception) {
                            // If parsing fails, use the raw error body
                            Log.d("ReservationService", "Error body is not JSON, using raw: $errorBody")
                            errorBody
                        }
                    } else {
                        "Erreur lors de la création de la réservation - HTTP ${response.code()}"
                    }
                } catch (e: Exception) {
                    Log.e("ReservationService", "Error parsing error response: ${e.message}")
                    errorBody ?: "Erreur lors de la création de la réservation - HTTP ${response.code()}"
                }
                Log.e("ReservationService", "HTTP ${response.code()}: $errorMsg")
                Log.e("ReservationService", "Request sent: id_voyage=${request.id_voyage}, prix=${request.prix}")
                Log.e("ReservationService", "Raw error body: $errorBody")
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
    
    suspend fun cancelReservation(reservationId: String): Result<Reservation> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            Log.d("ReservationService", "=== CANCEL RESERVATION ===")
            Log.d("ReservationService", "Reservation ID: $reservationId")
            
            val token = getAuthToken()
            val response = reservationApi.updateReservationStatus(
                reservationId,
                token,
                mapOf("statut" to "annulee")
            )
            
            Log.d("ReservationService", "Cancel response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val cancelledReservation = response.body()!!
                Log.d("ReservationService", "Successfully cancelled reservation: ${cancelledReservation.id_reservation}")
                
                // Refresh the reservations list
                getMyReservations()
                
                Result.success(cancelledReservation)
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e("ReservationService", "Cancel error response: $errorBody")
                val errorMsg = "Erreur lors de l'annulation de la réservation: ${response.message()}"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur lors de l'annulation: ${e.message}"
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

