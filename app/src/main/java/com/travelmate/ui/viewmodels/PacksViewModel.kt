package com.travelmate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.Pack
import com.travelmate.data.repository.PacksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PacksViewModel @Inject constructor(
    private val repository: PacksRepository
) : ViewModel() {

    private val _packs = MutableStateFlow<List<Pack>>(emptyList())
    val packs: StateFlow<List<Pack>> = _packs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Load packs for the current agency
     */
    fun loadAgencyPacks() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _packs.value = repository.getAgencyPacks() // ✅ FIXED: No parameters
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors du chargement des packs"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load all active offers (for users browsing)
     */
    fun loadAllActiveOffers() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _packs.value = repository.getAllActiveOffers()
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors du chargement des offres"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get a single pack by ID
     */
    fun getPackById(id: String): Pack? {
        return _packs.value.find { it.id == id }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Refresh packs
     */
    fun refresh() {
        loadAgencyPacks()
    }
}