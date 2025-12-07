package com.travelmate.data.models
import kotlinx.serialization.Serializable
@Serializable
data class PendingRequest(
    val _id: String,
    val id: String = _id,
    val groupId: String,
    val userId: PendingRequestUser,  // ✅ Modèle spécifique
    val status: String,
    val role: String,
    val joinedAt: String
)

// ✅ Modèle spécifique pour les demandes en attente
@Serializable
data class PendingRequestUser(
    val _id: String,
    val id: String = _id,
    val name: String? = null,
    val nom: String? = null,
    val prenom: String? = null,
    val email: String? = null,
    val photo: String? = null,
    val avatar: String? = null
)