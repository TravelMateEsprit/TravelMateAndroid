package com.travelmate.data.api

import com.travelmate.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface InsuranceApi {
    
    // ========== Endpoints IA ==========
    
    @GET("insurances/recommendations")
    suspend fun getRecommendations(): Response<InsuranceRecommendationsResponse>
    
    @POST("insurances/compare")
    suspend fun compareInsurances(
        @Body request: CompareInsurancesRequest
    ): Response<ComparisonResult>
    
    // ========== Endpoints Utilisateurs ==========
    
    @GET("insurances")
    suspend fun getAllInsurances(): Response<List<Insurance>>
    
    @GET("insurances/search")
    suspend fun searchInsurances(
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
    suspend fun getMySubscriptions(): Response<List<Insurance>>
    
    @POST("insurances/{id}/subscribe")
    suspend fun subscribeToInsurance(
        @Path("id") insuranceId: String
    ): Response<Insurance>
    
    @POST("insurances/{id}/unsubscribe")
    suspend fun unsubscribeFromInsurance(
        @Path("id") insuranceId: String
    ): Response<Insurance>
    
    // ========== Endpoints Agences ==========
    
    @POST("insurances/agency")
    suspend fun createInsurance(
        @Body request: CreateInsuranceRequest
    ): Response<Insurance>
    
    @GET("insurances/agency/my-insurances")
    suspend fun getMyInsurances(): Response<List<Insurance>>
    
    @PATCH("insurances/agency/{id}")
    suspend fun updateInsurance(
        @Path("id") insuranceId: String,
        @Body request: UpdateInsuranceRequest
    ): Response<Insurance>
    
    @DELETE("insurances/agency/{id}")
    suspend fun deleteInsurance(
        @Path("id") insuranceId: String
    ): Response<Unit>
    
    @GET("insurances/agency/{id}/subscribers")
    suspend fun getInsuranceSubscribers(
        @Path("id") insuranceId: String
    ): Response<InsuranceSubscribersResponse>
    
    // ========== Endpoints Demandes d'Assurance ==========
    
    // Créer une demande (Utilisateur)
    @POST("insurance-requests")
    suspend fun createInsuranceRequest(
        @Body request: CreateInsuranceRequestRequest
    ): Response<InsuranceRequest>
    
    // Obtenir mes demandes (Utilisateur)
    @GET("insurance-requests/my-requests")
    suspend fun getMyRequests(): Response<List<InsuranceRequest>>
    
    // Obtenir les demandes de l'agence (Agence)
    @GET("insurance-requests/agency-requests")
    suspend fun getAgencyRequests(
        @Query("status") status: String? = null
    ): Response<List<InsuranceRequest>>
    
    // Obtenir les statistiques (Agence)
    @GET("insurance-requests/agency-stats")
    suspend fun getAgencyRequestStats(): Response<InsuranceRequestStats>
    
    // Obtenir une demande spécifique
    @GET("insurance-requests/{id}")
    suspend fun getRequestById(
        @Path("id") requestId: String
    ): Response<InsuranceRequest>
    
    // Marquer comme lue (Agence)
    @PUT("insurance-requests/{id}/mark-read")
    suspend fun markRequestAsRead(
        @Path("id") requestId: String
    ): Response<InsuranceRequest>
    
    // Modifier une demande (Utilisateur)
    @PUT("insurance-requests/{id}")
    suspend fun updateRequest(
        @Path("id") requestId: String,
        @Body request: CreateInsuranceRequestRequest
    ): Response<InsuranceRequest>
    
    // Annuler une demande (Utilisateur)
    @DELETE("insurance-requests/{id}")
    suspend fun cancelRequest(
        @Path("id") requestId: String
    ): Response<InsuranceRequest>
    
    // Approuver/Rejeter une demande (Agence)
    @PUT("insurance-requests/{id}/review")
    suspend fun reviewRequest(
        @Path("id") requestId: String,
        @Body request: ReviewInsuranceRequestRequest
    ): Response<InsuranceRequest>
    
    // ========== Endpoint IA : Comparaison d'assurances ==========
    
    // ========== Endpoints Paiement Stripe ==========
    
    // Créer un paiement (Utilisateur)
    @POST("insurance-requests/{id}/create-payment")
    suspend fun createPayment(
        @Path("id") requestId: String
    ): Response<CreatePaymentResponse>
    
    // Confirmer un paiement (Utilisateur)
    @POST("insurance-requests/{id}/confirm-payment")
    suspend fun confirmPayment(
        @Path("id") requestId: String,
        @Body request: ConfirmPaymentRequest
    ): Response<ConfirmPaymentResponse>
}
