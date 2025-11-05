package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val _id: String,
    val email: String,
    val name: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val userType: String, // "user" or "agence" (admin users cannot access mobile app)
    val status: String = "active",
    val phone: String? = null,
    val address: String? = null
)
