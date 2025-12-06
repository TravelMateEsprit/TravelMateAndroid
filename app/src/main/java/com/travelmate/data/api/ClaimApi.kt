package com.travelmate.data.api

import com.travelmate.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ClaimApi {
    
    @POST("claims")
    suspend fun createClaim(
        @Header("Authorization") token: String,
        @Body request: CreateClaimRequest
    ): Response<Claim>
    
    @GET("claims/my-claims")
    suspend fun getMyClaims(
        @Header("Authorization") token: String
    ): Response<List<Claim>>
    
    @GET("claims/agency-claims")
    suspend fun getAgencyClaims(
        @Header("Authorization") token: String
    ): Response<List<Claim>>
    
    @GET("claims/unread-count")
    suspend fun getUnreadCount(
        @Header("Authorization") token: String
    ): Response<ClaimUnreadCountResponse>
    
    @GET("claims/{id}")
    suspend fun getClaimById(
        @Path("id") claimId: String,
        @Header("Authorization") token: String
    ): Response<Claim>
    
    @POST("claims/{id}/messages")
    suspend fun addMessage(
        @Path("id") claimId: String,
        @Header("Authorization") token: String,
        @Body request: AddMessageRequest
    ): Response<Claim>
    
    @PATCH("claims/{id}/status")
    suspend fun updateClaimStatus(
        @Path("id") claimId: String,
        @Header("Authorization") token: String,
        @Body request: UpdateClaimStatusRequest
    ): Response<Claim>
}
