package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Claim(
    val _id: String,
    val ticketNumber: String,
    val userId: UserBasicInfo? = null,
    val insuranceRequestId: InsuranceRequestBasicInfo,
    val agencyId: UserBasicInfo,
    val subject: String,
    val description: String,
    val category: String,
    val status: String,
    val priority: String,
    val initialAttachments: List<String> = emptyList(),
    val messages: List<ClaimMessage> = emptyList(),
    val statusHistory: List<StatusHistoryItem> = emptyList(),
    val firstResponseAt: String? = null,
    val resolvedAt: String? = null,
    val agencyResponse: String? = null,
    val respondedBy: UserBasicInfo? = null,
    val respondedAt: String? = null,
    val unreadByUser: Int = 0,
    val unreadByAgency: Int = 0,
    val isReadByUser: Boolean = false,
    val isReadByAgency: Boolean = false,
    val responseCount: Int = 0,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ClaimMessage(
    val _id: String? = null,
    val sender: UserBasicInfo,
    val senderRole: String,
    val message: String,
    val attachments: List<String> = emptyList(),
    val createdAt: String
)

@Serializable
data class StatusHistoryItem(
    val status: String,
    val changedBy: UserBasicInfo,
    val comment: String? = null,
    val timestamp: String
)

@Serializable
data class UserBasicInfo(
    val _id: String,
    val name: String,
    val email: String
)

@Serializable
data class InsuranceRequestBasicInfo(
    val _id: String,
    val travelerName: String,
    val destination: String,
    val departureDate: String,
    val returnDate: String
)

@Serializable
data class CreateClaimRequest(
    val insuranceRequestId: String,
    val subject: String,
    val description: String,
    val category: String,
    val priority: String = "MOYENNE",
    val attachments: List<String> = emptyList()
)

@Serializable
data class AddMessageRequest(
    val message: String,
    val attachments: List<String> = emptyList()
)

@Serializable
data class UpdateClaimStatusRequest(
    val status: String,
    val comment: String? = null,
    val priority: String? = null
)

@Serializable
data class ClaimUnreadCountResponse(
    val count: Int
)

enum class ClaimStatus(val value: String, val displayName: String) {
    OPEN("OUVERT", "Ouvert"),
    IN_PROGRESS("EN_COURS", "En cours"),
    WAITING_USER("EN_ATTENTE_CLIENT", "En attente client"),
    RESOLVED("RESOLU", "Résolu"),
    CLOSED("FERME", "Fermé");

    companion object {
        fun fromValue(value: String): ClaimStatus? = values().find { it.value == value }
    }
}

enum class ClaimPriority(val value: String, val displayName: String) {
    LOW("BASSE", "Basse"),
    MEDIUM("MOYENNE", "Moyenne"),
    HIGH("HAUTE", "Haute"),
    URGENT("URGENTE", "Urgente");

    companion object {
        fun fromValue(value: String): ClaimPriority? = values().find { it.value == value }
    }
}

enum class ClaimCategory(val value: String, val displayName: String) {
    BOOKING("RESERVATION", "Réservation"),
    PAYMENT("PAIEMENT", "Paiement"),
    COVERAGE("COUVERTURE", "Couverture"),
    CLAIM_PROCESS("PROCEDURE_RECLAMATION", "Procédure réclamation"),
    REFUND("REMBOURSEMENT", "Remboursement"),
    TECHNICAL("TECHNIQUE", "Technique"),
    OTHER("AUTRE", "Autre");

    companion object {
        fun fromValue(value: String): ClaimCategory? = values().find { it.value == value }
    }
}
