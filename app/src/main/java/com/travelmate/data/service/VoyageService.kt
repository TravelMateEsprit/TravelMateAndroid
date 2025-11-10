package com.travelmate.data.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.travelmate.data.api.VoyageApi
import com.travelmate.data.models.CreateVoyageRequest
import com.travelmate.data.models.Voyage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoyageService @Inject constructor(
    private val voyageApi: VoyageApi,
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("travelmate_prefs", Context.MODE_PRIVATE)
    
    private val _voyages = MutableStateFlow<List<Voyage>>(emptyList())
    val voyages: StateFlow<List<Voyage>> = _voyages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private fun getAuthToken(): String {
        val token = prefs.getString("access_token", "") ?: ""
        return "Bearer $token"
    }
    
    suspend fun getAllVoyages(): Result<List<Voyage>> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val token = getAuthToken()
            Log.d("VoyageService", "=== GET ALL VOYAGES ===")
            Log.d("VoyageService", "Auth token: $token")
            
            val response = voyageApi.getAllVoyages(token)
            Log.d("VoyageService", "Response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val voyages = response.body()!!
                Log.d("VoyageService", "Received ${voyages.size} voyages")
                _voyages.value = voyages
                Result.success(voyages)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors du chargement des voyages - HTTP ${response.code()}"
                Log.e("VoyageService", errorMsg)
                Log.e("VoyageService", "Error body: $errorBody")
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Exception: ${e.javaClass.simpleName} - ${e.message}"
            Log.e("VoyageService", errorMsg, e)
            _error.value = errorMsg
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun getVoyageById(voyageId: String): Result<Voyage> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val response = voyageApi.getVoyageById(voyageId, getAuthToken())
            
            if (response.isSuccessful && response.body() != null) {
                val voyage = response.body()!!
                Result.success(voyage)
            } else {
                val errorMsg = "Erreur lors du chargement du voyage - HTTP ${response.code()}"
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
    
    suspend fun createVoyage(request: CreateVoyageRequest): Result<Voyage> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            // Log the request data
            Log.d("VoyageService", "=== CREATE VOYAGE REQUEST ===")
            Log.d("VoyageService", "Destination: ${request.destination}")
            Log.d("VoyageService", "Date de départ: ${request.date_depart}")
            Log.d("VoyageService", "Date de retour: ${request.date_retour}")
            Log.d("VoyageService", "Type: ${request.type}")
            Log.d("VoyageService", "Prix estimé: ${request.prix_estime}")
            Log.d("VoyageService", "Nombre de places: ${request.nombre_places}")
            Log.d("VoyageService", "Description: ${request.description}")
            Log.d("VoyageService", "Image URL: ${request.imageUrl}")
            
            val token = getAuthToken()
            Log.d("VoyageService", "Auth token: $token")
            
            val response = voyageApi.createVoyage(token, request)
            
            // Log response details
            Log.d("VoyageService", "Response code: ${response.code()}")
            Log.d("VoyageService", "Response message: ${response.message()}")
            Log.d("VoyageService", "Response headers: ${response.headers()}")
            
            if (response.isSuccessful) {
                response.body()?.let { voyage ->
                    Log.d("VoyageService", "Voyage created successfully: $voyage")
                    // Refresh list
                    getAllVoyages()
                    Result.success(voyage)
                } ?: run {
                    val errorMsg = "Réponse vide du serveur lors de la création du voyage"
                    Log.e("VoyageService", errorMsg)
                    _error.value = errorMsg
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la création du voyage - HTTP ${response.code()}: ${response.message()}\n$errorBody"
                Log.e("VoyageService", errorMsg)
                Log.e("VoyageService", "Error body: $errorBody")
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Exception lors de la création du voyage: ${e.javaClass.simpleName} - ${e.message}"
            Log.e("VoyageService", errorMsg, e)
            _error.value = errorMsg
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun updateVoyage(voyageId: String, request: CreateVoyageRequest): Result<Voyage> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val response = voyageApi.updateVoyage(voyageId, getAuthToken(), request)
            
            if (response.isSuccessful && response.body() != null) {
                val voyage = response.body()!!
                // Refresh list
                getAllVoyages()
                Result.success(voyage)
            } else {
                val errorMsg = "Erreur lors de la modification du voyage - HTTP ${response.code()}"
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
    
    suspend fun deleteVoyage(voyageId: String): Result<Unit> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val response = voyageApi.deleteVoyage(voyageId, getAuthToken())
            
            if (response.isSuccessful) {
                // Refresh list
                getAllVoyages()
                Result.success(Unit)
            } else {
                val errorMsg = "Erreur lors de la suppression du voyage - HTTP ${response.code()}"
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

