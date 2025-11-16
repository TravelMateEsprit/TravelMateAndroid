package com.travelmate.data.api

import com.travelmate.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface InsuranceApi {
    
    // ========== Endpoints Utilisateurs ==========
    
    @GET("insurances")
    suspend fun getAllInsurances(
        @Header("Authorization") token: String
    ): Response<List<Insurance>>
    
    @GET("insurances/search")
    suspend fun searchInsurances(
        @Header("Authorization") token: String,
        @Query("searchTerm") searchTerm: String? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("duration") duration: String? = null,
        @Query("coverage") coverage: String? = null,
        @Query("agencyName") agencyName: String? = null,
        @Query("city") city: String? = null,
        @Query("country") country: String? = null,
        @Query("isActive") isActive: Boolean? = true,
        @Query("sortBy") sortBy: String? = "createdAt",
        @Query("sortOrder") sortOrder: String? = "desc",
        @Query("limit") limit: Int? = 20,
        @Query("page") page: Int? = 0
    ): Response<SearchInsuranceResponse>
    
    @GET("insurances/my-subscriptions")
    suspend fun getMySubscriptions(
        @Header("Authorization") token: String
    ): Response<List<Insurance>>
    
    @POST("insurances/{id}/subscribe")
    suspend fun subscribeToInsurance(
        @Path("id") insuranceId: String,
        @Header("Authorization") token: String
    ): Response<Insurance>
    
    @POST("insurances/{id}/unsubscribe")
    suspend fun unsubscribeFromInsurance(
        @Path("id") insuranceId: String,
        @Header("Authorization") token: String
    ): Response<Insurance>
    
    // ========== Endpoints Agences ==========
    
    @POST("insurances/agency")
    suspend fun createInsurance(
        @Header("Authorization") token: String,
        @Body request: CreateInsuranceRequest
    ): Response<Insurance>
    
    @GET("insurances/agency/my-insurances")
    suspend fun getMyInsurances(
        @Header("Authorization") token: String
    ): Response<List<Insurance>>
    
    @PATCH("insurances/agency/{id}")
    suspend fun updateInsurance(
        @Path("id") insuranceId: String,
        @Header("Authorization") token: String,
        @Body request: UpdateInsuranceRequest
    ): Response<Insurance>
    
    @DELETE("insurances/agency/{id}")
    suspend fun deleteInsurance(
        @Path("id") insuranceId: String,
        @Header("Authorization") token: String
    ): Response<Unit>
    
    @GET("insurances/agency/{id}/subscribers")
    suspend fun getInsuranceSubscribers(
        @Path("id") insuranceId: String,
        @Header("Authorization") token: String
    ): Response<InsuranceSubscribersResponse>
}
