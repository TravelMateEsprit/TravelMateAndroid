package com.travelmate.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    @SerialName("_id")
    val id: String = "",
    
    @SerialName("sender_id")
    val senderId: String,
    
    @SerialName("receiver_id")
    val receiverId: String,
    
    @SerialName("message")
    val message: String,
    
    @SerialName("pack_id")
    val packId: String? = null,
    
    @SerialName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    
    @SerialName("read")
    val read: Boolean = false
)

@Serializable
data class ChatConversation(
    @SerialName("_id")
    val id: String = "",
    
    @SerialName("user_id")
    val userId: String,
    
    @SerialName("user_name")
    val userName: String,
    
    @SerialName("agency_id")
    val agencyId: String,
    
    @SerialName("agency_name")
    val agencyName: String,
    
    @SerialName("pack_id")
    val packId: String? = null,
    
    @SerialName("last_message")
    val lastMessage: String? = null,
    
    @SerialName("last_message_time")
    val lastMessageTime: Long = 0,
    
    @SerialName("unread_count")
    val unreadCount: Int = 0
)

