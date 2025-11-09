package com.travelmate.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class Reservation(
    @SerialName("_id")
    val id_reservation: String,
    val id_voyage: JsonElement, // Backend returns either String or Voyage object
    val id_utilisateur: String,
    val statut: String, // e.g., "en_attente", "confirmee", "annulee"
    val prix: Double,
    val date_reservation: String, // Format: "YYYY-MM-DD HH:mm"
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    // Helper to extract voyage ID whether it's a string or object
    fun getVoyageId(): String {
        return try {
            when {
                id_voyage is kotlinx.serialization.json.JsonPrimitive -> {
                    // It's a string
                    id_voyage.content
                }
                id_voyage is kotlinx.serialization.json.JsonObject -> {
                    // It's an object, extract _id
                    id_voyage["_id"]?.jsonPrimitive?.content 
                        ?: id_voyage["id_voyage"]?.jsonPrimitive?.content
                        ?: ""
                }
                else -> {
                    // Fallback: try to parse as string
                    id_voyage.toString().trim('"')
                }
            }
        } catch (e: Exception) {
            // If all else fails, return empty string
            ""
        }
    }
}

