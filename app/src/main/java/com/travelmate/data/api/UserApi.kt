package com.travelmate.data.api

import com.travelmate.data.models.User
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    
    @GET("users/{id}")
    suspend fun getUserById(
        @Path("id") userId: String
    ): Response<User>
    
    @POST("users/batch")
    suspend fun getUsersByIds(
        @Body userIds: List<String>
    ): Response<List<User>>
}
