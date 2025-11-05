package com.travelmate.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class AgencyRegistrationRequest(
    val email: String,
    val password: String,
    val agencyName: String,
    val siret: String,
    val address: String,
    val city: String,
    val postalCode: String,
    val country: String,
    val phone: String,
    val websiteUrl: String? = null,
    val description: String? = null,
    val kbisDocument: String? = null, // Base64 encoded
    val legalRepresentativeFirstName: String,
    val legalRepresentativeLastName: String
) {
    fun toJson(): String {
        // Créer un objet avec le format attendu par le backend
        val backendFormat = mapOf(
            "name" to "$legalRepresentativeFirstName $legalRepresentativeLastName",
            "email" to email,
            "password" to password,
            "agencyName" to agencyName
        )
        return Json.encodeToString(backendFormat)
    }
}
