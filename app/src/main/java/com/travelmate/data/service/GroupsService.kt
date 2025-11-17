package com.travelmate.data.service

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import com.travelmate.data.api.GroupsApi
import com.travelmate.data.models.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupsService @Inject constructor(
    private val groupsApi: GroupsApi,
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("travelmate_prefs", Context.MODE_PRIVATE)

    private val _allGroups = MutableStateFlow<List<Group>>(emptyList())
    val allGroups: StateFlow<List<Group>> = _allGroups.asStateFlow()

    private val _myGroups = MutableStateFlow<List<Group>>(emptyList())
    val myGroups: StateFlow<List<Group>> = _myGroups.asStateFlow()

    private val _myCreatedGroups = MutableStateFlow<List<Group>>(emptyList())
    val myCreatedGroups: StateFlow<List<Group>> = _myCreatedGroups.asStateFlow()

    private val _currentGroup = MutableStateFlow<Group?>(null)
    val currentGroup: StateFlow<Group?> = _currentGroup.asStateFlow()

    private val _groupMessages = MutableStateFlow<List<MessageGroupe>>(emptyList())
    val groupMessages: StateFlow<List<MessageGroupe>> = _groupMessages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ✅ AJOUT : État pour les demandes en attente
    private val _pendingRequests = MutableStateFlow<List<GroupMember>>(emptyList())
    val pendingRequests: StateFlow<List<GroupMember>> = _pendingRequests.asStateFlow()

    private val _joinedGroupIds = MutableStateFlow<Set<String>>(emptySet())

    private fun getAuthToken(): String {
        val token = prefs.getString("access_token", "") ?: ""
        return "Bearer $token"
    }

    private fun getUserId(): String {
        return prefs.getString("user_id", "") ?: ""
    }

    private fun isCreator(group: Group, userId: String): Boolean {
        if (group.createdBy == null) return false
        val createdByStr = group.createdBy.toString().trim()
        val userIdStr = userId.trim()
        return createdByStr.equals(userIdStr, ignoreCase = true) ||
                createdByStr == userIdStr ||
                (createdByStr.length >= 20 && userIdStr.length >= 20 &&
                        createdByStr.lowercase() == userIdStr.lowercase())
    }

    // ========== GROUPS ==========

    suspend fun getAllGroups(): Result<List<Group>> {
        return try {
            _isLoading.value = true
            _error.value = null

            Log.d("GroupsService", "=== GET ALL GROUPS ===")
            val response = groupsApi.getAllGroups(getAuthToken())

            if (response.isSuccessful && response.body() != null) {
                val groups = response.body()!!
                val userId = getUserId()

                val groupsWithStatus = groups.map { group ->
                    val isInCache = _joinedGroupIds.value.contains(group._id)
                    val isCreator = isCreator(group, userId)
                    val isMemberInArray = group.members.any { memberId ->
                        val memberIdStr = memberId.toString().trim()
                        val userIdStr = userId.trim()
                        memberIdStr.equals(userIdStr, ignoreCase = true) || memberIdStr == userIdStr
                    }

                    val isMember = isInCache || isMemberInArray || isCreator

                    // ✅ CORRECTION : Assigner les propriétés avec apply
                    group.apply {
                        memberCount = members.size
                        isUserMember = isMember
                    }
                }

                _allGroups.value = groupsWithStatus
                _myGroups.value = groupsWithStatus.filter { it.isUserMember && !isCreator(it, userId) }
                _myCreatedGroups.value = groupsWithStatus.filter { isCreator(it, userId) }

                Log.d("GroupsService", "✅ Loaded ${groupsWithStatus.size} groups")
                Result.success(groupsWithStatus)
            } else {
                val errorMsg = "Erreur lors du chargement des groupes: ${response.code()}"
                Log.e("GroupsService", errorMsg)
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("GroupsService", "❌ Error loading groups", e)
            _error.value = e.message
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun getGroupById(groupId: String): Result<Group> {
        return try {
            _isLoading.value = true
            _error.value = null

            val response = groupsApi.getGroupById(groupId, getAuthToken())

            if (response.isSuccessful && response.body() != null) {
                val group = response.body()!!
                _currentGroup.value = group
                Result.success(group)
            } else {
                val errorMsg = "Erreur lors du chargement du groupe: ${response.code()}"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("GroupsService", "❌ Error loading group", e)
            _error.value = e.message
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun createGroup(request: Map<String, String>): Result<Group> {
        return try {
            _isLoading.value = true
            _error.value = null

            val createRequest = CreateGroupRequest(
                name = request["name"]?.trim() ?: "",
                description = request["description"]?.trim() ?: "",
                destination = request["destination"]?.trim(),
                image = request["image"]?.takeIf { it.isNotBlank() }
            )

            val response = groupsApi.createGroup(getAuthToken(), createRequest)

            if (response.isSuccessful && response.body() != null) {
                val group = response.body()!!
                _joinedGroupIds.value = _joinedGroupIds.value + group._id
                getAllGroups()
                Result.success(group)
            } else {
                val errorMsg = "Erreur: ${response.code()}"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("GroupsService", "❌ Error creating group", e)
            _error.value = e.message
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun updateGroup(groupId: String, name: String?, destination: String?, description: String?, imageUrl: String?): Result<Group> {
        return try {
            _isLoading.value = true
            _error.value = null

            val updateRequest = UpdateGroupRequest(
                name = name,
                description = description,
                image = imageUrl
            )

            val response = groupsApi.updateGroup(groupId, getAuthToken(), updateRequest)

            if (response.isSuccessful && response.body() != null) {
                getAllGroups()
                Result.success(response.body()!!)
            } else {
                val errorMsg = "Erreur: ${response.code()}"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("GroupsService", "❌ Error updating group", e)
            _error.value = e.message
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun deleteGroup(groupId: String): Result<Unit> {
        return try {
            _isLoading.value = true
            _error.value = null

            val response = groupsApi.deleteGroup(groupId, getAuthToken())

            if (response.isSuccessful) {
                _joinedGroupIds.value = _joinedGroupIds.value - groupId
                getAllGroups()
                Result.success(Unit)
            } else {
                val errorMsg = "Erreur: ${response.code()}"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("GroupsService", "❌ Error deleting group", e)
            _error.value = e.message
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun joinGroup(groupId: String): Result<GroupMember> {
        return try {
            _isLoading.value = true
            _error.value = null

            val response = groupsApi.joinGroup(groupId, getAuthToken())

            if (response.isSuccessful && response.body() != null) {
                val member = response.body()!!

                // ✅ Afficher un message si la demande est en attente
                if (member.status == "pending") {
                    _error.value = "Demande envoyée ! En attente d'approbation."
                } else if (member.status == "active") {
                    _joinedGroupIds.value = _joinedGroupIds.value + groupId
                }

                kotlinx.coroutines.delay(500)
                getAllGroups()
                Result.success(member)
            } else {
                // ✅ Gérer les erreurs 400
                val errorBody = response.errorBody()?.string() ?: ""
                val errorMsg = when {
                    errorBody.contains("en attente") -> "Demande déjà envoyée, en attente d'approbation"
                    errorBody.contains("déjà membre") -> "Vous êtes déjà membre de ce groupe"
                    errorBody.contains("banni") -> "Vous avez été banni de ce groupe"
                    else -> "Erreur: ${response.code()}"
                }
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("GroupsService", "❌ Error joining group", e)
            _error.value = e.message
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun leaveGroup(groupId: String): Result<Unit> {
        return try {
            _isLoading.value = true
            _error.value = null

            val response = groupsApi.leaveGroup(groupId, getAuthToken())

            if (response.isSuccessful) {
                _joinedGroupIds.value = _joinedGroupIds.value - groupId
                getAllGroups()
                Result.success(Unit)
            } else {
                val errorMsg = "Erreur: ${response.code()}"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("GroupsService", "❌ Error leaving group", e)
            _error.value = e.message
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun getGroupMembers(groupId: String): Result<List<GroupMember>> {
        return try {
            val response = groupsApi.getGroupMembers(groupId, getAuthToken())

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erreur"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ NOUVEAU : Fonctions pour gérer les demandes
    suspend fun getPendingRequests(groupId: String): Result<List<GroupMember>> {
        return try {
            val response = groupsApi.getPendingRequests(groupId, getAuthToken())
            if (response.isSuccessful && response.body() != null) {
                _pendingRequests.value = response.body()!!
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erreur"))
            }
        } catch (e: Exception) {
            Log.e("GroupsService", "❌ Error getting pending requests", e)
            Result.failure(e)
        }
    }

    suspend fun approveMember(groupId: String, userId: String): Result<GroupMember> {
        return try {
            _isLoading.value = true
            val response = groupsApi.approveMember(groupId, userId, getAuthToken())

            if (response.isSuccessful && response.body() != null) {
                getPendingRequests(groupId)
                getAllGroups()
                Result.success(response.body()!!)
            } else {
                val errorMsg = "Erreur: ${response.code()}"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("GroupsService", "❌ Error approving member", e)
            _error.value = e.message
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun rejectMember(groupId: String, userId: String): Result<Unit> {
        return try {
            _isLoading.value = true
            val response = groupsApi.rejectMember(groupId, userId, getAuthToken())

            if (response.isSuccessful) {
                getPendingRequests(groupId)
                Result.success(Unit)
            } else {
                val errorMsg = "Erreur: ${response.code()}"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("GroupsService", "❌ Error rejecting member", e)
            _error.value = e.message
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    // ========== MESSAGES ==========

    suspend fun getGroupMessages(groupId: String): Result<List<MessageGroupe>> {
        return try {
            _isLoading.value = true
            _error.value = null

            val response = groupsApi.getGroupMessages(groupId, getAuthToken())

            if (response.isSuccessful && response.body() != null) {
                _groupMessages.value = response.body()!!
                Result.success(response.body()!!)
            } else {
                val errorMsg = "Erreur: ${response.code()}"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("GroupsService", "❌ Error loading messages", e)
            _error.value = e.message
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun createMessage(groupId: String, content: String, images: List<String> = emptyList()): Result<MessageGroupe> {
        return try {
            _isLoading.value = true
            _error.value = null

            val request = CreateMessageRequest(
                content = content.trim(),
                images = images
            )

            val response = groupsApi.createMessage(groupId, getAuthToken(), request)

            if (response.isSuccessful && response.body() != null) {
                getGroupMessages(groupId)
                Result.success(response.body()!!)
            } else {
                val errorMsg = "Erreur: ${response.code()}"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("GroupsService", "❌ Error creating message", e)
            _error.value = e.message
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun deleteMessage(groupId: String, messageId: String): Result<Unit> {
        return try {
            _isLoading.value = true
            _error.value = null

            val response = groupsApi.deleteMessage(groupId, messageId, getAuthToken())

            if (response.isSuccessful) {
                getGroupMessages(groupId)
                Result.success(Unit)
            } else {
                val errorMsg = "Erreur: ${response.code()}"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("GroupsService", "❌ Error deleting message", e)
            _error.value = e.message
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun updateMessage(groupId: String, messageId: String, newContent: String): Result<MessageGroupe> {
        return try {
            _isLoading.value = true
            _error.value = null

            val request = UpdateMessageRequest(content = newContent)
            val response = groupsApi.updateMessage(groupId, messageId, getAuthToken(), request)

            if (response.isSuccessful && response.body() != null) {
                getGroupMessages(groupId)
                Result.success(response.body()!!)
            } else {
                val errorMsg = "Erreur: ${response.code()}"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("GroupsService", "❌ Error updating message", e)
            _error.value = e.message
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun uploadGroupImage(imageUri: Uri): Result<String> {
        return try {
            _isLoading.value = true
            _error.value = null

            val file = File(context.cacheDir, "group_image_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(imageUri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

            val response = groupsApi.uploadGroupImage(body)

            if (response.isSuccessful && response.body() != null) {
                val imageUrl = response.body()!!.imageUrl
                file.delete()
                Result.success(imageUrl)
            } else {
                val errorMsg = "Erreur upload: ${response.code()}"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("GroupsService", "❌ Error uploading image", e)
            _error.value = e.message
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}