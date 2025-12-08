package com.travelmate.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class AgencyRegistrationRequest(
    @SerialName("name")
    val name: String,
    
    @SerialName("email")
    val email: String,
    
    @SerialName("password")
    val password: String,
    
    @SerialName("agencyName")
    val agencyName: String,
    
    @SerialName("agencyLicense")
    val agencyLicense: String,
    
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
