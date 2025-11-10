package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateMemberRoleRequest(
    val userId: String,
    val role: String  // "admin" ou "member"
)