package com.travelmate.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class UserRegistrationRequest(
    @SerialName("name")
    val name: String,
    
    @SerialName("email")
    val email: String,
    
    @SerialName("password")
    val password: String
)
