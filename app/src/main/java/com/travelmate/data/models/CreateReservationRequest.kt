package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateReservationRequest(
    val id_voyage: String,
    val prix: Double,
    val notes: String? = null,
    val nombre_personnes: Int
    // id_utilisateur is retrieved from the authentication token by the backend
)

