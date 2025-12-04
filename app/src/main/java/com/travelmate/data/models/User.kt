package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val _id: String,
    val email: String,
    val name: String? = null,
    val userType: String,
    val status: String = "active",
    val phone: String? = null,
    val avatar: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String? = null,
    
    // Agency specific
    val agencyName: String? = null,
    val agencyLicense: String? = null,
    val agencyWebsite: String? = null,
    val agencyDescription: String? = null,
    val isAgencyVerified: Boolean? = false,
    val signatureUrl: String? = null,
    val signatureName: String? = null,
    
    // New fields
    val bio: String? = null,
    val socialLinks: List<SocialLink>? = null,
    // val preferences: Map<String, String>? = null, // Complex object handling might require custom serializer
    val logo: String? = null,
    val openingHours: OpeningHours? = null,
    val location: Location? = null
)

@Serializable
data class SocialLink(
    val platform: String,
    val url: String
)

@Serializable
data class OpeningHours(
    val monday: String? = null,
    val tuesday: String? = null,
    val wednesday: String? = null,
    val thursday: String? = null,
    val friday: String? = null,
    val saturday: String? = null,
    val sunday: String? = null
)

@Serializable
data class Location(
    val lat: Double,
    val lng: Double
)
