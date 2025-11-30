package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class PriceAlert(
    val id: String,
    val origin: String,           // Code aéroport origine (ex: "TUN")
    val destination: String,      // Code aéroport destination (ex: "CDG")
    val departureDate: String,   // Date de départ (format: "YYYY-MM-DD")
    val returnDate: String? = null, // Date de retour (format: "YYYY-MM-DD") si aller-retour
    val tripType: TripType = TripType.ALLER_SIMPLE, // Type de trajet
    val airline: String? = null, // Code compagnie aérienne (optionnel)
    val priceThreshold: Double,  // Prix seuil
    val currentPrice: Double? = null, // Prix actuel (mis à jour lors de la vérification)
    val status: AlertStatus = AlertStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val triggeredAt: Long? = null // Date de déclenchement si status = TRIGGERED
)

enum class AlertStatus {
    ACTIVE,      // Alerte active, en attente
    TRIGGERED    // Alerte déclenchée (prix <= seuil)
}

enum class TripType {
    ALLER_SIMPLE,
    ALLER_RETOUR
}

