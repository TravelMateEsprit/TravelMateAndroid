package com.travelmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.FlightOffer
import com.travelmate.data.models.Preferences
import com.travelmate.data.models.RecommendedOffer
import com.travelmate.data.service.OffersService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OffersViewModel @Inject constructor(
    private val offersService: OffersService
) : ViewModel() {
    
    val offers: StateFlow<List<FlightOffer>> = offersService.offers
    val isLoading: StateFlow<Boolean> = offersService.isLoading
    val error: StateFlow<String?> = offersService.error
    
    val recommendations: StateFlow<List<RecommendedOffer>> = offersService.recommendations
    val isLoadingRecommendations: StateFlow<Boolean> = offersService.isLoadingRecommendations
    
    // Search and filter state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType.asStateFlow()
    
    private val _fromAirport = MutableStateFlow<String?>(null)
    val fromAirport: StateFlow<String?> = _fromAirport.asStateFlow()
    
    private val _toAirport = MutableStateFlow<String?>(null)
    val toAirport: StateFlow<String?> = _toAirport.asStateFlow()
    
    private val _directOnly = MutableStateFlow<Boolean?>(null)
    val directOnly: StateFlow<Boolean?> = _directOnly.asStateFlow()
    
    private val _dateDepart = MutableStateFlow<String?>(null)
    val dateDepart: StateFlow<String?> = _dateDepart.asStateFlow()
    
    private val _dateReturn = MutableStateFlow<String?>(null)
    val dateReturn: StateFlow<String?> = _dateReturn.asStateFlow()
    
    private val _sortBy = MutableStateFlow<String?>(null)
    val sortBy: StateFlow<String?> = _sortBy.asStateFlow()
    
    init {
        // Load all offers on init
        loadAllOffers()
    }
    
    /**
     * Load all offers without filters
     */
    fun loadAllOffers() {
        viewModelScope.launch {
            offersService.getAllOffers()
        }
    }
    
    /**
     * Search offers with current filters
     */
    fun searchOffers() {
        viewModelScope.launch {
            offersService.getOffers(
                q = _searchQuery.value.takeIf { it.isNotBlank() },
                type = _selectedType.value,
                from = _fromAirport.value,
                to = _toAirport.value,
                direct = _directOnly.value,
                date_depart = _dateDepart.value,
                date_return = _dateReturn.value,
                sort = _sortBy.value
            )
        }
    }
    
    /**
     * Get round-trips from Tunis to Paris on specific dates
     */
    fun getRoundTripsFromTunisToParis(dateDepart: String, dateReturn: String) {
        viewModelScope.launch {
            offersService.getRoundTripsFromTunisToParis(dateDepart, dateReturn)
        }
    }
    
    /**
     * Search with keyword, filter, and sort
     */
    fun searchWithFilters(
        query: String,
        type: String? = null,
        directOnly: Boolean? = null,
        sortBy: String? = null
    ) {
        viewModelScope.launch {
            offersService.searchOffers(query, type, directOnly, sortBy)
        }
    }
    
    // Setters for filters
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun setType(type: String?) {
        _selectedType.value = type
    }
    
    fun setFromAirport(airport: String?) {
        _fromAirport.value = airport
    }
    
    fun setToAirport(airport: String?) {
        _toAirport.value = airport
    }
    
    fun setDirectOnly(direct: Boolean?) {
        _directOnly.value = direct
    }
    
    fun setDateDepart(date: String?) {
        _dateDepart.value = date
    }
    
    fun setDateReturn(date: String?) {
        _dateReturn.value = date
    }
    
    fun setSortBy(sort: String?) {
        _sortBy.value = sort
    }
    
    fun clearFilters() {
        _searchQuery.value = ""
        _selectedType.value = null
        _fromAirport.value = null
        _toAirport.value = null
        _directOnly.value = null
        _dateDepart.value = null
        _dateReturn.value = null
        _sortBy.value = null
        loadAllOffers()
    }
    
    fun clearError() {
        offersService.clearError()
    }
    
    /**
     * Get AI-powered recommendations based on user preferences
     */
    fun getRecommendations(preferences: Preferences) {
        viewModelScope.launch {
            offersService.getRecommendations(preferences)
        }
    }
    
    fun clearRecommendations() {
        offersService.clearRecommendations()
    }
}

