package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class SocketMessage(
    val type: String,
    val data: String,
    val timestamp: Long = System.currentTimeMillis()
)
