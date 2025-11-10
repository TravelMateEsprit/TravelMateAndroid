package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateGroupRequest(
    val name: String? = null,
    val description: String? = null,
    val image: String? = null,
    val status: String? = null  // String au lieu d'enum
    // Note: destination n'est pas dans le DTO backend UpdateGroupDto
)