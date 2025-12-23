package com.travelmate.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Agency registration request matching backend SignupAgencyDto
 * Backend accepts: name, email, password, agencyName, agencyLicense, 
 * agencyWebsite (optional), phone, address, city, country, agencyDescription (optional)
 */
@Serializable
data class AgencyRegistrationRequest(
    @SerialName("name")
    val name: String, // Contact person full name (e.g., "Mohamed Ben Ali")
    
    @SerialName("email")
    val email: String,
    
    @SerialName("password")
    val password: String,
    
    @SerialName("agencyName")
    val agencyName: String,
    
    @SerialName("agencyLicense")
    val agencyLicense: String, // License number (e.g., "LIC-2024-12345")
    
    @SerialName("agencyWebsite")
    val agencyWebsite: String? = null,
    
    @SerialName("phone")
    val phone: String,
    
    @SerialName("address")
    val address: String,
    
    @SerialName("city")
    val city: String,
    
    @SerialName("country")
    val country: String,
    
    @SerialName("agencyDescription")
    val agencyDescription: String? = null
)
