package com.travelmate.data.api

import com.travelmate.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ClaimApi {
    
    @POST("claims")
    suspend fun createClaim(
        @Body request: CreateClaimRequest
    ): Response<Claim>
    
    @GET("claims/my-claims")
    suspend fun getMyClaims(): Response<List<Claim>>
    
    @GET("claims/agency-claims")
    suspend fun getAgencyClaims(): Response<List<Claim>>
    
    @GET("claims/unread-count")
    suspend fun getUnreadCount(): Response<ClaimUnreadCountResponse>
    
    @GET("claims/{id}")
    suspend fun getClaimById(
        @Path("id") claimId: String
    ): Response<Claim>
    
    @POST("claims/{id}/messages")
    suspend fun addMessage(
        @Path("id") claimId: String,
        @Body request: AddMessageRequest
    ): Response<Claim>
    
    @PATCH("claims/{id}/status")
    suspend fun updateClaimStatus(
        @Path("id") claimId: String,
        @Body request: UpdateClaimStatusRequest
    ): Response<Claim>
}
