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
        return api.createInsuranceRequest(request)
    }
    
    suspend fun getMyRequests(token: String): Response<List<InsuranceRequest>> {
        return api.getMyRequests()
    }
    
    suspend fun updateRequest(
        token: String,
        requestId: String,
        request: CreateInsuranceRequestRequest
    ): Response<InsuranceRequest> {
        return api.updateRequest(requestId, request)
    }
    
    suspend fun cancelRequest(
        token: String,
        requestId: String
    ): Response<InsuranceRequest> {
        return api.cancelRequest(requestId)
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
        return api.getAgencyRequests(statusString)
    }
    
    suspend fun getAgencyStats(token: String): Response<InsuranceRequestStats> {
        return api.getAgencyRequestStats()
    }
    
    suspend fun markAsRead(
        token: String,
        requestId: String
    ): Response<InsuranceRequest> {
        return api.markRequestAsRead(requestId)
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
        return api.reviewRequest(requestId, reviewRequest)
    }
    
    // ========== Commun ==========
    
    suspend fun getRequestById(
        token: String,
        requestId: String
    ): Response<InsuranceRequest> {
        return api.getRequestById(requestId)
    }
}
