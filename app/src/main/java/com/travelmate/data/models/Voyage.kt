package com.travelmate.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Voyage(
    @SerialName("_id")
    val id_voyage: String,
    val destination: String,
    val date_depart: String, // Format: "YYYY-MM-DD HH:mm"
    val date_retour: String, // Format: "YYYY-MM-DD HH:mm"
    val type: String, // e.g., "Vacances", "Affaires", "Aventure"
    val imageUrl: String? = null,
    @SerialName("prix_estime")
    val prix_estime: Double? = null,
    val description: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

