package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordWithCodeRequest(
    val email: String,
    val code: String,
    val newPassword: String
)
