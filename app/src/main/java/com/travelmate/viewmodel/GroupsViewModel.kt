package com.travelmate.viewmodel

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
    val isLoading = groupsService.isLoading
    val error = groupsService.error

    fun loadAllGroups() {
        viewModelScope.launch {
            groupsService.getAllGroups()
        }
    }

    fun createGroup(name: String, destination: String, description: String) {
        viewModelScope.launch {
            val request = mapOf(
                "name" to name.trim(),
                "description" to description.trim(),
                "destination" to destination.trim()
            )
            val result = groupsService.createGroup(request)
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

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            val result = groupsService.deleteGroup(groupId)
            if (result.isSuccess) {
                loadAllGroups()
            }
        }
    }

    fun clearError() {
        groupsService.clearError()
    }
}