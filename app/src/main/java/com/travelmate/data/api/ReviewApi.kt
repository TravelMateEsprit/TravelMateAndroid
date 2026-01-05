package com.travelmate.data.api

import com.travelmate.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ReviewApi {
    @POST("reviews")
    suspend fun createReview(
        @Body request: CreateReviewRequest
    ): Response<Review>

    @PUT("reviews/{id}")
    suspend fun updateReview(
        @Path("id") reviewId: String,
        @Body request: UpdateReviewRequest
    ): Response<Review>

    @DELETE("reviews/{id}")
    suspend fun deleteReview(
        @Path("id") reviewId: String
    ): Response<Unit>

    @GET("reviews/my-reviews")
    suspend fun getMyReviews(): Response<List<Review>>

    @GET("reviews/insurance/{insuranceId}")
    suspend fun getInsuranceReviews(
        @Path("insuranceId") insuranceId: String
    ): Response<InsuranceReviewsResponse>

    @GET("reviews/insurance/{insuranceId}/stats")
    suspend fun getInsuranceStats(
        @Path("insuranceId") insuranceId: String
    ): Response<RatingStats>

    @GET("reviews/agency/my-reviews")
    suspend fun getAgencyReviews(): Response<AgencyReviewsResponse>

    @GET("reviews/can-review/{insuranceId}")
    suspend fun canUserReview(
        @Path("insuranceId") insuranceId: String
    ): Response<CanReviewResponse>

    @GET("reviews/user-review/{insuranceId}")
    suspend fun getUserReviewForInsurance(
        @Path("insuranceId") insuranceId: String
    ): Response<Review>
}
