package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTravelProfileResponse(
    val travelProfile: TravelProfile?,
    val profileCompletionPercentage: Int
)
