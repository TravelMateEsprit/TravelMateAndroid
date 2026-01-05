package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val _id: String,
    val userId: String,
    val insuranceId: String,
    val agencyId: String,
    val rating: Int,
    val comment: String,
    val createdAt: String,
    val updatedAt: String,
    val userName: String? = null,
    val userEmail: String? = null,
    val insuranceName: String? = null
)

@Serializable
data class CreateReviewRequest(
    val insuranceId: String,
    val rating: Int,
    val comment: String
)

@Serializable
data class UpdateReviewRequest(
    val rating: Int? = null,
    val comment: String? = null
)

@Serializable
data class RatingStats(
    val averageRating: Double,
    val totalReviews: Int,
    val ratingDistribution: Map<String, Int>
)

@Serializable
data class InsuranceReviewsResponse(
    val reviews: List<Review>,
    val stats: RatingStats
)

@Serializable
data class AgencyReviewsResponse(
    val reviews: List<Review>,
    val stats: RatingStats
)

@Serializable
data class CanReviewResponse(
    val canReview: Boolean
)
