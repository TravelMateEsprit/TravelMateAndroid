package com.travelmate.data.model

import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class NotificationModel(
    val _id: String,
    val userId: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val data: Map<String, String> = emptyMap(),
    val isRead: Boolean = false,
    val isSent: Boolean = false,
    val createdAt: String,
    val updatedAt: String? = null
) {
    fun getFormattedTime(): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(createdAt.substring(0, 19)) ?: return createdAt
            val now = Date()
            
            val diffInMillis = now.time - date.time
            val minutes = diffInMillis / (1000 * 60)
            val hours = diffInMillis / (1000 * 60 * 60)
            val days = diffInMillis / (1000 * 60 * 60 * 24)
            
            when {
                minutes < 1 -> "À l'instant"
                minutes < 60 -> "Il y a ${minutes}m"
                hours < 24 -> "Il y a ${hours}h"
                days < 7 -> "Il y a ${days}j"
                else -> {
                    val displayFormat = SimpleDateFormat("dd MMM", Locale.FRENCH)
                    displayFormat.format(date)
                }
            }
        } catch (e: Exception) {
            createdAt
        }
    }
}

@Serializable
enum class NotificationType {
    NEW_INSURANCE_REQUEST,
    REQUEST_STATUS_CHANGED,
    PAYMENT_CONFIRMED,
    PAYMENT_FAILED,
    NEW_INSURANCE_PRODUCT,
    SUBSCRIPTION_CONFIRMED
}

@Serializable
data class RegisterTokenRequest(
    val token: String,
    val platform: String = "android"
)

@Serializable
data class UnreadCountResponse(
    val count: Int
)

@Serializable
data class NotificationsResponse(
    val notifications: List<NotificationModel>
)
