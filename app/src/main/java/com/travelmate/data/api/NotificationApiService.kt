package com.travelmate.data.api

import com.travelmate.data.model.NotificationModel
import com.travelmate.data.model.NotificationsResponse
import com.travelmate.data.model.RegisterTokenRequest
import com.travelmate.data.model.UnreadCountResponse
import retrofit2.Response
import retrofit2.http.*

interface NotificationApiService {
    
    @POST("notifications/register-token")
    suspend fun registerFcmToken(
        @Body request: RegisterTokenRequest
    ): Response<Unit>
    
    @HTTP(method = "DELETE", path = "notifications/unregister-token", hasBody = true)
    suspend fun unregisterFcmToken(
        @Body request: RegisterTokenRequest
    ): Response<Unit>
    
    @GET("notifications")
    suspend fun getNotifications(): Response<List<NotificationModel>>
    
    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): Response<UnreadCountResponse>
    
    @PATCH("notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") id: String
    ): Response<NotificationModel>
    
    @PATCH("notifications/read-all")
    suspend fun markAllAsRead(): Response<Unit>
}
