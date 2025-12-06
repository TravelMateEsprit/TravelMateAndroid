package com.travelmate.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.Insurance
import com.travelmate.data.models.UpdateInsuranceRequest
import com.travelmate.data.service.InsuranceService
import com.travelmate.data.service.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class AgencyStats(
    val totalInsurances: Int = 0,
    val totalSubscribers: Int = 0,
    val activeInsurances: Int = 0,
    val estimatedRevenue: Double = 0.0
)

@HiltViewModel
class AgencyDashboardViewModel @Inject constructor(
    private val insuranceService: InsuranceService,
    private val authService: AuthService
) : ViewModel() {
    
    val myInsurances = insuranceService.myAgencyInsurances
    val isLoading = insuranceService.isLoading
    val error = insuranceService.error
    
    private val _stats = MutableStateFlow(AgencyStats())
    val stats: StateFlow<AgencyStats> = _stats.asStateFlow()
    
    fun loadMyInsurances() {
        viewModelScope.launch {
            android.util.Log.d("AgencyDashboardVM", "=== LOADING MY INSURANCES ===")
            insuranceService.getMyAgencyInsurances().onSuccess { insurances ->
                android.util.Log.d("AgencyDashboardVM", "Successfully loaded ${insurances.size} insurances")
                calculateStats(insurances)
            }.onFailure { error ->
                android.util.Log.e("AgencyDashboardVM", "Failed to load insurances: ${error.message}", error)
            }
        }
    }
    
    private fun calculateStats(insurances: List<Insurance>) {
        val total = insurances.size
        val active = insurances.count { it.isActive }
        val totalSubs = insurances.sumOf { it.subscribers.size }
        val revenue = insurances.sumOf { it.price * it.subscribers.size }
        
        _stats.value = AgencyStats(
            totalInsurances = total,
            totalSubscribers = totalSubs,
            activeInsurances = active,
            estimatedRevenue = revenue
        )
    }
    
    fun deleteInsurance(insuranceId: String) {
        viewModelScope.launch {
            insuranceService.deleteInsurance(insuranceId).onSuccess {
                loadMyInsurances()
            }
        }
    }
    
    fun toggleInsuranceActive(insuranceId: String, isActive: Boolean) {
        viewModelScope.launch {
            val update = UpdateInsuranceRequest(isActive = isActive)
            insuranceService.updateInsurance(insuranceId, update).onSuccess {
                loadMyInsurances()
            }
        }
    }

    fun createInsurance(request: com.travelmate.data.models.CreateInsuranceRequest) {
        viewModelScope.launch {
            insuranceService.createInsurance(request).onSuccess {
                loadMyInsurances()
            }.onFailure { err ->
                android.util.Log.e("AgencyDashboardVM", "Failed to create insurance: ${err.message}", err)
            }
        }
    }

    fun editInsurance(insuranceId: String, request: com.travelmate.data.models.UpdateInsuranceRequest) {
        viewModelScope.launch {
            insuranceService.updateInsurance(insuranceId, request).onSuccess {
                loadMyInsurances()
            }.onFailure { err ->
                android.util.Log.e("AgencyDashboardVM", "Failed to update insurance: ${err.message}", err)
            }
        }
    }
    
    suspend fun getInsuranceSubscribers(insuranceId: String) = insuranceService.getInsuranceSubscribers(insuranceId)

    suspend fun uploadSignature(context: Context, imageUri: Uri): Boolean {
        return try {
            // Copier le fichier URI vers un fichier temporaire
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val tempFile = File(context.cacheDir, "signature_temp.png")
            inputStream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Créer le MultipartBody.Part
            val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("signature", tempFile.name, requestFile)

            // Uploader
            val result = authService.uploadSignature(body)
            
            // Nettoyer le fichier temporaire
            tempFile.delete()
            
            result.isSuccess
        } catch (e: Exception) {
            android.util.Log.e("AgencyDashboardVM", "Failed to upload signature: ${e.message}", e)
            false
        }
    }

    suspend fun updateSignatureName(name: String): Boolean {
        return try {
            val result = authService.updateSignatureName(mapOf("signatureName" to name))
            result.isSuccess
        } catch (e: Exception) {
            android.util.Log.e("AgencyDashboardVM", "Failed to update signature name: ${e.message}", e)
            false
        }
    }
}
