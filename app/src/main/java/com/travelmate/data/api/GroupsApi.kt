package com.travelmate.data.api

import com.travelmate.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface GroupsApi {

    @POST("groups")
    suspend fun createGroup(
        @Header("Authorization") token: String,
        @Body request: CreateGroupRequest
    ): Response<Group>

    @GET("groups")
    suspend fun getAllGroups(
        @Header("Authorization") token: String
    ): Response<List<Group>>

    @GET("groups/{id}")
    suspend fun getGroupById(
        @Path("id") groupId: String,
        @Header("Authorization") token: String
    ): Response<Group>

    @PUT("groups/{id}")
    suspend fun updateGroup(
        @Path("id") groupId: String,
        @Header("Authorization") token: String,
        @Body request: UpdateGroupRequest
    ): Response<Group>

    @DELETE("groups/{id}")
    suspend fun deleteGroup(
        @Path("id") groupId: String,
        @Header("Authorization") token: String
    ): Response<DeleteGroupResponse>

    @POST("groups/{id}/join")
    suspend fun joinGroup(
        @Path("id") groupId: String,
        @Header("Authorization") token: String
    ): Response<GroupMember>

    @POST("groups/{id}/leave")
    suspend fun leaveGroup(
        @Path("id") groupId: String,
        @Header("Authorization") token: String
    ): Response<LeaveGroupResponse>

    @GET("groups/{id}/members")
    suspend fun getGroupMembers(
        @Path("id") groupId: String,
        @Header("Authorization") token: String
    ): Response<List<GroupMember>>

    @PUT("groups/{id}/members/role")
    suspend fun updateMemberRole(
        @Path("id") groupId: String,
        @Header("Authorization") token: String,
        @Body request: UpdateMemberRoleRequest
    ): Response<GroupMember>

    @POST("groups/{id}/members/{userId}/ban")
    suspend fun banMember(
        @Path("id") groupId: String,
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<GroupMember>
}