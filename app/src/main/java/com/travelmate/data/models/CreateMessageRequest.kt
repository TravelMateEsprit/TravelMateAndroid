package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateMessageRequest(
    val content: String = "",
    val images: List<String> = emptyList()
) {
    val hasContent: Boolean get() = content.isNotBlank() || images.isNotEmpty()
}