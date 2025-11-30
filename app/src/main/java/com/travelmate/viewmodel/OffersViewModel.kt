package com.travelmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.FlightOffer
import com.travelmate.data.models.Preferences
import com.travelmate.data.models.RecommendedOffer
import com.travelmate.data.service.OffersService
import com.travelmate.data.service.PriceAlertService
import com.travelmate.data.models.PriceAlert
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OffersViewModel @Inject constructor(
    private val offersService: OffersService,
    private val priceAlertService: PriceAlertService
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
    
    // Amadeus search parameters
    private val _origin = MutableStateFlow<String?>(null)
    val origin: StateFlow<String?> = _origin.asStateFlow()
    
    private val _destination = MutableStateFlow<String?>(null)
    val destination: StateFlow<String?> = _destination.asStateFlow()
    
    private val _departureDate = MutableStateFlow<String?>(null)
    val departureDate: StateFlow<String?> = _departureDate.asStateFlow()
    
    private val _returnDate = MutableStateFlow<String?>(null)
    val returnDate: StateFlow<String?> = _returnDate.asStateFlow()
    
    private val _adults = MutableStateFlow<Int?>(null)
    val adults: StateFlow<Int?> = _adults.asStateFlow()
    
    // Selected offer for details screen
    private val _selectedOffer = MutableStateFlow<FlightOffer?>(null)
    val selectedOffer: StateFlow<FlightOffer?> = _selectedOffer.asStateFlow()
    
    // Price alerts
    val priceAlerts: StateFlow<List<PriceAlert>> = priceAlertService.alerts
    
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
                sort = _sortBy.value,
                origin = _origin.value,
                destination = _destination.value,
                departureDate = _departureDate.value,
                returnDate = _returnDate.value,
                adults = _adults.value
            )
        }
    }
    
    /**
     * Search flights using Amadeus real-time API
     * This is the dedicated function for Amadeus search with validation
     * IMPORTANT: Only pass Amadeus parameters, NOT JSON parameters (q, from, to, date_depart)
     */
    fun searchFlights(
        origin: String,
        destination: String,
        departureDate: String,
        returnDate: String? = null,
        adults: Int = 1,
        direct: Boolean? = null,
        sort: String? = null
    ) {
        viewModelScope.launch {
            // Update state
            _origin.value = origin
            _destination.value = destination
            _departureDate.value = departureDate
            _returnDate.value = returnDate
            _adults.value = adults
            _directOnly.value = direct
            _sortBy.value = sort
            
            // Perform search - ONLY pass Amadeus parameters, NOT JSON fallback parameters
            offersService.getOffers(
                q = null,              // DO NOT pass - would trigger JSON fallback
                type = null,          // DO NOT pass - would trigger JSON fallback
                from = null,          // DO NOT pass - would trigger JSON fallback
                to = null,            // DO NOT pass - would trigger JSON fallback
                date_depart = null,   // DO NOT pass - would trigger JSON fallback
                date_return = null,   // DO NOT pass - would trigger JSON fallback
                direct = direct,
                sort = sort,
                origin = origin,      // Amadeus parameter
                destination = destination,  // Amadeus parameter
                departureDate = departureDate,  // Amadeus parameter
                returnDate = returnDate,  // Amadeus parameter
                adults = adults       // Amadeus parameter
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
    
    // Setters for Amadeus parameters
    fun setOrigin(origin: String?) {
        _origin.value = origin
    }
    
    fun setDestination(destination: String?) {
        _destination.value = destination
    }
    
    fun setDepartureDate(date: String?) {
        _departureDate.value = date
    }
    
    fun setReturnDate(date: String?) {
        _returnDate.value = date
    }
    
    fun setAdults(adults: Int?) {
        _adults.value = adults
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
        _origin.value = null
        _destination.value = null
        _departureDate.value = null
        _returnDate.value = null
        _adults.value = null
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
    
    /**
     * Set selected offer for details screen
     */
    fun setSelectedOffer(offer: FlightOffer) {
        _selectedOffer.value = offer
    }
    
    /**
     * Clear selected offer
     */
    fun clearSelectedOffer() {
        _selectedOffer.value = null
    }
    
    // Price alerts actions
    fun deleteAlert(alertId: String) {
        viewModelScope.launch { priceAlertService.deleteAlert(alertId) }
    }
    
    fun createAlertFromOffer(offer: FlightOffer, priceThreshold: Double) {
        viewModelScope.launch { priceAlertService.createAlertFromOffer(offer, priceThreshold) }
    }
    
    fun checkAlerts(offers: List<FlightOffer>) {
        viewModelScope.launch { priceAlertService.checkAlerts(offers) }
    }
}

