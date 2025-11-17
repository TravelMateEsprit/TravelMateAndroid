package com.travelmate.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroupMember(
    @SerialName("_id")
    val id: String = "",

    @SerialName("groupId")
    val groupId: String = "",

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("role")
    val role: String = "member",

    @SerialName("status")
    val status: String = "active",

    @SerialName("joinedAt")
    val joinedAt: String = "",

    @SerialName("leftAt")
    val leftAt: String? = null,

    @SerialName("user")
    val user: UserInfo? = null
) {
    fun isAdmin() = role.equals("admin", ignoreCase = true)
    fun isActive() = status.equals("active", ignoreCase = true)
    fun isPending() = status.equals("pending", ignoreCase = true)
}

@Serializable
data class UserInfo(
    @SerialName("_id")
    val id: String = "",

    @SerialName("nom")
    val nom: String? = null,

    @SerialName("prenom")
    val prenom: String? = null,

    @SerialName("email")
    val email: String? = null
) {
    val name: String
        get() = "${prenom ?: ""} ${nom ?: ""}".trim().ifEmpty { "Utilisateur" }
}