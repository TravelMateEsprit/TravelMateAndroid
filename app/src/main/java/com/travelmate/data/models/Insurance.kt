package com.travelmate.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Insurance(
    val _id: String,
    val agencyId: JsonElement, // Peut être String ou Object
    val name: String,
    val description: String,
    val price: Double,
    val duration: String, // Format: "1 mois", "1 an", "voyage unique"
    val coverage: List<String> = emptyList(),
    val imageUrl: String? = null,
    val conditions: InsuranceConditions? = null,
    val subscribers: List<String> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    
    // Champs calculés/UI
    var agencyName: String? = null,
    var rating: Double = 4.7,
    var isSubscribed: Boolean = false
) {
    fun getAgencyIdString(): String {
        return try {
            if (agencyId.toString().startsWith("\"")) {
                agencyId.toString().trim('"')
            } else {
                // Si c'est un objet, extraire l'_id
                agencyId.toString()
            }
        } catch (e: Exception) {
            ""
        }
    }
}
