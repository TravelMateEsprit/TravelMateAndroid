package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String? = null,
    val bio: String? = null,
    val socialLinks: List<SocialLink>? = null,
    val avatar: String? = null
)

@Serializable
data class UpdateAgencyProfileRequest(
    val name: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String? = null,
    val bio: String? = null,
    val socialLinks: List<SocialLink>? = null,
    val avatar: String? = null,
    
    val agencyName: String? = null,
    val agencyLicense: String? = null,
    val agencyWebsite: String? = null,
    val agencyDescription: String? = null,
    val logo: String? = null,
    val openingHours: OpeningHours? = null,
    val location: Location? = null
)
