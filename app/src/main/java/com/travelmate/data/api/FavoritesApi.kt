package com.travelmate.data.api

import com.travelmate.data.models.FavoriteCheckResponse
import com.travelmate.data.models.FavoriteDto
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface FavoritesApi {
    @GET("favorites/my-favorites")
    suspend fun getMyFavorites(
        @Header("Authorization") token: String
    ): List<FavoriteDto>

    @GET("favorites/offers/{offerId}/check")
    suspend fun isFavorite(
        @Path("offerId") offerId: String,
        @Header("Authorization") token: String
    ): FavoriteCheckResponse

    @POST("favorites/offers/{offerId}")
    suspend fun addToFavorites(
        @Path("offerId") offerId: String,
        @Header("Authorization") token: String
    )

    @DELETE("favorites/offers/{offerId}")
    suspend fun removeFromFavorites(
        @Path("offerId") offerId: String,
        @Header("Authorization") token: String
    )
}

