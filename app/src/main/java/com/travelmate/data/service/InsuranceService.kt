package com.travelmate.data.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.travelmate.data.api.InsuranceApi
import com.travelmate.data.models.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsuranceService @Inject constructor(
    private val insuranceApi: InsuranceApi,
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("travelmate_prefs", Context.MODE_PRIVATE)
    
    private val _insurances = MutableStateFlow<List<Insurance>>(emptyList())
    val insurances: StateFlow<List<Insurance>> = _insurances.asStateFlow()
    
    private val _mySubscriptions = MutableStateFlow<List<Insurance>>(emptyList())
    val mySubscriptions: StateFlow<List<Insurance>> = _mySubscriptions.asStateFlow()
    
    private val _myAgencyInsurances = MutableStateFlow<List<Insurance>>(emptyList())
    val myAgencyInsurances: StateFlow<List<Insurance>> = _myAgencyInsurances.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private fun getAuthToken(): String {
        val token = prefs.getString("access_token", "") ?: ""
        return "Bearer $token"
    }
    
    // ========== User Methods ==========
    
    suspend fun getAllInsurances(): Result<List<Insurance>> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val token = getAuthToken()
            Log.d("InsuranceService", "=== GET ALL INSURANCES ===")
            Log.d("InsuranceService", "Auth token: $token")
            
            val response = insuranceApi.getAllInsurances(token)
            Log.d("InsuranceService", "Response code: ${response.code()}")
            Log.d("InsuranceService", "Response message: ${response.message()}")
            Log.d("InsuranceService", "Is successful: ${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("InsuranceService", "Response body is null: ${body == null}")
                
                if (body != null) {
                    Log.d("InsuranceService", "Received ${body.size} insurances")
                    body.forEachIndexed { index, insurance ->
                        Log.d("InsuranceService", "[$index] ${insurance.name} - ${insurance._id}")
                    }
                    _insurances.value = body
                    Result.success(body)
                } else {
                    val errorMsg = "Response body is null"
                    Log.e("InsuranceService", errorMsg)
                    _error.value = errorMsg
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                Log.e("InsuranceService", errorMsg)
                Log.e("InsuranceService", "Error body: $errorBody")
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Exception: ${e.javaClass.simpleName} - ${e.message}"
            Log.e("InsuranceService", errorMsg, e)
            e.printStackTrace()
            _error.value = errorMsg
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun getMySubscriptions(): Result<List<Insurance>> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val response = insuranceApi.getMySubscriptions(getAuthToken())
            if (response.isSuccessful && response.body() != null) {
                val subscriptions = response.body()!!
                _mySubscriptions.value = subscriptions
                Result.success(subscriptions)
            } else {
                val errorMsg = "Erreur lors du chargement de vos inscriptions"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            _error.value = e.message ?: "Erreur réseau"
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun subscribeToInsurance(insuranceId: String): Result<Insurance> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val response = insuranceApi.subscribeToInsurance(insuranceId, getAuthToken())
            if (response.isSuccessful && response.body() != null) {
                val insurance = response.body()!!
                // Refresh lists
                getMySubscriptions()
                getAllInsurances()
                Result.success(insurance)
            } else {
                val errorMsg = "Erreur lors de l'inscription"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            _error.value = e.message ?: "Erreur réseau"
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun unsubscribeFromInsurance(insuranceId: String): Result<Insurance> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val response = insuranceApi.unsubscribeFromInsurance(insuranceId, getAuthToken())
            if (response.isSuccessful && response.body() != null) {
                val insurance = response.body()!!
                // Refresh lists
                getMySubscriptions()
                getAllInsurances()
                Result.success(insurance)
            } else {
                val errorMsg = "Erreur lors de la désinscription"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            _error.value = e.message ?: "Erreur réseau"
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    // ========== Agency Methods ==========
    
    suspend fun createInsurance(request: CreateInsuranceRequest): Result<Insurance> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            Log.d("InsuranceService", "=== CREATE INSURANCE ===")
            Log.d("InsuranceService", "Request: name=${request.name}, price=${request.price}, duration=${request.duration}")
            
            val response = insuranceApi.createInsurance(getAuthToken(), request)
            Log.d("InsuranceService", "Response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val insurance = response.body()!!
                Log.d("InsuranceService", "Successfully created insurance: ${insurance._id}")
                // Refresh agency insurances
                getMyAgencyInsurances()
                Result.success(insurance)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la création de l'assurance - HTTP ${response.code()}: ${response.message()}"
                Log.e("InsuranceService", errorMsg)
                Log.e("InsuranceService", "Error body: $errorBody")
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Exception lors de la création: ${e.javaClass.simpleName} - ${e.message}"
            Log.e("InsuranceService", errorMsg, e)
            e.printStackTrace()
            _error.value = errorMsg
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun getMyAgencyInsurances(): Result<List<Insurance>> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val token = getAuthToken()
            Log.d("InsuranceService", "=== GET MY AGENCY INSURANCES ===")
            Log.d("InsuranceService", "Auth token: $token")
            
            val response = insuranceApi.getMyInsurances(token)
            Log.d("InsuranceService", "Response code: ${response.code()}")
            Log.d("InsuranceService", "Response message: ${response.message()}")
            Log.d("InsuranceService", "Is successful: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val insurances = response.body()!!
                Log.d("InsuranceService", "Received ${insurances.size} agency insurances")
                insurances.forEachIndexed { index, insurance ->
                    Log.d("InsuranceService", "[$index] ${insurance.name} - ${insurance._id} - Active: ${insurance.isActive}")
                }
                _myAgencyInsurances.value = insurances
                Result.success(insurances)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors du chargement de vos assurances - HTTP ${response.code()}: ${response.message()}"
                Log.e("InsuranceService", errorMsg)
                Log.e("InsuranceService", "Error body: $errorBody")
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Exception: ${e.javaClass.simpleName} - ${e.message}"
            Log.e("InsuranceService", errorMsg, e)
            e.printStackTrace()
            _error.value = errorMsg
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun updateInsurance(insuranceId: String, request: UpdateInsuranceRequest): Result<Insurance> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            Log.d("InsuranceService", "=== UPDATE INSURANCE ===")
            Log.d("InsuranceService", "Insurance ID: $insuranceId")
            Log.d("InsuranceService", "Request: ${request}")
            
            val response = insuranceApi.updateInsurance(insuranceId, getAuthToken(), request)
            Log.d("InsuranceService", "Response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val insurance = response.body()!!
                Log.d("InsuranceService", "Successfully updated insurance: ${insurance._id}")
                // Refresh agency insurances
                getMyAgencyInsurances()
                Result.success(insurance)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la modification - HTTP ${response.code()}: ${response.message()}"
                Log.e("InsuranceService", errorMsg)
                Log.e("InsuranceService", "Error body: $errorBody")
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Exception lors de la modification: ${e.javaClass.simpleName} - ${e.message}"
            Log.e("InsuranceService", errorMsg, e)
            e.printStackTrace()
            _error.value = errorMsg
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun deleteInsurance(insuranceId: String): Result<Unit> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            Log.d("InsuranceService", "=== DELETE INSURANCE ===")
            Log.d("InsuranceService", "Insurance ID: $insuranceId")
            
            val response = insuranceApi.deleteInsurance(insuranceId, getAuthToken())
            Log.d("InsuranceService", "Response code: ${response.code()}")
            
            if (response.isSuccessful) {
                Log.d("InsuranceService", "Successfully deleted insurance: $insuranceId")
                // Refresh agency insurances
                getMyAgencyInsurances()
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la suppression - HTTP ${response.code()}: ${response.message()}"
                Log.e("InsuranceService", errorMsg)
                Log.e("InsuranceService", "Error body: $errorBody")
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Exception lors de la suppression: ${e.javaClass.simpleName} - ${e.message}"
            Log.e("InsuranceService", errorMsg, e)
            e.printStackTrace()
            _error.value = errorMsg
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun getInsuranceSubscribers(insuranceId: String): Result<List<User>> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val response = insuranceApi.getInsuranceSubscribers(insuranceId, getAuthToken())
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = "Erreur lors du chargement des inscrits"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            _error.value = e.message ?: "Erreur réseau"
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
