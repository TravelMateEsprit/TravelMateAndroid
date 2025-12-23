package com.travelmate.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * User registration request matching backend SignupDto
 * Backend only accepts: name, email, password
 */
@Serializable
data class UserRegistrationRequest(
    @SerialName("name")
    val name: String, // Full name (e.g., "Jean Dupont")
    
    @SerialName("email")
    val email: String,
    
    @SerialName("password")
    val password: String
)
