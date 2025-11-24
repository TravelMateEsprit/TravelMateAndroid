package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Claim(
    val _id: String,
    val userId: UserBasicInfo? = null,
    val insuranceRequestId: InsuranceRequestBasicInfo,
    val agencyId: UserBasicInfo,
    val subject: String,
    val description: String,
    val status: String, // EN_ATTENTE, EN_COURS, RESOLUE, FERMEE
    val priority: String, // BASSE, MOYENNE, HAUTE, URGENTE
    val attachments: List<String> = emptyList(),
    val agencyResponse: String? = null,
    val respondedBy: UserBasicInfo? = null,
    val respondedAt: String? = null,
    val isReadByUser: Boolean = false,
    val isReadByAgency: Boolean = false,
    val responseCount: Int = 0,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class UserBasicInfo(
    val _id: String,
    val name: String,
    val email: String
)

@Serializable
data class InsuranceRequestBasicInfo(
    val _id: String,
    val travelerName: String,
    val destination: String,
    val departureDate: String,
    val returnDate: String
)

@Serializable
data class CreateClaimRequest(
    val insuranceRequestId: String,
    val subject: String,
    val description: String,
    val attachments: List<String> = emptyList()
)

@Serializable
data class UpdateClaimStatusRequest(
    val status: String,
    val agencyResponse: String? = null,
    val priority: String? = null
)

@Serializable
data class ClaimUnreadCountResponse(
    val count: Int
)
