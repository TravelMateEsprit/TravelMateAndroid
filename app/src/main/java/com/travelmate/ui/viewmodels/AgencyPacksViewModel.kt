package com.travelmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.CreatePackRequest
import com.travelmate.data.models.Pack
import com.travelmate.data.models.UpdatePackRequest
import com.travelmate.data.repository.PacksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AgencyPacksViewModel @Inject constructor(
    private val repo: PacksRepository
) : ViewModel() {

    private val _packs = MutableStateFlow<List<Pack>>(emptyList())
    val packs: StateFlow<List<Pack>> = _packs.asStateFlow()

    private val _filteredPacks = MutableStateFlow<List<Pack>>(emptyList())
    val filteredPacks: StateFlow<List<Pack>> = _filteredPacks.asStateFlow()

    private val _selectedPacks = MutableStateFlow<Set<String>>(emptySet())
    val selectedPacks: StateFlow<Set<String>> = _selectedPacks.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterStatus = MutableStateFlow<FilterStatus?>(null)
    val filterStatus: StateFlow<FilterStatus?> = _filterStatus.asStateFlow()

    private val _filterDestination = MutableStateFlow<String?>(null)
    val filterDestination: StateFlow<String?> = _filterDestination.asStateFlow()

    private val _filterPriceRange = MutableStateFlow<PriceRange?>(null)
    val filterPriceRange: StateFlow<PriceRange?> = _filterPriceRange.asStateFlow()

    private val _filterTypeOffre = MutableStateFlow<String?>(null)
    val filterTypeOffre: StateFlow<String?> = _filterTypeOffre.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        // Apply search/filter whenever query, filters or packs change
        // Combine all flows: packs + search + 4 filters = 6 flows total
        // Since combine supports max 5 flows, we'll combine them in groups
        viewModelScope.launch {
            combine(
                combine(_packs, _searchQuery) { packs: List<Pack>, query: String -> 
                    Pair(packs, query) 
                },
                combine(_filterStatus, _filterDestination) { status: FilterStatus?, destination: String? -> 
                    Pair(status, destination) 
                },
                combine(_filterPriceRange, _filterTypeOffre) { priceRange: PriceRange?, typeOffre: String? -> 
                    Pair(priceRange, typeOffre) 
                }
            ) { packQuery: Pair<List<Pack>, String>, statusDest: Pair<FilterStatus?, String?>, priceType: Pair<PriceRange?, String?> ->
                val packs = packQuery.first
                val query = packQuery.second
                val status = statusDest.first
                val destination = statusDest.second
                val priceRange = priceType.first
                val typeOffre = priceType.second
                
                applyFilters(packs, query, status, destination, priceRange, typeOffre)
            }.collect { filtered: List<Pack> ->
                _filteredPacks.value = filtered
            }
        }
    }
    
    private fun applyFilters(
        packs: List<Pack>,
        query: String,
        status: FilterStatus?,
        destination: String?,
        priceRange: PriceRange?,
        typeOffre: String?
    ): List<Pack> {
        var filtered = packs

        // Apply search query
        if (query.isNotBlank()) {
            val normalizedQuery = query.trim()
            filtered = filtered.filter {
                it.titre.contains(normalizedQuery, ignoreCase = true) ||
                        it.destination.orEmpty().contains(normalizedQuery, ignoreCase = true) ||
                        (it.country?.contains(normalizedQuery, ignoreCase = true) == true) ||
                        (it.region?.contains(normalizedQuery, ignoreCase = true) == true) ||
                        it.description.contains(normalizedQuery, ignoreCase = true) ||
                        it.activities.any { activity ->
                            activity.contains(normalizedQuery, ignoreCase = true)
                        } ||
                        it.placesToVisit.any { place ->
                            place.contains(normalizedQuery, ignoreCase = true)
                        }
            }
        }

        // Apply status filter
        status?.let { s ->
            filtered = when (s) {
                FilterStatus.ACTIVE -> filtered.filter { pack -> pack.actif }
                FilterStatus.INACTIVE -> filtered.filter { pack -> !pack.actif }
                FilterStatus.ALL -> filtered
            }
        }

        // Apply destination filter
        destination?.let { dest ->
            if (dest.isNotBlank()) {
                filtered = filtered.filter { pack ->
                    pack.destination.orEmpty().contains(dest, ignoreCase = true) ||
                            (pack.country?.contains(dest, ignoreCase = true) == true) ||
                            (pack.region?.contains(dest, ignoreCase = true) == true)
                }
            }
        }

        // Apply price range filter
        priceRange?.let { range ->
            filtered = filtered.filter { pack ->
                val price = pack.adultPricePerNight
                price >= range.min && price <= range.max
            }
        }

        // Apply type offre filter
        typeOffre?.let { type ->
            if (type.isNotBlank()) {
                filtered = filtered.filter { pack ->
                    pack.typeOffre.equals(type, ignoreCase = true)
                }
            }
        }

        return filtered
    }

    /**
     * Load agency's packs
     */
    fun loadMyPacks() {
        viewModelScope.launch {
            try {
                android.util.Log.d("AgencyPacksViewModel", "=== loadMyPacks() called ===")
                _isLoading.value = true
                _error.value = null
                
                val loadedPacks = repo.getAgencyPacks()
                android.util.Log.d("AgencyPacksViewModel", "✓ Loaded ${loadedPacks.size} packs from repository")
                
                if (loadedPacks.isEmpty()) {
                    android.util.Log.w("AgencyPacksViewModel", "⚠️ No packs loaded. This could mean:")
                    android.util.Log.w("AgencyPacksViewModel", "  1. No packs exist in database")
                    android.util.Log.w("AgencyPacksViewModel", "  2. Agency ID mismatch")
                    android.util.Log.w("AgencyPacksViewModel", "  3. API returned empty list")
                }
                
                _packs.value = loadedPacks
                
                // Force update filtered packs with current filters
                val filtered = applyFilters(
                    loadedPacks,
                    _searchQuery.value,
                    _filterStatus.value,
                    _filterDestination.value,
                    _filterPriceRange.value,
                    _filterTypeOffre.value
                )
                android.util.Log.d("AgencyPacksViewModel", "✓ After filtering: ${filtered.size} packs")
                android.util.Log.d("AgencyPacksViewModel", "  Search query: '${_searchQuery.value}'")
                android.util.Log.d("AgencyPacksViewModel", "  Active filters: status=${_filterStatus.value}, destination=${_filterDestination.value}")
                
                _filteredPacks.value = filtered
                
                if (filtered.isEmpty() && loadedPacks.isNotEmpty()) {
                    android.util.Log.w("AgencyPacksViewModel", "⚠️ All packs filtered out! Check filter settings.")
                }
            } catch (e: Exception) {
                android.util.Log.e("AgencyPacksViewModel", "❌ Error loading packs: ${e.message}", e)
                e.printStackTrace()
                _error.value = e.message ?: "Erreur lors du chargement des packs. Vérifiez votre connexion et réessayez."
            } finally {
                _isLoading.value = false
                android.util.Log.d("AgencyPacksViewModel", "=== loadMyPacks() completed ===")
            }
        }
    }

    /**
     * Search packs dynamically
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Toggle pack selection (for batch delete)
     */
    fun togglePackSelection(packId: String) {
        val current = _selectedPacks.value.toMutableSet()
        if (current.contains(packId)) {
            current.remove(packId)
        } else {
            current.add(packId)
        }
        _selectedPacks.value = current
    }

    /**
     * Clear all selections
     */
    fun clearSelection() {
        _selectedPacks.value = emptySet()
    }

    /**
     * Delete single pack
     */
    fun deletePack(id: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repo.deleteOffer(id)

                if (result.isSuccess) {
                    _successMessage.value = "Pack supprimé avec succès"
                    loadMyPacks() // Refresh list
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Erreur lors de la suppression"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de la suppression"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete multiple selected packs
     */
    fun deleteSelectedPacks() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val selected = _selectedPacks.value.toList()
                var successCount = 0
                var failCount = 0

                selected.forEach { id ->
                    val result = repo.deleteOffer(id)
                    if (result.isSuccess) successCount++ else failCount++
                }

                _successMessage.value = "$successCount pack(s) supprimé(s)"
                if (failCount > 0) {
                    _error.value = "$failCount pack(s) n'ont pas pu être supprimés"
                }

                clearSelection()
                loadMyPacks() // Refresh list
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de la suppression"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Create a new pack
     */
    fun createPack(request: CreatePackRequest) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val result = repo.createOffer(request)

                if (result.isSuccess) {
                    val newPack = result.getOrNull()
                    _successMessage.value = "Pack créé avec succès"
                    
                    // Immediately add the newly created pack to the list
                    // This ensures it appears even if the API hasn't updated yet
                    if (newPack != null) {
                        val currentPacks = _packs.value.toMutableList()
                        // Check if pack already exists (avoid duplicates)
                        if (!currentPacks.any { it.id == newPack.id }) {
                            currentPacks.add(0, newPack) // Add at the beginning
                            _packs.value = currentPacks
                            // Update filtered packs immediately
                            val filtered = applyFilters(
                                currentPacks,
                                _searchQuery.value,
                                _filterStatus.value,
                                _filterDestination.value,
                                _filterPriceRange.value,
                                _filterTypeOffre.value
                            )
                            _filteredPacks.value = filtered
                            android.util.Log.d("AgencyPacksViewModel", "Added new pack to list: ${newPack.id}, total packs: ${currentPacks.size}, filtered: ${filtered.size}")
                        }
                    }
                    
                    // Also refresh from server to get the complete data
                    // Add a small delay to ensure backend has processed the new pack
                    kotlinx.coroutines.delay(500)
                    loadMyPacks()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Erreur lors de la création"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de la création"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update an existing pack
     */
    fun updatePack(id: String, request: UpdatePackRequest) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val result = repo.updateOffer(id, request)

                if (result.isSuccess) {
                    _successMessage.value = "Pack modifié avec succès"
                    loadMyPacks() // Refresh list
                } else {
                    val error = result.exceptionOrNull()
                    _error.value = when {
                        error?.message?.contains("403") == true ||
                        error?.message?.contains("not allowed") == true -> "Vous n'avez pas la permission de modifier ce pack"
                        else -> error?.message ?: "Erreur lors de la modification"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de la modification"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Sort packs by different criteria
     */
    fun sortPacks(sortBy: SortOption) {
        val sorted = when (sortBy) {
            SortOption.PRICE_ASC -> _filteredPacks.value.sortedBy { it.adultPricePerNight }
            SortOption.PRICE_DESC -> _filteredPacks.value.sortedByDescending { it.adultPricePerNight }
            SortOption.DATE_ASC -> _filteredPacks.value.sortedBy { it.dateDebut }
            SortOption.DATE_DESC -> _filteredPacks.value.sortedByDescending { it.dateDebut }
            SortOption.TITLE -> _filteredPacks.value.sortedBy { it.titre }
        }
        _filteredPacks.value = sorted
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
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    /**
     * Apply status filter
     */
    fun setFilterStatus(status: FilterStatus?) {
        _filterStatus.value = status
    }

    /**
     * Apply destination filter
     */
    fun setFilterDestination(destination: String?) {
        _filterDestination.value = destination
    }

    /**
     * Apply price range filter
     */
    fun setFilterPriceRange(range: PriceRange?) {
        _filterPriceRange.value = range
    }

    /**
     * Apply type offre filter
     */
    fun setFilterTypeOffre(type: String?) {
        _filterTypeOffre.value = type
    }

    /**
     * Clear all filters
     */
    fun clearFilters() {
        _filterStatus.value = null
        _filterDestination.value = null
        _filterPriceRange.value = null
        _filterTypeOffre.value = null
    }
}

enum class SortOption {
    PRICE_ASC,
    PRICE_DESC,
    DATE_ASC,
    DATE_DESC,
    TITLE
}

enum class FilterStatus {
    ALL,
    ACTIVE,
    INACTIVE
}

data class PriceRange(
    val min: Double,
    val max: Double
)