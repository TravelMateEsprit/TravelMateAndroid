package com.travelmate.data.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.travelmate.data.api.GroupsApi
import com.travelmate.data.models.*
import com.travelmate.network.api.GroupService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ✅ Stocker les groupes rejoints localement
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
        // Comparaison exacte (ObjectId sont case-sensitive mais on compare sans case pour sécurité)
        return createdByStr.equals(userIdStr, ignoreCase = true) ||
               createdByStr == userIdStr ||
               // Si les IDs sont de longueur similaire (ObjectId = 24 chars), vérifier contenu exact
               (createdByStr.length >= 20 && userIdStr.length >= 20 && 
                createdByStr.lowercase() == userIdStr.lowercase())
    }

    suspend fun getAllGroups(): Result<List<Group>> {
        return try {
            _isLoading.value = true
            _error.value = null

            Log.d("GroupsService", "=== GET ALL GROUPS ===")
            val response = groupsApi.getAllGroups(getAuthToken())

            if (response.isSuccessful && response.body() != null) {
                val groups = response.body()!!
                val userId = getUserId()

                Log.d("GroupsService", "Current user ID: $userId")
                Log.d("GroupsService", "User ID length: ${userId.length}")
                Log.d("GroupsService", "Loaded ${groups.size} groups")

                // ✅ Récupérer les membres de chaque groupe pour savoir si l'user est membre
                // Pour chaque groupe, vérifier l'appartenance via plusieurs méthodes
                // Note: Si l'utilisateur a rejoint via Swagger, on peut ne pas le détecter immédiatement
                // mais l'erreur 400 lors d'un join tentatif mettra à jour le cache
                val groupsWithStatus = groups.map { group ->
                    // Normaliser l'userId pour la comparaison
                    val userIdStr = userId.trim()
                    
                    // Méthode 1: Vérifier dans le cache local
                    val isInCache = _joinedGroupIds.value.contains(group._id)
                    
                    // Méthode 2: Vérifier si créateur
                    val isCreator = isCreator(group, userId)
                    
                    // Méthode 3: Vérifier dans le tableau members avec comparaison améliorée
                    // ObjectId MongoDB sont des strings de 24 caractères hexadécimaux
                    val isMemberInArray = group.members.any { memberId ->
                        val memberIdStr = memberId.toString().trim()
                        // Comparaison exacte (ObjectId sont case-sensitive mais on compare sans case pour sécurité)
                        val exactMatch = memberIdStr.equals(userIdStr, ignoreCase = true) || memberIdStr == userIdStr
                        // Si les deux IDs ont la même longueur (ObjectId = 24 chars), comparer en lowercase
                        val sameLengthMatch = memberIdStr.length == userIdStr.length && 
                                            memberIdStr.length >= 20 &&
                                            memberIdStr.lowercase() == userIdStr.lowercase()
                        exactMatch || sameLengthMatch
                    }
                    
                    // Si aucune méthode n'a détecté l'appartenance, on considère qu'on n'est pas membre
                    // (si l'utilisateur a rejoint via Swagger, l'erreur 400 lors du join mettra à jour le cache)
                    val isMember = isInCache || isMemberInArray || isCreator
                    
                    // Si on n'est pas sûr et que le groupe a des membres, vérifier via l'API (optionnel, coûteux)
                    // On skip cette vérification pour l'instant car trop coûteux, mais on peut l'ajouter si nécessaire

                    // Logging détaillé pour debugging
                    if (isMember) {
                        Log.d("GroupsService", "✅ User IS member of group: ${group._id} (${group.name})")
                        Log.d("GroupsService", "  - User ID: $userIdStr (length: ${userIdStr.length})")
                        Log.d("GroupsService", "  - In joinedGroupIds cache: $isInCache")
                        Log.d("GroupsService", "  - In members array: $isMemberInArray")
                        Log.d("GroupsService", "  - Is creator: $isCreator")
                        Log.d("GroupsService", "  - Group members count: ${group.members.size}")
                        if (group.members.isNotEmpty()) {
                            group.members.forEachIndexed { index, memberId ->
                                Log.d("GroupsService", "  - Member[$index] ID: ${memberId} (length: ${memberId.toString().length})")
                            }
                        }
                        Log.d("GroupsService", "  - Group createdBy: ${group.createdBy} (length: ${group.createdBy?.toString()?.length})")
                    } else {
                        Log.d("GroupsService", "❌ User is NOT member of group: ${group._id} (${group.name})")
                        Log.d("GroupsService", "  - User ID: $userIdStr (length: ${userIdStr.length})")
                        Log.d("GroupsService", "  - In joinedGroupIds cache: $isInCache")
                        Log.d("GroupsService", "  - Group members count: ${group.members.size}")
                        if (group.members.isNotEmpty()) {
                            group.members.forEachIndexed { index, memberId ->
                                val memberIdStr = memberId.toString()
                                Log.d("GroupsService", "  - Member[$index] ID: $memberIdStr (length: ${memberIdStr.length})")
                                Log.d("GroupsService", "    Comparison: '${memberIdStr.lowercase()}' vs '${userIdStr.lowercase()}' = ${memberIdStr.lowercase() == userIdStr.lowercase()}")
                            }
                        }
                        Log.d("GroupsService", "  - Group createdBy: ${group.createdBy} (length: ${group.createdBy?.toString()?.length})")
                    }

                    group.copy(
                        memberCount = group.members.size,
                        isUserMember = isMember
                    )
                }

                _allGroups.value = groupsWithStatus

                // Mettre à jour myGroups (groupes où l'utilisateur est membre mais pas créateur)
                _myGroups.value = groupsWithStatus.filter { it.isUserMember && !isCreator(it, userId) }

                // Mettre à jour myCreatedGroups (groupes créés par l'utilisateur)
                _myCreatedGroups.value = groupsWithStatus.filter { isCreator(it, userId) }

                Log.d("GroupsService", "✅ Loaded ${groupsWithStatus.size} groups")
                Log.d("GroupsService", "✅ User is member of ${_myGroups.value.size} groups")
                Log.d("GroupsService", "✅ User created ${_myCreatedGroups.value.size} groups")

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

    suspend fun getMyGroups(): Result<List<Group>> {
        return try {
            val allGroupsResult = getAllGroups()
            if (allGroupsResult.isSuccess) {
                Result.success(_myGroups.value)
            } else {
                allGroupsResult
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createGroup(request: Map<String, String>): Result<Group> {
        return try {
            _isLoading.value = true
            _error.value = null

            Log.d("GroupsService", "=== CREATE GROUP ===")
            Log.d("GroupsService", "Request: $request")
            Log.d("GroupsService", "Auth Token: ${getAuthToken().take(20)}...")

            // Backend DTO only accepts: name, description, image (optional)
            // Note: destination existe dans le schema Group mais pas dans CreateGroupDto
            val createRequest = CreateGroupRequest(
                name = request["name"]?.trim() ?: "",
                description = request["description"]?.trim() ?: "",
                image = request["image"]?.takeIf { it.isNotBlank() }
            )

            Log.d("GroupsService", "CreateRequest: name=${createRequest.name}, description=${createRequest.description}")

            val response = groupsApi.createGroup(getAuthToken(), createRequest)

            Log.d("GroupsService", "Response code: ${response.code()}")
            Log.d("GroupsService", "Response isSuccessful: ${response.isSuccessful}")

            if (response.isSuccessful && response.body() != null) {
                val group = response.body()!!
                Log.d("GroupsService", "✅ Group created: ${group._id}")
                Log.d("GroupsService", "Group members: ${group.members}")
                Log.d("GroupsService", "Group createdBy: ${group.createdBy}")
                Log.d("GroupsService", "Current user ID: ${getUserId()}")

                // ✅ Ajouter le groupe aux groupes rejoints (créateur = automatiquement membre)
                _joinedGroupIds.value = _joinedGroupIds.value + group._id
                Log.d("GroupsService", "Added to joinedGroupIds: ${_joinedGroupIds.value}")

                // Refresh groups to update UI
                getAllGroups()
                Result.success(group)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "No error body"
                } catch (e: Exception) {
                    "Error reading error body: ${e.message}"
                }
                val errorMsg = "Erreur lors de la création: ${response.code()} - $errorBody"
                Log.e("GroupsService", errorMsg)
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("GroupsService", "❌ Error creating group", e)
            e.printStackTrace()
            _error.value = e.message ?: "Erreur inconnue lors de la création du groupe"
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun joinGroup(groupId: String): Result<GroupMember> {
        return try {
            _isLoading.value = true
            _error.value = null

            Log.d("GroupsService", "=== JOIN GROUP ===")
            Log.d("GroupsService", "Group ID: $groupId")
            Log.d("GroupsService", "User ID: ${getUserId()}")

            val response = groupsApi.joinGroup(groupId, getAuthToken())

            Log.d("GroupsService", "Join response code: ${response.code()}")
            Log.d("GroupsService", "Join response isSuccessful: ${response.isSuccessful}")

            if (response.isSuccessful && response.body() != null) {
                val member = response.body()!!
                Log.d("GroupsService", "✅ Joined group successfully")
                Log.d("GroupsService", "Member: ${member.userId ?: "null"}, Group: ${member.groupId ?: "null"}")

                // ✅ Ajouter aux groupes rejoints
                _joinedGroupIds.value = _joinedGroupIds.value + groupId
                Log.d("GroupsService", "Updated joinedGroupIds: ${_joinedGroupIds.value}")

                // Refresh groups to update UI with new membership status
                // Attendre un peu pour que le backend mette à jour
                kotlinx.coroutines.delay(500)
                getAllGroups()
                Result.success(member)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "No error body"
                } catch (e: Exception) {
                    "Error reading error body: ${e.message}"
                }
                
                // Si l'erreur indique qu'on est déjà membre, mettre à jour le cache
                if (response.code() == 400 && errorBody.contains("déjà membre")) {
                    Log.d("GroupsService", "⚠️ User is already member (backend confirmed), updating cache")
                    _joinedGroupIds.value = _joinedGroupIds.value + groupId
                    // Refresh groups to update UI
                    getAllGroups()
                }
                
                val errorMsg = "Erreur lors de l'inscription: ${response.code()} - $errorBody"
                Log.e("GroupsService", errorMsg)
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("GroupsService", "❌ Error joining group", e)
            e.printStackTrace()
            _error.value = e.message ?: "Erreur inconnue lors de l'inscription au groupe"
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun leaveGroup(groupId: String): Result<Unit> {
        return try {
            _isLoading.value = true
            _error.value = null

            Log.d("GroupsService", "=== LEAVE GROUP ===")
            Log.d("GroupsService", "Group ID: $groupId")
            Log.d("GroupsService", "User ID: ${getUserId()}")

            val response = groupsApi.leaveGroup(groupId, getAuthToken())

            Log.d("GroupsService", "Leave response code: ${response.code()}")
            Log.d("GroupsService", "Leave response isSuccessful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val leaveResponse = response.body()
                Log.d("GroupsService", "✅ Left group successfully")
                Log.d("GroupsService", "Response message: ${leaveResponse?.message}")

                // ✅ Retirer des groupes rejoints
                _joinedGroupIds.value = _joinedGroupIds.value - groupId
                Log.d("GroupsService", "Updated joinedGroupIds: ${_joinedGroupIds.value}")

                // Refresh groups to update UI with new membership status
                getAllGroups()
                Result.success(Unit)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "No error body"
                } catch (e: Exception) {
                    "Error reading error body: ${e.message}"
                }
                val errorMsg = "Erreur lors de la sortie: ${response.code()} - $errorBody"
                Log.e("GroupsService", errorMsg)
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("GroupsService", "❌ Error leaving group", e)
            e.printStackTrace()
            _error.value = e.message ?: "Erreur inconnue lors de la sortie du groupe"
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
                Result.failure(Exception("Erreur lors du chargement des membres"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper method to check if user is member of a specific group by querying members
    suspend fun checkUserMembership(groupId: String): Boolean {
        return try {
            val membersResult = getGroupMembers(groupId)
            if (membersResult.isSuccess) {
                val userId = getUserId()
                val members = membersResult.getOrNull() ?: emptyList()
                val isMember = members.any { member ->
                    val memberUserId = member.userId?.toString()?.trim() ?: ""
                    val currentUserId = userId.trim()
                    memberUserId.isNotEmpty() && currentUserId.isNotEmpty() && (
                        memberUserId.equals(currentUserId, ignoreCase = true) ||
                        memberUserId == currentUserId ||
                        (member.status.equals("active", ignoreCase = true) && 
                        (memberUserId.contains(currentUserId) || currentUserId.contains(memberUserId)))
                    )
                }
                Log.d("GroupsService", "Membership check for group $groupId: $isMember")
                isMember
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("GroupsService", "Error checking membership", e)
            false
        }
    }

    suspend fun deleteGroup(groupId: String): Result<Unit> {
        return try {
            _isLoading.value = true
            _error.value = null

            Log.d("GroupsService", "=== DELETE GROUP ===")
            Log.d("GroupsService", "Group ID: $groupId")
            Log.d("GroupsService", "User ID: ${getUserId()}")

            val response = groupsApi.deleteGroup(groupId, getAuthToken())

            Log.d("GroupsService", "Delete response code: ${response.code()}")
            Log.d("GroupsService", "Delete response isSuccessful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val deleteResponse = response.body()
                Log.d("GroupsService", "✅ Group deleted successfully")
                Log.d("GroupsService", "Response message: ${deleteResponse?.message}")

                // Retirer des groupes rejoints
                _joinedGroupIds.value = _joinedGroupIds.value - groupId

                // Refresh groups to update UI
                getAllGroups()
                Result.success(Unit)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "No error body"
                } catch (e: Exception) {
                    "Error reading error body: ${e.message}"
                }
                val errorMsg = "Erreur lors de la suppression: ${response.code()} - $errorBody"
                Log.e("GroupsService", errorMsg)
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("GroupsService", "❌ Error deleting group", e)
            e.printStackTrace()
            _error.value = e.message ?: "Erreur inconnue lors de la suppression du groupe"
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}