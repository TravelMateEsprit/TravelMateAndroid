package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class InsuranceConditions(
    val ageMin: Int? = null,
    val ageMax: Int? = null,
    val destination: List<String> = emptyList(),
    val other: String? = null
)
