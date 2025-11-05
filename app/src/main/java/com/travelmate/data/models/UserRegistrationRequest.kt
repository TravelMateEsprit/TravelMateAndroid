package com.travelmate.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class UserRegistrationRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val dateOfBirth: String? = null // Format: yyyy-MM-dd
) {
    fun toJson(): String {
        // Créer un objet avec le format attendu par le backend
        val backendFormat = mapOf(
            "name" to "$firstName $lastName",
            "email" to email,
            "password" to password
        )
        return Json.encodeToString(backendFormat)
    }
}
