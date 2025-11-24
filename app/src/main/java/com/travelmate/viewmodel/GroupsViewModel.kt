package com.travelmate.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.*
import com.travelmate.data.service.GroupsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val groupsService: GroupsService
) : ViewModel() {

    val allGroups = groupsService.allGroups
    val myGroups = groupsService.myGroups
    val myCreatedGroups = groupsService.myCreatedGroups
    val currentGroup = groupsService.currentGroup
    val groupMessages = groupsService.groupMessages
    val isLoading = groupsService.isLoading
    val error = groupsService.error

    // ✅ AJOUTÉ : StateFlow pour les demandes en attente
    private val _pendingRequests = MutableStateFlow<List<PendingRequest>>(emptyList())
    val pendingRequests: StateFlow<List<PendingRequest>> = _pendingRequests.asStateFlow()

    private val _filterQuery = MutableStateFlow("")
    val filterQuery: StateFlow<String> = _filterQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.RECENT)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _filteredGroups = MutableStateFlow<List<Group>>(emptyList())
    val filteredGroups: StateFlow<List<Group>> = _filteredGroups.asStateFlow()

    init {
        viewModelScope.launch {
            allGroups.collect { groups ->
                applyFiltersAndSort(groups)
            }
        }
    }

    fun loadAllGroups() {
        viewModelScope.launch {
            groupsService.getAllGroups()
        }
    }

    fun loadGroupById(groupId: String) {
        viewModelScope.launch {
            groupsService.getGroupById(groupId)
        }
    }

    fun createGroup(name: String, destination: String, description: String, imageUrl: String? = null) {
        viewModelScope.launch {
            val request = mutableMapOf(
                "name" to name.trim(),
                "description" to description.trim(),
                "destination" to destination.trim()
            )
            imageUrl?.let { request["image"] = it }

            val result = groupsService.createGroup(request)
            if (result.isSuccess) {
                loadAllGroups()
            }
        }
    }

    fun updateGroup(groupId: String, name: String?, destination: String?, description: String?, imageUrl: String?) {
        viewModelScope.launch {
            val result = groupsService.updateGroup(groupId, name, destination, description, imageUrl)
            if (result.isSuccess) {
                loadAllGroups()
                loadGroupById(groupId)
            }
        }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            val result = groupsService.deleteGroup(groupId)
            if (result.isSuccess) {
                loadAllGroups()
            }
        }
    }

    fun joinGroup(groupId: String) {
        viewModelScope.launch {
            val result = groupsService.joinGroup(groupId)
            if (result.isSuccess) {
                loadAllGroups()
            }
        }
    }

    fun leaveGroup(groupId: String) {
        viewModelScope.launch {
            val result = groupsService.leaveGroup(groupId)
            if (result.isSuccess) {
                loadAllGroups()
            }
        }
    }

    // ✅ NOUVEAU : Fonctions pour gérer les demandes
    fun loadPendingRequests(groupId: String) {
        viewModelScope.launch {
            try {
                val requests = groupsService.getPendingRequests(groupId)
                _pendingRequests.value = requests
                Log.d("GroupsViewModel", "✅ Loaded ${requests.size} pending requests")
            } catch (e: Exception) {
                Log.e("GroupsViewModel", "❌ Error loading pending requests", e)
            }
        }
    }

    fun approveMember(groupId: String, userId: String) {
        viewModelScope.launch {
            try {
                groupsService.approveMember(groupId, userId)
                // Recharger les demandes et les groupes
                loadPendingRequests(groupId)
                loadGroupById(groupId)
                loadAllGroups()
                Log.d("GroupsViewModel", "✅ Member approved")
            } catch (e: Exception) {
                Log.e("GroupsViewModel", "❌ Error approving member", e)
                groupsService.setError("Erreur lors de l'approbation")
            }
        }
    }

    fun rejectMember(groupId: String, userId: String) {
        viewModelScope.launch {
            try {
                groupsService.rejectMember(groupId, userId)
                // Recharger les demandes
                loadPendingRequests(groupId)
                Log.d("GroupsViewModel", "✅ Member rejected")
            } catch (e: Exception) {
                Log.e("GroupsViewModel", "❌ Error rejecting member", e)
                groupsService.setError("Erreur lors du rejet")
            }
        }
    }

    fun loadGroupMessages(groupId: String) {
        viewModelScope.launch {
            groupsService.getGroupMessages(groupId)
        }
    }

    fun createMessage(groupId: String, content: String, images: List<String> = emptyList()) {
        viewModelScope.launch {
            groupsService.createMessage(groupId, content, images)
        }
    }

    fun deleteMessage(groupId: String, messageId: String) {
        viewModelScope.launch {
            groupsService.deleteMessage(groupId, messageId)
        }
    }

    fun updateMessage(groupId: String, messageId: String, newContent: String) {
        viewModelScope.launch {
            groupsService.updateMessage(groupId, messageId, newContent)
        }
    }

    fun uploadGroupImage(imageUri: Uri, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = groupsService.uploadGroupImage(imageUri)

            if (result.isSuccess) {
                result.getOrNull()?.let { onSuccess(it) }
            } else {
                val error = result.exceptionOrNull()?.message ?: "Erreur upload"
                onError(error)
            }
        }
    }

    fun setFilterQuery(query: String) {
        _filterQuery.value = query
        applyFiltersAndSort(allGroups.value)
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
        applyFiltersAndSort(allGroups.value)
    }

    private fun applyFiltersAndSort(groups: List<Group>) {
        var filtered = groups

        if (_filterQuery.value.isNotBlank()) {
            filtered = filtered.filter { group ->
                group.name.contains(_filterQuery.value, ignoreCase = true) ||
                        group.description.contains(_filterQuery.value, ignoreCase = true) ||
                        (group.destination?.contains(_filterQuery.value, ignoreCase = true) == true)
            }
        }

        filtered = when (_sortOption.value) {
            SortOption.RECENT -> filtered.sortedByDescending { it.createdAt }
            SortOption.OLDEST -> filtered.sortedBy { it.createdAt }
            SortOption.NAME_AZ -> filtered.sortedBy { it.name.lowercase() }
            SortOption.NAME_ZA -> filtered.sortedByDescending { it.name.lowercase() }
            SortOption.MOST_MEMBERS -> filtered.sortedByDescending { it.memberCount }
            SortOption.LEAST_MEMBERS -> filtered.sortedBy { it.memberCount }
        }

        _filteredGroups.value = filtered
    }

    fun clearError() {
        groupsService.clearError()
    }
}

enum class SortOption(val label: String) {
    RECENT("Plus récents"),
    OLDEST("Plus anciens"),
    NAME_AZ("Nom (A-Z)"),
    NAME_ZA("Nom (Z-A)"),
    MOST_MEMBERS("Plus de membres"),
    LEAST_MEMBERS("Moins de membres")
}