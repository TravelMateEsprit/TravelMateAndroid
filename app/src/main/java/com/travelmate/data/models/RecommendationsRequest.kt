package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class RecommendationsRequest(
    val preferences: Preferences
)

