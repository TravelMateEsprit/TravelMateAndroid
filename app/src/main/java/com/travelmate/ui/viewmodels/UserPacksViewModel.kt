package com.travelmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.Pack
import com.travelmate.data.models.Reservation
import com.travelmate.data.repository.FavoritesRepository
import com.travelmate.data.repository.PacksRepository
import com.travelmate.data.repository.ReservationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserPacksViewModel @Inject constructor(
    private val packsRepository: PacksRepository,
    private val reservationsRepository: ReservationsRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _availablePacks = MutableStateFlow<List<Pack>>(emptyList())
    val availablePacks: StateFlow<List<Pack>> = _availablePacks.asStateFlow()

    private val _favoritePacks = MutableStateFlow<Set<String>>(emptySet())
    val favoritePacks: StateFlow<Set<String>> = _favoritePacks.asStateFlow()

    private val _favoritePackObjects = MutableStateFlow<List<Pack>>(emptyList())
    val favoritePackObjects: StateFlow<List<Pack>> = _favoritePackObjects.asStateFlow()

    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    val reservations: StateFlow<List<Reservation>> = _reservations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        refreshFavorites()
        loadAvailablePacks()
        loadReservations()
    }

    /**
     * Load all available packs (active only)
     */
    fun loadAvailablePacks() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _availablePacks.value = packsRepository.getAllActiveOffers()
                // Refresh favorites to ensure sync with backend
                refreshFavorites()
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors du chargement"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load user reservations
     */
    fun loadReservations() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _reservations.value = reservationsRepository.getUserReservations()
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors du chargement"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Reserve a pack
     */
    fun reservePack(packId: String, notes: String? = null, people: Int = 1, totalPrice: Double? = null) {
        val pack = _availablePacks.value.find { it.id == packId } ?: return
        val finalPrice = totalPrice ?: pack.prix
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val result = reservationsRepository.createReservationForOffer(
                    packId = pack.id,
                    price = finalPrice,
                    notes = notes,
                    people = people
                )
                if (result.isSuccess) {
                    loadReservations()
                    _successMessage.value = "Réservation créée avec succès"
                } else {
                    _error.value = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de la réservation"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Refresh favorites from the repository
     */
    fun refreshFavorites() {
        viewModelScope.launch {
            try {
                val favorites = favoritesRepository.fetchFavorites()
                _favoritePacks.value = favorites.map { it.id }.toSet()
                _favoritePackObjects.value = favorites
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * Load favorite packs directly
     */
    fun loadFavoritePacks() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val favorites = favoritesRepository.fetchFavorites()
                _favoritePackObjects.value = favorites
                _favoritePacks.value = favorites.map { it.id }.toSet()
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors du chargement des favoris"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Add pack to favorites
     */
    fun addToFavorites(packId: String) {
        viewModelScope.launch {
            val result = favoritesRepository.addFavorite(packId)
            if (result.isSuccess) {
                _favoritePacks.value = _favoritePacks.value + packId
                // Add the pack object to favorites if we have it
                val packToAdd = _availablePacks.value.find { it.id == packId }
                if (packToAdd != null) {
                    _favoritePackObjects.value = _favoritePackObjects.value + packToAdd
                } else {
                    // If we don't have the pack locally, refresh favorites from backend
                    refreshFavorites()
                }
                _successMessage.value = "Ajouté aux favoris"
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }

    /**
     * Remove pack from favorites
     */
    fun removeFromFavorites(packId: String) {
        viewModelScope.launch {
            val result = favoritesRepository.removeFavorite(packId)
            if (result.isSuccess) {
                _favoritePacks.value = _favoritePacks.value - packId
                // Remove the pack object from favorites
                _favoritePackObjects.value = _favoritePackObjects.value.filter { it.id != packId }
                _successMessage.value = "Retiré des favoris"
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }

    /**
     * Check if a pack is in favorites
     */
    fun isFavorite(packId: String): Boolean = _favoritePacks.value.contains(packId)

    /**
     * Clear error message
     */
    fun clearError() { _error.value = null }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() { _successMessage.value = null }
}

