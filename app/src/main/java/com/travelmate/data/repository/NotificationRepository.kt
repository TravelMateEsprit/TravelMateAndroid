package com.travelmate.data.repository

import com.travelmate.data.api.NotificationApiService
import com.travelmate.data.model.NotificationModel
import com.travelmate.data.model.RegisterTokenRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val apiService: NotificationApiService
) {
    private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notifications: Flow<List<NotificationModel>> = _notifications.asStateFlow()
    
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: Flow<Int> = _unreadCount.asStateFlow()
    
    suspend fun registerFcmToken(token: String): Result<Unit> {
        return try {
            val response = apiService.registerFcmToken(
                RegisterTokenRequest(token = token, platform = "android")
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to register token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun unregisterFcmToken(token: String): Result<Unit> {
        return try {
            val response = apiService.unregisterFcmToken(
                RegisterTokenRequest(token = token, platform = "android")
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to unregister token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun fetchNotifications(): Result<List<NotificationModel>> {
        return try {
            val response = apiService.getNotifications()
            if (response.isSuccessful) {
                val notificationsList = response.body() ?: emptyList()
                _notifications.value = notificationsList
                Result.success(notificationsList)
            } else {
                Result.failure(Exception("Failed to fetch notifications: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun fetchUnreadCount(): Result<Int> {
        return try {
            val response = apiService.getUnreadCount()
            if (response.isSuccessful) {
                val count = response.body()?.count ?: 0
                _unreadCount.value = count
                Result.success(count)
            } else {
                Result.failure(Exception("Failed to fetch unread count: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun markAsRead(notificationId: String): Result<NotificationModel> {
        return try {
            val response = apiService.markAsRead(notificationId)
            if (response.isSuccessful) {
                val updatedNotification = response.body()!!
                // Update local cache
                _notifications.value = _notifications.value.map { 
                    if (it._id == notificationId) updatedNotification else it 
                }
                // Update unread count
                _unreadCount.value = maxOf(0, _unreadCount.value - 1)
                Result.success(updatedNotification)
            } else {
                Result.failure(Exception("Failed to mark as read: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun markAllAsRead(): Result<Unit> {
        return try {
            val response = apiService.markAllAsRead()
            if (response.isSuccessful) {
                // Update local cache
                _notifications.value = _notifications.value.map { 
                    it.copy(isRead = true) 
                }
                _unreadCount.value = 0
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to mark all as read: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun addNotificationLocally(notification: NotificationModel) {
        _notifications.value = listOf(notification) + _notifications.value
        if (!notification.isRead) {
            _unreadCount.value += 1
        }
    }
}
