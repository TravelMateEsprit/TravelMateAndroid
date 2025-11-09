package com.travelmate.data.api

import com.travelmate.data.models.CreateVoyageRequest
import com.travelmate.data.models.Voyage
import retrofit2.Response
import retrofit2.http.*

interface VoyageApi {
    
    @GET("voyages")
    suspend fun getAllVoyages(
        @Header("Authorization") token: String
    ): Response<List<Voyage>>
    
    @GET("voyages/{id}")
    suspend fun getVoyageById(
        @Path("id") voyageId: String,
        @Header("Authorization") token: String
    ): Response<Voyage>
    
    @POST("voyages")
    suspend fun createVoyage(
        @Header("Authorization") token: String,
        @Body request: CreateVoyageRequest
    ): Response<Voyage>
    
    @PUT("voyages/{id}")
    suspend fun updateVoyage(
        @Path("id") voyageId: String,
        @Header("Authorization") token: String,
        @Body request: CreateVoyageRequest
    ): Response<Voyage>
    
    @DELETE("voyages/{id}")
    suspend fun deleteVoyage(
        @Path("id") voyageId: String,
        @Header("Authorization") token: String
    ): Response<Unit>
}

