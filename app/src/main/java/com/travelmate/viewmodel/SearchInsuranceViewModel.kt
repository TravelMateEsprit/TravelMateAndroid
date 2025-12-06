package com.travelmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.Insurance
import com.travelmate.data.models.SearchInsuranceRequest
import com.travelmate.data.repository.InsuranceRepository
import com.travelmate.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchInsuranceViewModel @Inject constructor(
    private val insuranceRepository: InsuranceRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _searchResults = MutableStateFlow<List<Insurance>>(emptyList())
    val searchResults: StateFlow<List<Insurance>> = _searchResults.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _totalResults = MutableStateFlow(0)
    val totalResults: StateFlow<Int> = _totalResults.asStateFlow()
    
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    
    // Filtres actifs
    private val _searchTerm = MutableStateFlow("")
    val searchTerm: StateFlow<String> = _searchTerm.asStateFlow()
    
    private val _minPrice = MutableStateFlow<Double?>(null)
    val minPrice: StateFlow<Double?> = _minPrice.asStateFlow()
    
    private val _maxPrice = MutableStateFlow<Double?>(null)
    val maxPrice: StateFlow<Double?> = _maxPrice.asStateFlow()
    
    private val _selectedDuration = MutableStateFlow<String?>(null)
    val selectedDuration: StateFlow<String?> = _selectedDuration.asStateFlow()
    
    private val _selectedCoverage = MutableStateFlow<String?>(null)
    val selectedCoverage: StateFlow<String?> = _selectedCoverage.asStateFlow()
    
    private val _selectedCity = MutableStateFlow<String?>(null)
    val selectedCity: StateFlow<String?> = _selectedCity.asStateFlow()
    
    private val _sortBy = MutableStateFlow("createdAt")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()
    
    private val _sortOrder = MutableStateFlow("desc")
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()
    
    fun updateSearchTerm(term: String) {
        _searchTerm.value = term
    }
    
    fun updatePriceRange(min: Double?, max: Double?) {
        _minPrice.value = min
        _maxPrice.value = max
    }
    
    fun updateDuration(duration: String?) {
        _selectedDuration.value = duration
    }
    
    fun updateCoverage(coverage: String?) {
        _selectedCoverage.value = coverage
    }
    
    fun updateCity(city: String?) {
        _selectedCity.value = city
    }
    
    fun updateSorting(sortBy: String, sortOrder: String) {
        _sortBy.value = sortBy
        _sortOrder.value = sortOrder
    }
    
    fun searchInsurances(page: Int = 0) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val searchRequest = SearchInsuranceRequest(
                    searchTerm = _searchTerm.value.takeIf { it.isNotEmpty() },
                    minPrice = _minPrice.value,
                    maxPrice = _maxPrice.value,
                    duration = _selectedDuration.value,
                    coverage = _selectedCoverage.value,
                    city = _selectedCity.value,
                    sortBy = _sortBy.value,
                    sortOrder = _sortOrder.value,
                    page = page,
                    limit = 20
                )
                
                val token = userPreferences.getAccessToken() ?: return@launch
                insuranceRepository.searchInsurances(token, searchRequest)
                    .onSuccess { response ->
                        if (page == 0) {
                            _searchResults.value = response.insurances
                        } else {
                            _searchResults.value = _searchResults.value + response.insurances
                        }
                        _totalResults.value = response.total
                        _currentPage.value = page
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to search insurances"
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadMore() {
        if (!_isLoading.value && _searchResults.value.size < _totalResults.value) {
            searchInsurances(_currentPage.value + 1)
        }
    }
    
    fun clearFilters() {
        _searchTerm.value = ""
        _minPrice.value = null
        _maxPrice.value = null
        _selectedDuration.value = null
        _selectedCoverage.value = null
        _selectedCity.value = null
        _sortBy.value = "createdAt"
        _sortOrder.value = "desc"
        searchInsurances(0)
    }
    
    fun clearError() {
        _error.value = null
    }
}
