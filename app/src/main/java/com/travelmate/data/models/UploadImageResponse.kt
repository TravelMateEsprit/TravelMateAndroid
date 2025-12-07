package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UploadImageResponse(
    val imageUrl: String
)
