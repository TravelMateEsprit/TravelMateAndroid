package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthResetCodeRequest(
    val email: String,
    val code: String
)
