package com.travelmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.*
import com.travelmate.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository
) : ViewModel() {
    
    // State for insurance reviews
    private val _insuranceReviews = MutableStateFlow<List<Review>>(emptyList())
    val insuranceReviews: StateFlow<List<Review>> = _insuranceReviews.asStateFlow()
    
    private val _ratingStats = MutableStateFlow<RatingStats?>(null)
    val ratingStats: StateFlow<RatingStats?> = _ratingStats.asStateFlow()
    
    // State for user's reviews
    private val _myReviews = MutableStateFlow<List<Review>>(emptyList())
    val myReviews: StateFlow<List<Review>> = _myReviews.asStateFlow()
    
    // State for current user's review for specific insurance
    private val _userReview = MutableStateFlow<Review?>(null)
    val userReview: StateFlow<Review?> = _userReview.asStateFlow()
    
    // Check if user can review
    private val _canReview = MutableStateFlow(false)
    val canReview: StateFlow<Boolean> = _canReview.asStateFlow()
    
    // Loading states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Success state for showing feedback
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    /**
     * Load reviews for a specific insurance
     */
    fun loadInsuranceReviews(insuranceId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                reviewRepository.getInsuranceReviews(insuranceId, forceRefresh)
                    .onSuccess { response ->
                        _insuranceReviews.value = response.reviews
                        _ratingStats.value = response.stats
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Échec du chargement des avis"
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Une erreur inattendue s'est produite"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load only rating statistics for an insurance (lightweight)
     */
    fun loadRatingStats(insuranceId: String) {
        viewModelScope.launch {
            try {
                reviewRepository.getInsuranceStats(insuranceId)
                    .onSuccess { stats ->
                        _ratingStats.value = stats
                    }
                    .onFailure { e ->
                        // Silently fail for stats - not critical
                    }
            } catch (e: Exception) {
                // Silently fail
            }
        }
    }
    
    /**
     * Check if user can review and load existing review
     */
    fun checkReviewEligibility(insuranceId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Check if can review
                reviewRepository.canUserReview(insuranceId)
                    .onSuccess { response ->
                        _canReview.value = response.canReview
                        
                        // If user already reviewed, load their review
                        if (!response.canReview) {
                            loadUserReview(insuranceId)
                        } else {
                            _userReview.value = null
                        }
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Impossible de vérifier l'éligibilité"
                        _canReview.value = false
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Une erreur inattendue s'est produite"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load user's existing review for an insurance
     */
    private fun loadUserReview(insuranceId: String) {
        viewModelScope.launch {
            try {
                reviewRepository.getUserReviewForInsurance(insuranceId)
                    .onSuccess { review ->
                        _userReview.value = review
                    }
                    .onFailure { e ->
                        // Silently fail - user might not have a review
                    }
            } catch (e: Exception) {
                // Silently fail
            }
        }
    }
    
    /**
     * Submit a new review
     */
    fun submitReview(insuranceId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null
            _successMessage.value = null
            
            try {
                reviewRepository.createReview(insuranceId, rating, comment)
                    .onSuccess { review ->
                        _userReview.value = review
                        _canReview.value = false
                        _successMessage.value = "Avis soumis avec succès !"
                        
                        // Refresh insurance reviews
                        loadInsuranceReviews(insuranceId, forceRefresh = true)
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Échec de la soumission de l'avis"
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Une erreur inattendue s'est produite"
            } finally {
                _isSubmitting.value = false
            }
        }
    }
    
    /**
     * Update an existing review
     */
    fun updateReview(
        reviewId: String,
        insuranceId: String,
        rating: Int,
        comment: String
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null
            _successMessage.value = null
            
            try {
                reviewRepository.updateReview(reviewId, insuranceId, rating, comment)
                    .onSuccess { review ->
                        _userReview.value = review
                        _successMessage.value = "Avis modifié avec succès !"
                        
                        // Refresh insurance reviews
                        loadInsuranceReviews(insuranceId, forceRefresh = true)
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Échec de la modification de l'avis"
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Une erreur inattendue s'est produite"
            } finally {
                _isSubmitting.value = false
            }
        }
    }
    
    /**
     * Delete a review
     */
    fun deleteReview(reviewId: String, insuranceId: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null
            _successMessage.value = null
            
            try {
                reviewRepository.deleteReview(reviewId, insuranceId)
                    .onSuccess {
                        _userReview.value = null
                        _canReview.value = true
                        _successMessage.value = "Avis supprimé avec succès !"
                        
                        // Refresh insurance reviews
                        loadInsuranceReviews(insuranceId, forceRefresh = true)
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Échec de la suppression de l'avis"
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Une erreur inattendue s'est produite"
            } finally {
                _isSubmitting.value = false
            }
        }
    }
    
    /**
     * Load all reviews for current user
     */
    fun loadMyReviews() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                reviewRepository.getMyReviews()
                    .onSuccess { reviews ->
                        _myReviews.value = reviews
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Échec du chargement de vos avis"
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Une erreur inattendue s'est produite"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Clear success message
     */
    fun clearSuccess() {
        _successMessage.value = null
    }
    
    /**
     * Reset state for new insurance
     */
    fun resetState() {
        _insuranceReviews.value = emptyList()
        _ratingStats.value = null
        _userReview.value = null
        _canReview.value = false
        _error.value = null
        _successMessage.value = null
    }
}
