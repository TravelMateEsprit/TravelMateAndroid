package com.travelmate.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoveMemberResponse(
    @SerialName("message")
    val message: String,
    @SerialName("targetUserId")
    val targetUserId: String,
    @SerialName("action")
    val action: String,
    @SerialName("groupId")
    val groupId: String
)
