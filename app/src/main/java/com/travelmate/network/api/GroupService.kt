package com.travelmate.network.api

import com.travelmate.data.models.Group
import retrofit2.Response
import retrofit2.http.*

interface GroupService {
    @GET("groups")
    suspend fun getAllGroups(@Header("Authorization") token: String): Response<List<Group>>

    @POST("groups")
    suspend fun createGroup(
        @Header("Authorization") token: String,
        @Body group: Map<String, String>
    ): Response<Group>

    @POST("groups/{groupId}/join")
    suspend fun joinGroup(
        @Path("groupId") groupId: String,
        @Header("Authorization") token: String
    ): Response<Group>

    @POST("groups/{groupId}/leave")
    suspend fun leaveGroup(
        @Path("groupId") groupId: String,
        @Header("Authorization") token: String
    ): Response<Group>
}