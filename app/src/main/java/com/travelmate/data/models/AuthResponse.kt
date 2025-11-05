package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String? = null, // Kept for backward compatibility
    val user: User? = null // New field with full user object
)
