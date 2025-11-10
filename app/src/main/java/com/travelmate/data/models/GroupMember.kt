package com.travelmate.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroupMember(
    val _id: String? = null,
    val groupId: String? = null,
    val userId: String? = null,
    val role: String = "member",  // String au lieu d'enum
    val status: String = "active",  // String au lieu d'enum
    val joinedAt: String? = null,
    val leftAt: String? = null,

    @SerialName("__v")
    val version: Int? = null,

    // Populated user data
    var user: User? = null
) {
    fun isAdmin() = role.equals("admin", ignoreCase = true)
    fun isActive() = status.equals("active", ignoreCase = true)
}