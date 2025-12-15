package com.travelmate.data.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.travelmate.data.api.NotificationApi
import com.travelmate.data.models.Notification
import com.travelmate.data.models.UnreadCountResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    private val notificationApi: NotificationApi,
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("travelmate_prefs", Context.MODE_PRIVATE)

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private fun getAuthToken(): String {
        val token = prefs.getString("access_token", "") ?: ""
        return "Bearer $token"
    }

    suspend fun getNotifications(unreadOnly: Boolean = false) {
        _isLoading.value = true
        _error.value = null
        try {
            val response = notificationApi.getNotifications(getAuthToken(), unreadOnly.takeIf { it })
            if (response.isSuccessful && response.body() != null) {
                _notifications.value = response.body()!!
                Log.d("NotificationService", "Loaded ${response.body()!!.size} notifications")
            } else {
                val errorMsg = "Erreur lors de la récupération des notifications - HTTP ${response.code()}"
                _error.value = errorMsg
                Log.e("NotificationService", errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Exception: ${e.javaClass.simpleName} - ${e.message}"
            _error.value = errorMsg
            Log.e("NotificationService", errorMsg, e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun getUnreadCount() {
        try {
            val response = notificationApi.getUnreadCount(getAuthToken())
            if (response.isSuccessful && response.body() != null) {
                _unreadCount.value = response.body()!!.count
                Log.d("NotificationService", "Unread count: ${response.body()!!.count}")
            } else {
                Log.e("NotificationService", "Error getting unread count - HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("NotificationService", "Exception getting unread count", e)
        }
    }

    suspend fun markAsRead(notificationId: String) {
        try {
            val response = notificationApi.markAsRead(notificationId, getAuthToken())
            if (response.isSuccessful) {
                // Update local state
                _notifications.value = _notifications.value.map { notif ->
                    if (notif.id == notificationId) notif.copy(read = true) else notif
                }
                // Refresh unread count
                getUnreadCount()
                Log.d("NotificationService", "Marked notification as read: $notificationId")
            } else {
                Log.e("NotificationService", "Error marking as read - HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("NotificationService", "Exception marking as read", e)
        }
    }

    suspend fun markAllAsRead() {
        try {
            val response = notificationApi.markAllAsRead(getAuthToken())
            if (response.isSuccessful) {
                // Update local state
                _notifications.value = _notifications.value.map { it.copy(read = true) }
                _unreadCount.value = 0
                Log.d("NotificationService", "Marked all notifications as read")
            } else {
                Log.e("NotificationService", "Error marking all as read - HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("NotificationService", "Exception marking all as read", e)
        }
    }

    suspend fun deleteNotification(notificationId: String) {
        try {
            // Supprimer localement d'abord pour une meilleure UX
            val notificationToDelete = _notifications.value.find { it.id == notificationId }
            _notifications.value = _notifications.value.filter { it.id != notificationId }
            
            // Essayer de supprimer côté backend
            val response = notificationApi.deleteNotification(notificationId, getAuthToken())
            if (response.isSuccessful) {
                Log.d("NotificationService", "✅ Deleted notification from backend: $notificationId")
            } else {
                // Si le backend n'a pas encore implémenté l'endpoint, on continue quand même
                Log.w("NotificationService", "⚠️ Backend delete failed (HTTP ${response.code()}), but notification removed locally")
                // Si c'était une notification non lue, mettre à jour le compteur
                if (notificationToDelete != null && !notificationToDelete.read) {
                    _unreadCount.value = maxOf(0, _unreadCount.value - 1)
                }
            }
            // Refresh unread count
            getUnreadCount()
        } catch (e: Exception) {
            // Même en cas d'exception, on garde la suppression locale
            Log.w("NotificationService", "⚠️ Exception deleting notification, but removed locally", e)
            // Si c'était une notification non lue, mettre à jour le compteur
            val notificationToDelete = _notifications.value.find { it.id == notificationId }
            if (notificationToDelete != null && !notificationToDelete.read) {
                _unreadCount.value = maxOf(0, _unreadCount.value - 1)
            }
        }
    }

    suspend fun deleteAllReadNotifications() {
        try {
            // Compter les notifications lues avant suppression
            val readCount = _notifications.value.count { it.read }
            
            // Supprimer localement d'abord
            _notifications.value = _notifications.value.filter { !it.read }
            
            // Essayer de supprimer côté backend
            val response = notificationApi.deleteAllRead(getAuthToken())
            if (response.isSuccessful) {
                Log.d("NotificationService", "✅ Deleted $readCount read notifications from backend")
            } else {
                // Si le backend n'a pas encore implémenté l'endpoint, on continue quand même
                Log.w("NotificationService", "⚠️ Backend delete all read failed (HTTP ${response.code()}), but removed locally")
            }
            // Refresh unread count
            getUnreadCount()
        } catch (e: Exception) {
            // Même en cas d'exception, on garde la suppression locale
            Log.w("NotificationService", "⚠️ Exception deleting all read, but removed locally", e)
        }
    }
}

