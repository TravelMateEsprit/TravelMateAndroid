package com.travelmate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.CreatePackRequest
import com.travelmate.data.repository.PacksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatePackViewModel @Inject constructor(
    private val repo: PacksRepository
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success = _success.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun submitPack(request: CreatePackRequest) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val result = repo.createOffer(request)

                if (result.isSuccess) {
                    _success.value = true
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Erreur lors de la création du pack"
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur inconnue"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun resetSuccess() {
        _success.value = false
    }

}