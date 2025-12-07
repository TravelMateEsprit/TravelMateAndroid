package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateMessageRequest(
    val content: String? = null,
    val status: String? = null
)
// ✅ SUPPRIMÉ le companion object qui causait l'erreur de sérialisation