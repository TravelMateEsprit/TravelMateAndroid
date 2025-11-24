package com.travelmate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.model.NotificationModel
import com.travelmate.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    companion object {
        private const val TAG = "NotificationsVM"
    }

    // State
    val notifications: StateFlow<List<NotificationModel>> = notificationRepository.notifications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val unreadCount: StateFlow<Int> = notificationRepository.unreadCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        fetchNotifications()
        fetchUnreadCount()
    }

    /**
     * Récupère toutes les notifications de l'utilisateur
     */
    fun fetchNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = notificationRepository.fetchNotifications()
                if (result.isFailure) {
                    _error.value = result.exceptionOrNull()?.message ?: "Erreur inconnue"
                    Log.e(TAG, "Failed to fetch notifications", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur réseau"
                Log.e(TAG, "Error fetching notifications", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Rafraîchir les notifications (pull-to-refresh)
     */
    fun refreshNotifications() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            
            try {
                val result = notificationRepository.fetchNotifications()
                if (result.isFailure) {
                    _error.value = result.exceptionOrNull()?.message ?: "Erreur de rafraîchissement"
                    Log.e(TAG, "Failed to refresh notifications", result.exceptionOrNull())
                }
                
                // Rafraîchir aussi le compteur
                notificationRepository.fetchUnreadCount()
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur réseau"
                Log.e(TAG, "Error refreshing notifications", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Récupère le nombre de notifications non lues
     */
    fun fetchUnreadCount() {
        viewModelScope.launch {
            try {
                val result = notificationRepository.fetchUnreadCount()
                if (result.isFailure) {
                    Log.e(TAG, "Failed to fetch unread count", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching unread count", e)
            }
        }
    }

    /**
     * Marque une notification comme lue
     */
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                val result = notificationRepository.markAsRead(notificationId)
                if (result.isSuccess) {
                    Log.d(TAG, "Notification marked as read: $notificationId")
                } else {
                    Log.e(TAG, "Failed to mark as read", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error marking notification as read", e)
            }
        }
    }

    /**
     * Marque toutes les notifications comme lues
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                val result = notificationRepository.markAllAsRead()
                if (result.isSuccess) {
                    Log.d(TAG, "All notifications marked as read")
                } else {
                    Log.e(TAG, "Failed to mark all as read", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error marking all notifications as read", e)
            }
        }
    }

    /**
     * Enregistre un token FCM
     */
    fun registerFcmToken(token: String) {
        viewModelScope.launch {
            try {
                val result = notificationRepository.registerFcmToken(token)
                if (result.isSuccess) {
                    Log.d(TAG, "FCM token registered successfully")
                } else {
                    Log.e(TAG, "Failed to register FCM token", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error registering FCM token", e)
            }
        }
    }

    /**
     * Efface le message d'erreur
     */
    fun clearError() {
        _error.value = null
    }
}
