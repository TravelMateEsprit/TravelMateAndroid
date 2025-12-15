package com.travelmate.data.api

import com.travelmate.data.models.CreatePackRequest
import com.travelmate.data.models.Pack
import com.travelmate.data.models.UpdatePackRequest
import retrofit2.http.*

interface PacksApi {

    // Get all active offers (for normal users)
    @GET("offers")
    suspend fun getAllActiveOffers(): List<Pack>
    
    // Get all offers (active and inactive) for agency - requires authentication
    @GET("offers/all")
    suspend fun getAllOffers(
        @Header("Authorization") token: String
    ): List<Pack>

    // Get offer by ID
    @GET("offers/{id}")
    suspend fun getOfferById(@Path("id") id: String): Pack

    // Create offer (AGENCY only) - requires JWT token
    @POST("offers")
    suspend fun createOffer(
        @Header("Authorization") token: String,
        @Body request: CreatePackRequest,
        @Query("agencyId") agencyId: String
    ): Pack

    // Update offer (AGENCY only) - requires JWT token
    @PUT("offers/{id}")
    suspend fun updateOffer(
        @Path("id") id: String,
        @Header("Authorization") token: String,
        @Body request: UpdatePackRequest
    ): Pack

    // Delete offer (AGENCY only) - requires JWT token
    @DELETE("offers/{id}")
    suspend fun deleteOffer(
        @Path("id") id: String,
        @Header("Authorization") token: String
    )
}