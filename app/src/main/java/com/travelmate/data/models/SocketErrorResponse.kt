package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class SocketErrorResponse(
    val message: String,
    val errors: Map<String, String>? = null
)
