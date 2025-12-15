package com.travelmate.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.format.DateTimeFormatter

data class Reservation(
    val id: String,
    val packId: String,
    val packName: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val status: String,
    val packDestination: String = "",
    val packPrice: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val agencyId: String = "",
    val paymentStatus: String? = null,
    val notes: String? = null,
    val numberOfPeople: Int = 1
)

@Serializable
data class CreateOfferReservationRequest(
    @SerialName("id_offer") val offerId: String,
    @SerialName("prix") val price: Double,
    val notes: String? = null,
    @SerialName("nombre_personnes") val numberOfPeople: Int = 1
)

@Serializable
data class ReservationDto(
    @SerialName("_id") val id: String,
    @SerialName("id_offer") val offer: ReservationOfferDto? = null,
    @SerialName("id_utilisateur") val user: ReservationUserDto? = null,
    @SerialName("statut") val status: String,
    @SerialName("prix") val price: Double? = null,
    val notes: String? = null,
    @SerialName("nombre_personnes") val numberOfPeople: Int? = null,
    @SerialName("paymentStatus") val paymentStatus: String? = null,
    val createdAt: String? = null
)

@Serializable
data class ReservationOfferDto(
    @SerialName("_id") val id: String = "",
    val titre: String = "",
    val destination: String = "",
    val prix: Double? = null,
    @SerialName("id_agence") val agency: ReservationAgencyDto? = null
)

@Serializable
data class ReservationAgencyDto(
    @SerialName("_id") val id: String = "",
    @SerialName("agencyName") val name: String? = null
)

@Serializable
data class ReservationUserDto(
    @SerialName("_id") val id: String = "",
    val name: String = "",
    val email: String = ""
)

@Serializable
data class FavoriteDto(
    @SerialName("_id") val id: String,
    @SerialName("id_offer") val offer: Pack? = null
)

@Serializable
data class FavoriteCheckResponse(
    @SerialName("isFavorite") val isFavorite: Boolean
)

fun ReservationDto.toReservation(): Reservation {
    val offerId = offer?.id ?: ""
    val agencyId = offer?.agency?.id ?: ""
    val uiStatus = backendStatusToUi(status)
    return Reservation(
        id = id,
        packId = offerId,
        packName = offer?.titre ?: "",
        userId = user?.id ?: "",
        userName = user?.name ?: "Utilisateur",
        userEmail = user?.email ?: "",
        status = uiStatus.name,
        packDestination = offer?.destination ?: "",
        packPrice = price ?: offer?.prix ?: 0.0,
        createdAt = createdAt.toEpochMillis(),
        agencyId = agencyId,
        paymentStatus = paymentStatus,
        notes = notes,
        numberOfPeople = numberOfPeople ?: 1
    )
}

private fun backendStatusToUi(backendValue: String?): ReservationStatus = when (backendValue?.lowercase()) {
    "en_attente" -> ReservationStatus.PENDING
    "confirmée" -> ReservationStatus.ACCEPTED
    "annulée" -> ReservationStatus.REJECTED
    "terminée" -> ReservationStatus.CANCELLED
    else -> ReservationStatus.PENDING
}

private fun String?.toEpochMillis(): Long {
    if (this.isNullOrBlank()) return System.currentTimeMillis()
    return runCatching {
        Instant.from(DateTimeFormatter.ISO_INSTANT.parse(this)).toEpochMilli()
    }.getOrDefault(System.currentTimeMillis())
}
