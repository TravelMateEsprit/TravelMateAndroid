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
    val userId: UserInfo? = null,

    @SerialName("role")
    val role: String = "member",

    @SerialName("status")
    val status: String = "active",

    @SerialName("joinedAt")
    val joinedAt: String = "",

    @SerialName("leftAt")
    val leftAt: String? = null,

    @SerialName("user")
    val user: UserInfo? = null,

    @SerialName("nom")
    val nom: String = "",

    @SerialName("prenom")
    val prenom: String = "",

    @SerialName("avatar")
    val avatar: String = "",

    @SerialName("email")
    val email: String = "",

    @SerialName("isCreator")
    val isCreator: Boolean = false
) {
    // Helper properties to get user info from either userId object or flat fields
    val userNom: String
        get() = userId?.nom ?: nom

    val userPrenom: String
        get() = userId?.prenom ?: prenom

    val userEmail: String
        get() = userId?.email ?: email

    val userAvatar: String
        get() = userId?.avatar ?: avatar

    val userName: String
        get() = userId?.name ?: "$prenom $nom".trim().ifEmpty { "Utilisateur" }

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
    val email: String? = null,

    @SerialName("avatar")
    val avatar: String? = null
) {
    val name: String
        get() = "${prenom ?: ""} ${nom ?: ""}".trim().ifEmpty { "Utilisateur" }
}