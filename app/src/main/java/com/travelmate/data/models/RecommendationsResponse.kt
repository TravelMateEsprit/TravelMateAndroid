package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class RecommendedOffer(
    val offerId: String,
    val score: Int? = null,
    val reason: String? = null // Gemini's explanation
) {
    // This will be populated after parsing by looking up the offer from the offers list
    var flightOffer: FlightOffer? = null
    
    fun getOffer(): FlightOffer {
        return flightOffer ?: FlightOffer()
    }
    
    fun getExplanation(): String {
        return reason ?: "Recommandé par notre IA"
    }
    
    fun getScore(): Int {
        return score ?: 0
    }
}

@Serializable
data class RecommendationsResponse(
    val recommendations: List<RecommendedOffer>
)

