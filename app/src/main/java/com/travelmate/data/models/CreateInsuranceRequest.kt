package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateInsuranceRequest(
    val name: String,
    val description: String,
    val price: Double,
    val duration: String, // Format: "1 mois", "1 an", "voyage unique"
    val coverage: List<String>,
    val imageUrl: String? = null,
    val conditions: InsuranceConditions? = null,
    val isActive: Boolean = true
)
