
package com.travelmate.data.models

data class Conversation(
    val id: String,
    val clientName: String,
    val lastMessage: String,
    val timestamp: Long
)
