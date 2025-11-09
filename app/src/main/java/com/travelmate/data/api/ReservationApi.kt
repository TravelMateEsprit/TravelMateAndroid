package com.travelmate.data.api

import com.travelmate.data.models.CreateReservationRequest
import com.travelmate.data.models.Reservation
import retrofit2.Response
import retrofit2.http.*

interface ReservationApi {
    
    @GET("reservations")
    suspend fun getAllReservations(
        @Header("Authorization") token: String
    ): Response<List<Reservation>>
    
    @GET("reservations/my-reservations")
    suspend fun getMyReservations(
        @Header("Authorization") token: String
    ): Response<List<Reservation>>
    
    @GET("reservations/{id}")
    suspend fun getReservationById(
        @Path("id") reservationId: String,
        @Header("Authorization") token: String
    ): Response<Reservation>
    
    @POST("reservations")
    suspend fun createReservation(
        @Header("Authorization") token: String,
        @Body request: CreateReservationRequest
    ): Response<Reservation>
    
    @PATCH("reservations/{id}/status")
    suspend fun updateReservationStatus(
        @Path("id") reservationId: String,
        @Header("Authorization") token: String,
        @Body status: Map<String, String>
    ): Response<Reservation>
    
    @DELETE("reservations/{id}")
    suspend fun deleteReservation(
        @Path("id") reservationId: String,
        @Header("Authorization") token: String
    ): Response<Unit>
}

