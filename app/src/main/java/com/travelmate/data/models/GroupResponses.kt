package com.travelmate.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiMessageResponse(  // ✅ Renommé
    @SerialName("message")
    val message: String
)

@Serializable
data class ImageUploadResponse(  // ✅ Renommé
    @SerialName("imageUrl")
    val imageUrl: String
)

@Serializable
data class GroupDeletionResponse(  // ✅ Renommé
    @SerialName("message")
    val message: String
)

@Serializable
data class GroupLeaveResponse(  // ✅ Renommé
    @SerialName("message")
    val message: String
)

@Serializable
data class MessageDeletionResponse(  // ✅ Renommé
    @SerialName("message")
    val message: String
)