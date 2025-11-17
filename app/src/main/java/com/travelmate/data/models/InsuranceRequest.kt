package com.travelmate.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class RequestStatus {
    @SerialName("EN_ATTENTE")
    PENDING,
    
    @SerialName("APPROUVEE")
    APPROVED,
    
    @SerialName("REJETEE")
    REJECTED,
    
    @SerialName("ANNULEE")
    CANCELLED
}

@Serializable
data class InsuranceRequest(
    @SerialName("_id")
    val id: String,
    
    @SerialName("userId")
    val userId: String,
    
    @SerialName("insuranceId")
    val insuranceId: String,
    
    @SerialName("agencyId")
    val agencyId: String,
    
    @SerialName("status")
    val status: RequestStatus,
    
    // Informations du voyageur
    @SerialName("travelerName")
    val travelerName: String,
    
    @SerialName("travelerEmail")
    val travelerEmail: String,
    
    @SerialName("travelerPhone")
    val travelerPhone: String,
    
    @SerialName("dateOfBirth")
    val dateOfBirth: String,
    
    @SerialName("passportNumber")
    val passportNumber: String,
    
    @SerialName("nationality")
    val nationality: String,
    
    // Détails du voyage
    @SerialName("destination")
    val destination: String,
    
    @SerialName("departureDate")
    val departureDate: String,
    
    @SerialName("returnDate")
    val returnDate: String,
    
    @SerialName("travelPurpose")
    val travelPurpose: String? = null,
    
    // Documents et messages
    @SerialName("documents")
    val documents: List<String>? = null,
    
    @SerialName("message")
    val message: String? = null,
    
    // Réponse de l'agence
    @SerialName("agencyResponse")
    val agencyResponse: String? = null,
    
    @SerialName("reviewedBy")
    val reviewedBy: String? = null,
    
    @SerialName("reviewedAt")
    val reviewedAt: String? = null,
    
    @SerialName("isRead")
    val isRead: Boolean = false,
    
    // Informations de paiement
    @SerialName("paymentIntentId")
    val paymentIntentId: String? = null,
    
    @SerialName("paymentStatus")
    val paymentStatus: String? = null, // pending, succeeded, failed, refunded
    
    @SerialName("paymentAmount")
    val paymentAmount: Double? = null,
    
    @SerialName("paymentDate")
    val paymentDate: String? = null,
    
    @SerialName("createdAt")
    val createdAt: String,
    
    @SerialName("updatedAt")
    val updatedAt: String? = null
)

// DTOs pour les requêtes
@Serializable
data class CreateInsuranceRequestRequest(
    @SerialName("insuranceId")
    val insuranceId: String,
    
    @SerialName("travelerName")
    val travelerName: String,
    
    @SerialName("travelerEmail")
    val travelerEmail: String,
    
    @SerialName("travelerPhone")
    val travelerPhone: String,
    
    @SerialName("dateOfBirth")
    val dateOfBirth: String,
    
    @SerialName("passportNumber")
    val passportNumber: String,
    
    @SerialName("nationality")
    val nationality: String,
    
    @SerialName("destination")
    val destination: String,
    
    @SerialName("departureDate")
    val departureDate: String,
    
    @SerialName("returnDate")
    val returnDate: String,
    
    @SerialName("travelPurpose")
    val travelPurpose: String? = null,
    
    @SerialName("message")
    val message: String? = null
)

@Serializable
data class ReviewInsuranceRequestRequest(
    @SerialName("status")
    val status: RequestStatus,
    
    @SerialName("agencyResponse")
    val agencyResponse: String
)

@Serializable
data class InsuranceRequestStats(
    @SerialName("stats")
    val stats: Map<String, Int>,
    
    @SerialName("unreadCount")
    val unreadCount: Int,
    
    @SerialName("pendingCount")
    val pendingCount: Int
) {
    val total: Int
        get() = stats.values.sum()
    
    val pending: Int
        get() = stats["EN_ATTENTE"] ?: 0
    
    val approved: Int
        get() = stats["APPROUVEE"] ?: 0
    
    val rejected: Int
        get() = stats["REJETEE"] ?: 0
}

// DTOs pour le paiement
@Serializable
data class CreatePaymentResponse(
    @SerialName("clientSecret")
    val clientSecret: String,
    
    @SerialName("paymentIntentId")
    val paymentIntentId: String,
    
    @SerialName("amount")
    val amount: Double,
    
    @SerialName("currency")
    val currency: String? = null,
    
    @SerialName("insuranceName")
    val insuranceName: String? = null
)

@Serializable
data class ConfirmPaymentRequest(
    @SerialName("paymentIntentId")
    val paymentIntentId: String
)
