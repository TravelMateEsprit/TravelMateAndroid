package com.travelmate.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Reservation(
    @SerialName("_id")
    val id_reservation: String,
    val id_voyage: String,
    val id_utilisateur: String,
    val statut: String, // e.g., "en_attente", "confirmee", "annulee"
    val prix: Double,
    val date_reservation: String, // Format: "YYYY-MM-DD HH:mm"
    val createdAt: String? = null,
    val updatedAt: String? = null
)

