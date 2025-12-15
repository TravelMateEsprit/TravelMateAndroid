package com.travelmate.data.repository

import android.util.Log
import com.travelmate.data.api.ReservationsApi
import com.travelmate.data.models.CreateOfferReservationRequest
import com.travelmate.data.models.Reservation
import com.travelmate.data.models.ReservationDto
import com.travelmate.data.models.ReservationStatus
import com.travelmate.data.models.toReservation
import com.travelmate.utils.UserPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReservationsRepository @Inject constructor(
    private val api: ReservationsApi,
    private val userPreferences: UserPreferences
) {
    private val tag = "ReservationsRepository"

    private fun authHeader(): String? {
        val token = userPreferences.getAccessToken()?.removePrefix("Bearer ")?.trim()
        return token?.takeIf { it.isNotEmpty() }?.let { "Bearer $it" }
    }

    suspend fun getAgencyReservations(): List<Reservation> {
        val header = authHeader() ?: return emptyList()
        return runCatching {
            api.getAllReservations(header).mapNotNull(ReservationDto::toReservation)
        }.getOrElse {
            Log.e(tag, "Failed to load agency reservations", it)
            emptyList()
        }
    }

    suspend fun getUserReservations(): List<Reservation> {
        val header = authHeader() ?: return emptyList()
        return runCatching {
            api.getMyReservations(header).mapNotNull(ReservationDto::toReservation)
        }.getOrElse {
            Log.e(tag, "Failed to load user reservations", it)
            emptyList()
        }
    }

    suspend fun createReservationForOffer(packId: String, price: Double, notes: String?, people: Int): Result<Reservation> {
        val header = authHeader() ?: return Result.failure(Exception("Non authentifié"))
        val request = CreateOfferReservationRequest(
            offerId = packId,
            price = price,
            notes = notes,
            numberOfPeople = people
        )
        return runCatching {
            api.createOfferReservation(header, request).toReservation()
        }.onFailure {
            Log.e(tag, "Failed to create reservation", it)
        }
    }

    suspend fun acceptReservation(reservationId: String): Result<Reservation> = updateStatus(reservationId, ReservationStatus.ACCEPTED)
    suspend fun rejectReservation(reservationId: String): Result<Reservation> = updateStatus(reservationId, ReservationStatus.REJECTED)

    suspend fun cancelReservation(reservationId: String): Result<Reservation> {
        val header = authHeader() ?: return Result.failure(Exception("Non authentifié"))
        return runCatching {
            api.cancelReservation(reservationId, header).toReservation()
        }.onFailure {
            Log.e(tag, "Failed to cancel reservation", it)
        }
    }

    private suspend fun updateStatus(reservationId: String, status: ReservationStatus): Result<Reservation> {
        val header = authHeader() ?: return Result.failure(Exception("Non authentifié"))
        val backendStatus = when (status) {
            ReservationStatus.PENDING -> "en_attente"
            ReservationStatus.ACCEPTED -> "confirmée"
            ReservationStatus.REJECTED -> "annulée"
            ReservationStatus.CANCELLED -> "terminée"
        }
        return runCatching {
            api.updateReservationStatus(reservationId, backendStatus, header).toReservation()
        }.onFailure {
            Log.e(tag, "Failed to update status", it)
        }
    }
}

