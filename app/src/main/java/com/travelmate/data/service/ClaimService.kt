package com.travelmate.data.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.travelmate.data.api.ClaimApi
import com.travelmate.data.models.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClaimService @Inject constructor(
    private val claimApi: ClaimApi,
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("travelmate_prefs", Context.MODE_PRIVATE)
    
    private val _myClaims = MutableStateFlow<List<Claim>>(emptyList())
    val myClaims: StateFlow<List<Claim>> = _myClaims.asStateFlow()
    
    private val _agencyClaims = MutableStateFlow<List<Claim>>(emptyList())
    val agencyClaims: StateFlow<List<Claim>> = _agencyClaims.asStateFlow()
    
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private fun getAuthToken(): String {
        val token = prefs.getString("access_token", "") ?: ""
        return "Bearer $token"
    }
    
    suspend fun createClaim(request: CreateClaimRequest): Result<Claim> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val response = claimApi.createClaim(getAuthToken(), request)
            
            if (response.isSuccessful && response.body() != null) {
                val claim = response.body()!!
                Log.d("ClaimService", "Réclamation créée avec succès: ${claim._id}")
                
                // Rafraîchir la liste des réclamations
                loadMyClaims()
                
                Result.success(claim)
            } else {
                val errorMsg = "Erreur lors de la création de la réclamation: ${response.code()}"
                _error.value = errorMsg
                Log.e("ClaimService", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            _error.value = errorMsg
            Log.e("ClaimService", errorMsg, e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun loadMyClaims() {
        try {
            _isLoading.value = true
            _error.value = null
            
            val response = claimApi.getMyClaims(getAuthToken())
            
            if (response.isSuccessful && response.body() != null) {
                _myClaims.value = response.body()!!
                Log.d("ClaimService", "Réclamations chargées: ${_myClaims.value.size}")
            } else {
                val errorMsg = "Erreur lors du chargement des réclamations: ${response.code()}"
                _error.value = errorMsg
                Log.e("ClaimService", errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            _error.value = errorMsg
            Log.e("ClaimService", errorMsg, e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun loadAgencyClaims() {
        try {
            _isLoading.value = true
            _error.value = null
            
            val response = claimApi.getAgencyClaims(getAuthToken())
            
            if (response.isSuccessful && response.body() != null) {
                _agencyClaims.value = response.body()!!
                Log.d("ClaimService", "Réclamations agence chargées: ${_agencyClaims.value.size}")
            } else {
                val errorMsg = "Erreur lors du chargement des réclamations: ${response.code()}"
                _error.value = errorMsg
                Log.e("ClaimService", errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            _error.value = errorMsg
            Log.e("ClaimService", errorMsg, e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun loadUnreadCount() {
        try {
            val response = claimApi.getUnreadCount(getAuthToken())
            
            if (response.isSuccessful && response.body() != null) {
                _unreadCount.value = response.body()!!.count
                Log.d("ClaimService", "Réclamations non lues: ${_unreadCount.value}")
            }
        } catch (e: Exception) {
            Log.e("ClaimService", "Erreur lors du chargement du compteur: ${e.message}", e)
        }
    }
    
    suspend fun getClaimById(claimId: String): Result<Claim> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val response = claimApi.getClaimById(claimId, getAuthToken())
            
            if (response.isSuccessful && response.body() != null) {
                val claim = response.body()!!
                Log.d("ClaimService", "Détails de la réclamation chargés: ${claim._id}")
                Result.success(claim)
            } else {
                val errorMsg = "Erreur lors du chargement de la réclamation: ${response.code()}"
                _error.value = errorMsg
                Log.e("ClaimService", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            _error.value = errorMsg
            Log.e("ClaimService", errorMsg, e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun updateClaimStatus(claimId: String, request: UpdateClaimStatusRequest): Result<Claim> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val response = claimApi.updateClaimStatus(claimId, getAuthToken(), request)
            
            if (response.isSuccessful && response.body() != null) {
                val updatedClaim = response.body()!!
                Log.d("ClaimService", "Réclamation mise à jour: ${updatedClaim._id}")
                
                // Rafraîchir la liste des réclamations
                loadAgencyClaims()
                
                Result.success(updatedClaim)
            } else {
                val errorMsg = "Erreur lors de la mise à jour: ${response.code()}"
                _error.value = errorMsg
                Log.e("ClaimService", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            _error.value = errorMsg
            Log.e("ClaimService", errorMsg, e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
