package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)
