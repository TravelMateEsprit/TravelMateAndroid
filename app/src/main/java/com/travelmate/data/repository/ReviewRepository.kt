package com.travelmate.data.repository

import com.travelmate.data.api.ReviewApi
import com.travelmate.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val reviewApi: ReviewApi
) {
    // Local cache for reviews
    private val _cachedReviews = MutableStateFlow<Map<String, InsuranceReviewsResponse>>(emptyMap())
    val cachedReviews: Flow<Map<String, InsuranceReviewsResponse>> = _cachedReviews.asStateFlow()
    
    private val _myReviews = MutableStateFlow<List<Review>>(emptyList())
    val myReviews: Flow<List<Review>> = _myReviews.asStateFlow()
    
    /**
     * Create a new review for an insurance
     */
    suspend fun createReview(
        insuranceId: String,
        rating: Int,
        comment: String
    ): Result<Review> {
        return try {
            val request = CreateReviewRequest(
                insuranceId = insuranceId,
                rating = rating,
                comment = comment
            )
            val response = reviewApi.createReview(request)
            
            if (response.isSuccessful && response.body() != null) {
                val review = response.body()!!
                // Invalidate cache for this insurance
                _cachedReviews.value = _cachedReviews.value - insuranceId
                Result.success(review)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Failed to create review: ${response.code()} - ${response.message()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update an existing review
     */
    suspend fun updateReview(
        reviewId: String,
        insuranceId: String,
        rating: Int? = null,
        comment: String? = null
    ): Result<Review> {
        return try {
            val request = UpdateReviewRequest(
                rating = rating,
                comment = comment
            )
            val response = reviewApi.updateReview(reviewId, request)
            
            if (response.isSuccessful && response.body() != null) {
                val review = response.body()!!
                // Invalidate cache for this insurance
                _cachedReviews.value = _cachedReviews.value - insuranceId
                Result.success(review)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to update review"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a review
     */
    suspend fun deleteReview(reviewId: String, insuranceId: String): Result<Unit> {
        return try {
            val response = reviewApi.deleteReview(reviewId)
            
            if (response.isSuccessful) {
                // Invalidate cache for this insurance
                _cachedReviews.value = _cachedReviews.value - insuranceId
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to delete review"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all reviews for a specific insurance with statistics
     */
    suspend fun getInsuranceReviews(insuranceId: String, forceRefresh: Boolean = false): Result<InsuranceReviewsResponse> {
        return try {
            // Check cache first unless force refresh
            if (!forceRefresh && _cachedReviews.value.containsKey(insuranceId)) {
                return Result.success(_cachedReviews.value[insuranceId]!!)
            }
            
            val response = reviewApi.getInsuranceReviews(insuranceId)
            
            if (response.isSuccessful && response.body() != null) {
                val reviewsResponse = response.body()!!
                // Update cache
                _cachedReviews.value = _cachedReviews.value + (insuranceId to reviewsResponse)
                Result.success(reviewsResponse)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch reviews"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get only the rating statistics for an insurance
     */
    suspend fun getInsuranceStats(insuranceId: String): Result<RatingStats> {
        return try {
            val response = reviewApi.getInsuranceStats(insuranceId)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch stats"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current user's reviews
     */
    suspend fun getMyReviews(): Result<List<Review>> {
        return try {
            val response = reviewApi.getMyReviews()
            
            if (response.isSuccessful && response.body() != null) {
                val reviews = response.body()!!
                _myReviews.value = reviews
                Result.success(reviews)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch my reviews"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all reviews for agency (agency only)
     */
    suspend fun getAgencyReviews(): Result<AgencyReviewsResponse> {
        return try {
            val response = reviewApi.getAgencyReviews()
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch agency reviews"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if the current user can review a specific insurance
     */
    suspend fun canUserReview(insuranceId: String): Result<CanReviewResponse> {
        return try {
            val response = reviewApi.canUserReview(insuranceId)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to check review eligibility"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user's existing review for a specific insurance
     */
    suspend fun getUserReviewForInsurance(insuranceId: String): Result<Review?> {
        return try {
            val response = reviewApi.getUserReviewForInsurance(insuranceId)
            
            if (response.isSuccessful) {
                Result.success(response.body())
            } else if (response.code() == 404) {
                // No review found - this is OK
                Result.success(null)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch user review"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clear cache for a specific insurance
     */
    fun clearCache(insuranceId: String) {
        _cachedReviews.value = _cachedReviews.value - insuranceId
    }
    
    /**
     * Clear all cached reviews
     */
    fun clearAllCache() {
        _cachedReviews.value = emptyMap()
        _myReviews.value = emptyList()
    }
}
