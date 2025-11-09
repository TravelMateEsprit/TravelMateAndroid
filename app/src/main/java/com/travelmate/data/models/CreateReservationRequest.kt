package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateReservationRequest(
    val id_voyage: String,
    val prix: Double
)

