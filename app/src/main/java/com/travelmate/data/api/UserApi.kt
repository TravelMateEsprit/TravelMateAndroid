package com.travelmate.data.api

import com.travelmate.data.models.User
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    
    @GET("api/users/{id}")
    suspend fun getUserById(
        @Path("id") userId: String,
        @Header("Authorization") token: String
    ): Response<User>
    
    @POST("api/users/batch")
    suspend fun getUsersByIds(
        @Header("Authorization") token: String,
        @Body userIds: List<String>
    ): Response<List<User>>
}
