package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupRequest(
    val name: String,
    val description: String,
    val destination: String? = null,  // ✅ AJOUTÉ
    val image: String? = null
)