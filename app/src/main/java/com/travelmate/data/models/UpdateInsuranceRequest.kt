package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateInsuranceRequest(
    val name: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val duration: String? = null, // Format: "1 mois", "1 an", "voyage unique"
    val coverage: List<String>? = null,
    val imageUrl: String? = null,
    val conditions: InsuranceConditions? = null,
    val isActive: Boolean? = null
)
