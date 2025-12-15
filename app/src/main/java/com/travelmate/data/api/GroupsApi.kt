package com.travelmate.data.api

import com.travelmate.data.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface GroupsApi {

    // ========== UPLOAD IMAGE (SANS AUTH) ==========
    @Multipart
    @POST("groups/upload-image")
    suspend fun uploadGroupImage(
        @Part image: MultipartBody.Part
    ): Response<UploadImageResponse>

    // ========== GROUPS ==========

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

    // ========== MEMBERSHIP ==========

    @POST("groups/{id}/join")
    suspend fun joinGroup(
        @Path("id") groupId: String,
        @Header("Authorization") token: String
    ): Response<GroupMember>

    @POST("groups/{id}/leave")  // ✅ CORRECTION : id au lieu de groupId
    suspend fun leaveGroup(
        @Path("id") groupId: String,
        @Header("Authorization") token: String
    ): Response<MessageResponse>

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

    // ✅ NOUVEAU : Endpoints pour gérer les demandes
    @GET("groups/{id}/pending-requests")
    suspend fun getPendingRequests(
        @Path("id") groupId: String,
        @Header("Authorization") token: String
    ): Response<List<PendingRequest>>

    @POST("groups/{id}/members/{userId}/approve")
    suspend fun approveMember(
        @Path("id") groupId: String,
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<Unit>

    @POST("groups/{id}/members/{userId}/reject")
    suspend fun rejectMember(
        @Path("id") groupId: String,
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<Unit>

    // ✅ NEW: Remove or ban member from group
    @DELETE("groups/{id}/members/{userId}")
    suspend fun removeMember(
        @Path("id") groupId: String,
        @Path("userId") userId: String,
        @Header("Authorization") token: String,
        @Query("action") action: String
    ): Response<RemoveMemberResponse>

    // ========== MESSAGES ==========

    @POST("groups/{id}/messages")
    suspend fun createMessage(
        @Path("id") groupId: String,
        @Header("Authorization") token: String,
        @Body request: CreateMessageRequest
    ): Response<MessageGroupe>

    @GET("groups/{id}/messages")
    suspend fun getGroupMessages(
        @Path("id") groupId: String,
        @Header("Authorization") token: String
    ): Response<List<MessageGroupe>>

    @PUT("groups/{id}/messages/{messageId}")
    suspend fun updateMessage(
        @Path("id") groupId: String,
        @Path("messageId") messageId: String,
        @Header("Authorization") token: String,
        @Body request: UpdateMessageRequest
    ): Response<MessageGroupe>

    @DELETE("groups/{id}/messages/{messageId}")
    suspend fun deleteMessage(
        @Path("id") groupId: String,
        @Path("messageId") messageId: String,
        @Header("Authorization") token: String
    ): Response<DeleteMessageResponse>

    @PUT("groups/{id}/messages/{messageId}/moderate")
    suspend fun moderateMessage(
        @Path("id") groupId: String,
        @Path("messageId") messageId: String,
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<MessageGroupe>


    // ✅ Upload image de message
    @Multipart
    @POST("groups/upload-message-image")
    suspend fun uploadMessageImage(
        @Part image: MultipartBody.Part
    ): Response<UploadImageResponse>

    // ✅ Réagir à un message
    @POST("groups/{id}/messages/{messageId}/react")
    suspend fun reactToMessage(
        @Path("id") groupId: String,
        @Path("messageId") messageId: String,
        @Header("Authorization") token: String,
        @Body emoji: Map<String, String>
    ): Response<MessageGroupe>


}

@kotlinx.serialization.Serializable
data class UploadImageResponse(
    val imageUrl: String
)