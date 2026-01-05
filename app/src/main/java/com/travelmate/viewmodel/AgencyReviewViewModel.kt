package com.travelmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.Review
import com.travelmate.data.models.RatingStats
import com.travelmate.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for agency reviews dashboard
 */
@HiltViewModel
class AgencyReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository
) : ViewModel() {
    
    // State for agency reviews
    private val _agencyReviews = MutableStateFlow<List<Review>>(emptyList())
    val agencyReviews: StateFlow<List<Review>> = _agencyReviews.asStateFlow()
    
    // State for agency statistics
    private val _agencyStats = MutableStateFlow<RatingStats?>(null)
    val agencyStats: StateFlow<RatingStats?> = _agencyStats.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Load all reviews for the agency's insurances
     */
    fun loadAgencyReviews() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                reviewRepository.getAgencyReviews()
                    .onSuccess { response ->
                        _agencyReviews.value = response.reviews
                        _agencyStats.value = response.stats
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
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
}
