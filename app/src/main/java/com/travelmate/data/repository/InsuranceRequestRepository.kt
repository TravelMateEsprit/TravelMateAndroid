package com.travelmate.data.repository

import com.travelmate.data.api.InsuranceApi
import com.travelmate.data.models.*
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsuranceRequestRepository @Inject constructor(
    private val api: InsuranceApi
) {
    
    // ========== Utilisateur ==========
    
    suspend fun createRequest(
        token: String,
        request: CreateInsuranceRequestRequest
    ): Response<InsuranceRequest> {
        return api.createInsuranceRequest("Bearer $token", request)
    }
    
    suspend fun getMyRequests(token: String): Response<List<InsuranceRequest>> {
        return api.getMyRequests("Bearer $token")
    }
    
    suspend fun updateRequest(
        token: String,
        requestId: String,
        request: CreateInsuranceRequestRequest
    ): Response<InsuranceRequest> {
        return api.updateRequest(requestId, "Bearer $token", request)
    }
    
    suspend fun cancelRequest(
        token: String,
        requestId: String
    ): Response<InsuranceRequest> {
        return api.cancelRequest(requestId, "Bearer $token")
    }
    
    // ========== Agence ==========
    
    suspend fun getAgencyRequests(
        token: String,
        status: RequestStatus? = null
    ): Response<List<InsuranceRequest>> {
        val statusString = when (status) {
            RequestStatus.PENDING -> "EN_ATTENTE"
            RequestStatus.APPROVED -> "APPROUVEE"
            RequestStatus.REJECTED -> "REJETEE"
            RequestStatus.CANCELLED -> "ANNULEE"
            null -> null
        }
        return api.getAgencyRequests("Bearer $token", statusString)
    }
    
    suspend fun getAgencyStats(token: String): Response<InsuranceRequestStats> {
        return api.getAgencyRequestStats("Bearer $token")
    }
    
    suspend fun markAsRead(
        token: String,
        requestId: String
    ): Response<InsuranceRequest> {
        return api.markRequestAsRead(requestId, "Bearer $token")
    }
    
    suspend fun reviewRequest(
        token: String,
        requestId: String,
        status: RequestStatus,
        response: String
    ): Response<InsuranceRequest> {
        val reviewRequest = ReviewInsuranceRequestRequest(
            status = status,
            agencyResponse = response
        )
        return api.reviewRequest(requestId, "Bearer $token", reviewRequest)
    }
    
    // ========== Commun ==========
    
    suspend fun getRequestById(
        token: String,
        requestId: String
    ): Response<InsuranceRequest> {
        return api.getRequestById(requestId, "Bearer $token")
    }
}
