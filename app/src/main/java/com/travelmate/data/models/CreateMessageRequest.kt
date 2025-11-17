package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateMessageRequest(
    val content: String,
    val images: List<String> = emptyList()
)