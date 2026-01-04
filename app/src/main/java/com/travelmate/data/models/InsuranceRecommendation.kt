package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class InsuranceRecommendation(
    val insuranceId: String,
    val insuranceName: String,
    val matchScore: Double,
    val reason: String,
    val matchFactors: List<String> = emptyList(),
    val pros: List<String> = emptyList(),
    val cons: List<String> = emptyList()
)

@Serializable
data class InsuranceRecommendationsResponse(
    val recommendations: List<InsuranceRecommendation> = emptyList(),
    val profileCompleteness: Int = 0,
    val message: String = ""
)
