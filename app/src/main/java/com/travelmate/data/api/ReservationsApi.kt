package com.travelmate.data.api

import com.travelmate.data.models.CreateOfferReservationRequest
import com.travelmate.data.models.ReservationDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ReservationsApi {
    @GET("reservations/my-reservations")
    suspend fun getMyReservations(
        @Header("Authorization") token: String
    ): List<ReservationDto>

    @GET("reservations")
    suspend fun getAllReservations(
        @Header("Authorization") token: String
    ): List<ReservationDto>

    @POST("reservations/offers")
    suspend fun createOfferReservation(
        @Header("Authorization") token: String,
        @Body request: CreateOfferReservationRequest
    ): ReservationDto

    @PATCH("reservations/{id}/status")
    suspend fun updateReservationStatus(
        @Path("id") reservationId: String,
        @Query("statut") status: String,
        @Header("Authorization") token: String
    ): ReservationDto

    @POST("reservations/{id}/cancel")
    suspend fun cancelReservation(
        @Path("id") reservationId: String,
        @Header("Authorization") token: String
    ): ReservationDto
}

