package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class DeleteMessageResponse(
    val message: String
)