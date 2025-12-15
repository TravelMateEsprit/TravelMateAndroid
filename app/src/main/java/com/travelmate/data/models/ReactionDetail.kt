package com.travelmate.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReactionDetail(
    @SerialName("userId")
    val userId: String = "",

    @SerialName("emoji")
    val emoji: String = "",

    @SerialName("name")
    val name: String? = null,

    @SerialName("nom")
    val nom: String = "",

    @SerialName("prenom")
    val prenom: String = "",

    @SerialName("avatar")
    val avatar: String = "",

    @SerialName("reactedAt")
    val reactedAt: String = ""
) {
    val fullName: String
        get() = name?.takeIf { it.isNotBlank() }
            ?: "$prenom $nom".trim().takeIf { it.isNotBlank() }
            ?: "Utilisateur"
}
