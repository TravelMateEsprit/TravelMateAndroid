package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateVoyageRequest(
    val destination: String,
    val date_depart: String, // Format: "YYYY-MM-DD HH:mm"
    val date_retour: String, // Format: "YYYY-MM-DD HH:mm"
    val type: String,
    val description: String? = null,
    val prix_estime: Double? = null,
    val nombre_places: Int? = null,
    val imageUrl: String? = null
)

