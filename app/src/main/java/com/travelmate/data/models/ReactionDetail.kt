package com.travelmate.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReactionDetail(
    @SerialName("userId")
    val userId: String = "",

    @SerialName("emoji")
    val emoji: String = "",

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
        get() = "$prenom $nom".trim().ifEmpty { "Utilisateur" }
}
