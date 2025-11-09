package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponse(
    val message: String? = null,
    val error: String? = null,
    val errors: Map<String, String>? = null
) {
    fun getErrorMessage(): String {
        return message ?: error ?: "Une erreur est survenue"
    }
}

