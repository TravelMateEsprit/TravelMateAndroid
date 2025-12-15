package com.travelmate.data.api

import com.travelmate.data.models.Notification
import com.travelmate.data.models.UnreadCountResponse
import retrofit2.Response
import retrofit2.http.*

interface NotificationApi {
    
    @GET("api/notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String,
        @Query("unreadOnly") unreadOnly: Boolean? = null
    ): Response<List<Notification>>
    
    @GET("api/notifications/unread-count")
    suspend fun getUnreadCount(
        @Header("Authorization") token: String
    ): Response<UnreadCountResponse>
    
    @PUT("api/notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") notificationId: String,
        @Header("Authorization") token: String
    ): Response<Notification>
    
    @PUT("api/notifications/read-all")
    suspend fun markAllAsRead(
        @Header("Authorization") token: String
    ): Response<Unit>
    
    @DELETE("api/notifications/{id}")
    suspend fun deleteNotification(
        @Path("id") notificationId: String,
        @Header("Authorization") token: String
    ): Response<Unit>
    
    @DELETE("api/notifications/read-all")
    suspend fun deleteAllRead(
        @Header("Authorization") token: String
    ): Response<Unit>
}

