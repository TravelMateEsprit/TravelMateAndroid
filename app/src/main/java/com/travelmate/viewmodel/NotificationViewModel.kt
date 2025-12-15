package com.travelmate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.User
import com.travelmate.data.service.GroupsService
import com.travelmate.data.service.NotificationService
import com.travelmate.data.service.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationService: NotificationService,
    private val groupsService: GroupsService,
    private val userService: UserService
) : ViewModel() {
    
    private val _userInfo = MutableStateFlow<User?>(null)
    val userInfo: StateFlow<User?> = _userInfo.asStateFlow()
    
    private val _isLoadingUserInfo = MutableStateFlow(false)
    val isLoadingUserInfo: StateFlow<Boolean> = _isLoadingUserInfo.asStateFlow()

    val notifications = notificationService.notifications
    val unreadCount = notificationService.unreadCount
    val isLoading = notificationService.isLoading
    val error = notificationService.error

    init {
        loadNotifications()
        loadUnreadCount()
    }

    fun loadNotifications(unreadOnly: Boolean = false) {
        viewModelScope.launch {
            notificationService.getNotifications(unreadOnly)
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            notificationService.getUnreadCount()
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationService.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationService.markAllAsRead()
        }
    }

    fun loadUserInfo(userId: String) {
        viewModelScope.launch {
            _isLoadingUserInfo.value = true
            try {
                val result = userService.getUserProfileById(userId)
                if (result.isSuccess) {
                    _userInfo.value = result.getOrNull()
                } else {
                    Log.e("NotificationViewModel", "❌ Error loading user info", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "❌ Exception loading user info", e)
            } finally {
                _isLoadingUserInfo.value = false
            }
        }
    }

    fun clearUserInfo() {
        _userInfo.value = null
    }

    fun approveGroupRequest(groupId: String, userId: String, notificationId: String) {
        viewModelScope.launch {
            try {
                Log.d("NotificationViewModel", "✅ Approving group request: groupId=$groupId, userId=$userId, notificationId=$notificationId")
                
                // Supprimer la notification immédiatement (localement) pour une meilleure UX
                deleteNotification(notificationId)
                
                // Ensuite, approuver le membre
                groupsService.approveMember(groupId, userId)
                
                Log.d("NotificationViewModel", "✅ Group request approved and notification deleted")
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "❌ Error approving group request", e)
                // La notification a déjà été supprimée localement, donc pas besoin de rollback
            }
        }
    }

    fun rejectGroupRequest(groupId: String, userId: String, notificationId: String) {
        viewModelScope.launch {
            try {
                Log.d("NotificationViewModel", "❌ Rejecting group request: groupId=$groupId, userId=$userId, notificationId=$notificationId")
                
                // Supprimer la notification immédiatement (localement) pour une meilleure UX
                deleteNotification(notificationId)
                
                // Ensuite, rejeter le membre
                groupsService.rejectMember(groupId, userId)
                
                Log.d("NotificationViewModel", "✅ Group request rejected and notification deleted")
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "❌ Error rejecting group request", e)
                // La notification a déjà été supprimée localement, donc pas besoin de rollback
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationService.deleteNotification(notificationId)
            loadNotifications()
            loadUnreadCount()
        }
    }

    fun deleteAllReadNotifications() {
        viewModelScope.launch {
            notificationService.deleteAllReadNotifications()
            loadNotifications()
            loadUnreadCount()
        }
    }

    fun refresh() {
        loadNotifications()
        loadUnreadCount()
    }
}

