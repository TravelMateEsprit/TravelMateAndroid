package com.travelmate.data.models

import android.net.Uri
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class Pack(
    @SerialName("id")
    val id: String,
    val titre: String = "",
    val description: String = "",
    val prix: Double = 0.0,
    @SerialName("date_debut")
    val dateDebutRaw: String? = null,
    @SerialName("date_fin")
    val dateFinRaw: String? = null,
    val actif: Boolean = true,
    val images: List<String> = emptyList(),
    val destination: String? = null,
    @SerialName("type_offre")
    val typeOffreRaw: String? = null,
    @SerialName("id_agence")
    val agenceInfo: AgencySummary? = null
) {
    val dateDebut: String
        get() = dateDebutRaw.orEmpty()

    val dateFin: String
        get() = dateFinRaw.orEmpty()

    val typeOffre: String?
        get() = typeOffreRaw

    val agenceId: String
        get() = agenceInfo?.id?.takeIf { it.isNotBlank() } ?: ""

    val priceAdult: Double
        get() = prix

    val priceChild: Double
        get() = priceAdult / 2

    val placesDisponibles: Int?
        get() = null

    val country: String?
        get() = null

    val region: String?
        get() = null

    val activities: List<String>
        get() = emptyList()

    val placesToVisit: List<String>
        get() = emptyList()

    val pensionType: String?
        get() = null

    val transportType: String?
        get() = null

    val hotelCategory: String?
        get() = null

    val adultPricePerNight: Double
        get() = prix
}

@Serializable
data class AgencySummary(
    @SerialName("id")
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val agencyName: String? = null,
    val agencyLicense: String? = null,
    val agencyWebsite: String? = null,
    val agencyDescription: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String? = null
)

@Serializable
data class CreatePackRequest(
    val titre: String,
    val description: String,
    val prix: Double,
    @SerialName("date_debut")
    val dateDebut: String,
    @SerialName("date_fin")
    val dateFin: String,
    val destination: String,
    @SerialName("type_offre")
    val typeOffre: String = "package_complet",
    val images: List<String> = emptyList()
)

@Serializable
data class UpdatePackRequest(
    val titre: String? = null,
    val description: String? = null,
    val prix: Double? = null,
    @SerialName("date_debut")
    val dateDebut: String? = null,
    @SerialName("date_fin")
    val dateFin: String? = null,
    val destination: String? = null,
    @SerialName("type_offre")
    val typeOffre: String? = null,
    val images: List<String>? = null,
    val actif: Boolean? = null
)

data class CreateOfferReservationDto(
    val id_offer: String,
    val prix: Double,
    val notes: String? = null,
    val nombre_personnes: Int = 1
)

data class PackFormData(
    val title: String,
    val destination: String,
    val description: String,
    val price: String,
    val startDate: String,
    val endDate: String,
    val images: List<Uri> = emptyList()
)
