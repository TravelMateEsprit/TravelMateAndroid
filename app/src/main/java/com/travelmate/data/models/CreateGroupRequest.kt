package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupRequest(
    val name: String,
    val description: String,
    val image: String? = null
    // Note: destination n'est pas dans le DTO backend, même si présent dans le schema
)