package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class SearchInsuranceResponse(
    val insurances: List<Insurance>,
    val total: Int,
    val page: Int,
    val limit: Int
)
