package com.travelmate.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.*
import com.travelmate.data.service.GroupsService
import com.travelmate.data.socket.GroupChatSocketService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val groupsService: GroupsService,
    private val chatSocketService: GroupChatSocketService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val allGroups = groupsService.allGroups
    val myGroups = groupsService.myGroups
    val myCreatedGroups = groupsService.myCreatedGroups
    val currentGroup = groupsService.currentGroup
    val isLoading = groupsService.isLoading
    val error = groupsService.error

    private val _groupMessages = MutableStateFlow<List<MessageGroupe>>(emptyList())
    val groupMessages: StateFlow<List<MessageGroupe>> = _groupMessages.asStateFlow()

    val socketConnectionState = chatSocketService.connectionState
    val socketError = chatSocketService.error
    val typingUsers = chatSocketService.typingUsers

    // ✅ NEW: Expose group members and reactions from socket service
    val groupMembers = chatSocketService.groupMembers
    val reactionsByEmoji = chatSocketService.reactionsByEmoji

    private val _pendingRequests = MutableStateFlow<List<PendingRequest>>(emptyList())
    val pendingRequests: StateFlow<List<PendingRequest>> = _pendingRequests.asStateFlow()

    private val _filterQuery = MutableStateFlow("")
    val filterQuery: StateFlow<String> = _filterQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.RECENT)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _filteredGroups = MutableStateFlow<List<Group>>(emptyList())
    val filteredGroups: StateFlow<List<Group>> = _filteredGroups.asStateFlow()

    private var currentUserId: String = ""
    private var observersInitialized = false
    
    // ✅ Flags pour éviter les clics multiples rapides
    private var lastReactionMessageId: String? = null
    private var lastReactionEmoji: String? = null

    init {
        viewModelScope.launch {
            allGroups.collect { groups ->
                applyFiltersAndSort(groups)
            }
        }

        if (!observersInitialized) {
            connectToWebSocket()
            observeWebSocketMessages()
            observeWebSocketDeletes()
            observeWebSocketUpdates()
            observeWebSocketReactions()
            observeTypingUsers()
            observersInitialized = true
            Log.d("GroupsViewModel", "✅ Observers initialized (once)")
        }
    }

    fun setUserId(userId: String) {
        currentUserId = userId
        Log.d("GroupsViewModel", "✅ UserId set: $userId")
    }

    private fun connectToWebSocket() {
        viewModelScope.launch {
            try {
                chatSocketService.connect()
                Log.d("GroupsViewModel", "✅ WebSocket connection initiated")
            } catch (e: Exception) {
                Log.e("GroupsViewModel", "❌ Error connecting to WebSocket", e)
            }
        }
    }

    private fun observeWebSocketMessages() {
        viewModelScope.launch {
            chatSocketService.newMessage.collect { newMessage ->
                if (newMessage != null) {
                    Log.d("GroupsViewModel", "📨 New message via WebSocket: ${newMessage.content}")

                    // Ajout local immédiat
                    val currentMessages = _groupMessages.value.toMutableList()
                    currentMessages.removeAll { it.id.startsWith("temp_") }
                    if (!currentMessages.any { it.id == newMessage.id }) {
                        currentMessages.add(newMessage)
                        _groupMessages.value = currentMessages
                        Log.d("GroupsViewModel", "✅ Message ajouté à la liste (total: ${currentMessages.size})")
                    }

                    // Recharge la liste complète depuis l'API pour garantir la synchro
                    currentGroup.value?._id?.let { groupId ->
                        loadGroupMessages(groupId)
                    }

                    chatSocketService.resetNewMessage()
                }
            }
        }
    }

    private fun observeWebSocketDeletes() {
        viewModelScope.launch {
            chatSocketService.messageDeleted.collect { deletedId ->
                if (deletedId != null) {
                    Log.d("GroupsViewModel", "🗑️ Message deleted via WebSocket: $deletedId")

                    val currentMessages = _groupMessages.value.toMutableList()
                    val sizeBefore = currentMessages.size
                    currentMessages.removeAll { it.id == deletedId }
                    _groupMessages.value = currentMessages

                    Log.d("GroupsViewModel", "✅ Message supprimé (${sizeBefore} -> ${currentMessages.size})")

                    chatSocketService.resetMessageDeleted()
                }
            }
        }
    }

    private fun observeWebSocketUpdates() {
        viewModelScope.launch {
            chatSocketService.messageUpdated.collect { updatedMessage ->
                if (updatedMessage != null) {
                    Log.d("GroupsViewModel", "✏️ Message updated via WebSocket: ${updatedMessage.id}")

                    val currentMessages = _groupMessages.value.toMutableList()
                    val index = currentMessages.indexOfFirst { it.id == updatedMessage.id }

                    if (index != -1) {
                        currentMessages[index] = updatedMessage
                        _groupMessages.value = currentMessages
                        Log.d("GroupsViewModel", "✅ Message mis à jour à l'index $index")
                    } else {
                        Log.w("GroupsViewModel", "⚠️ Message ${updatedMessage.id} non trouvé pour mise à jour")
                    }

                    chatSocketService.resetMessageUpdated()
                }
            }
        }
    }

    private fun observeWebSocketReactions() {
        viewModelScope.launch {
            chatSocketService.messageReacted.collect { reactedMessage ->
                    if (reactedMessage != null) {
                        val timestamp = System.currentTimeMillis()
                        Log.d("GroupsViewModel", "🔄 Reaction WebSocket recue: ${reactedMessage.id} [$timestamp]")
                        Log.d("GroupsViewModel", "   Reactions totales: ${reactedMessage.reactions.size}")

                        val currentMessages = _groupMessages.value.toMutableList()
                        val index = currentMessages.indexOfFirst { it.id == reactedMessage.id }

                        if (index != -1) {
                            Log.d("GroupsViewModel", "   Updating message at index $index")
                            currentMessages[index] = reactedMessage
                            Log.d("GroupsViewModel", "   Setting _groupMessages.value...")
                            _groupMessages.value = currentMessages
                            Log.d("GroupsViewModel", "✅ Message updated with ${reactedMessage.reactions.size} reactions [$timestamp]")
                        } else {
                            Log.w("GroupsViewModel", "⚠️ Message ${reactedMessage.id} not found")
                        }

                        kotlinx.coroutines.delay(50)
                        Log.d("GroupsViewModel", "   Resetting messageReacted")
                        chatSocketService.resetMessageReacted()
                    }
                }
        }
    }

    private fun observeTypingUsers() {
        viewModelScope.launch {
            chatSocketService.typingUsers.collect { users ->
                Log.d("GroupsViewModel", " Typing users: ${users.size}")
                Log.d("GroupsViewModel", "⌨️ Typing users: ${users.size}")
            }
        }
    }

    fun joinGroupChat(groupId: String) {
        chatSocketService.joinGroup(groupId)
        Log.d("GroupsViewModel", "🚪 Joining group chat: $groupId")
    }

    fun leaveGroupChat(groupId: String) {
        chatSocketService.leaveGroup(groupId)
        Log.d("GroupsViewModel", "🚶 Leaving group chat: $groupId")
    }

    fun sendTypingIndicator(groupId: String, isTyping: Boolean) {
        chatSocketService.sendTypingIndicator(groupId, isTyping)
    }

    fun loadAllGroups() {
        viewModelScope.launch {
            groupsService.getAllGroups()
        }
    }

    fun loadGroupById(groupId: String) {
        viewModelScope.launch {
            groupsService.getGroupById(groupId)
        }
    }

    fun createGroup(name: String, destination: String, description: String, imageUrl: String? = null) {
        viewModelScope.launch {
            val request = mutableMapOf(
                "name" to name.trim(),
                "description" to description.trim(),
                "destination" to destination.trim()
            )
            imageUrl?.let { request["image"] = it }

            val result = groupsService.createGroup(request)
            if (result.isSuccess) {
                loadAllGroups()
            }
        }
    }

    fun updateGroup(groupId: String, name: String?, destination: String?, description: String?, imageUrl: String?) {
        viewModelScope.launch {
            val result = groupsService.updateGroup(groupId, name, destination, description, imageUrl)
            if (result.isSuccess) {
                loadAllGroups()
                loadGroupById(groupId)
            }
        }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            val result = groupsService.deleteGroup(groupId)
            if (result.isSuccess) {
                loadAllGroups()
            }
        }
    }

    fun joinGroup(groupId: String) {
        viewModelScope.launch {
            val result = groupsService.joinGroup(groupId)
            if (result.isSuccess) {
                loadAllGroups()
            }
        }
    }

    fun leaveGroup(groupId: String) {
        viewModelScope.launch {
            val result = groupsService.leaveGroup(groupId)
            if (result.isSuccess) {
                leaveGroupChat(groupId)
                loadAllGroups()
            }
        }
    }

    fun loadPendingRequests(groupId: String) {
        viewModelScope.launch {
            try {
                val requests = groupsService.getPendingRequests(groupId)
                _pendingRequests.value = requests
                Log.d("GroupsViewModel", "✅ Loaded ${requests.size} pending requests")
            } catch (e: Exception) {
                Log.e("GroupsViewModel", "❌ Error loading pending requests", e)
            }
        }
    }

    fun approveMember(groupId: String, userId: String) {
        viewModelScope.launch {
            try {
                groupsService.approveMember(groupId, userId)
                loadPendingRequests(groupId)
                loadGroupById(groupId)
                loadAllGroups()
                Log.d("GroupsViewModel", "✅ Member approved")
            } catch (e: Exception) {
                Log.e("GroupsViewModel", "❌ Error approving member", e)
                groupsService.setError("Erreur lors de l'approbation")
            }
        }
    }

    fun rejectMember(groupId: String, userId: String) {
        viewModelScope.launch {
            try {
                groupsService.rejectMember(groupId, userId)
                loadPendingRequests(groupId)
                Log.d("GroupsViewModel", "✅ Member rejected")
            } catch (e: Exception) {
                Log.e("GroupsViewModel", "❌ Error rejecting member", e)
                groupsService.setError("Erreur lors du rejet")
            }
        }
    }

    fun loadGroupMessages(groupId: String) {
        viewModelScope.launch {
            Log.d("GroupsViewModel", "📥 Loading messages for group: $groupId")

            val result = groupsService.getGroupMessages(groupId)
            if (result.isSuccess) {
                val messages = result.getOrNull() ?: emptyList()
                _groupMessages.value = messages

                Log.d("GroupsViewModel", "✅ Loaded ${messages.size} messages")

                joinGroupChat(groupId)
            } else {
                Log.e("GroupsViewModel", "❌ Error loading messages: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    // ✅ CRÉER UN MESSAGE - API REST + WebSocket notification
    fun createMessage(groupId: String, content: String) {
        viewModelScope.launch {
            try {
                Log.d("GroupsViewModel", "📤 Envoi message via API REST...")

                // Ajout d'un message temporaire localement pour affichage instantané
                val tempId = "temp_${System.currentTimeMillis()}"
                val tempMessage = MessageGroupe(
                    id = tempId,
                    groupId = groupId,
                    authorId = AuthorInfo(id = currentUserId),
                    content = content,
                    images = emptyList(),
                    reactions = emptyList(),
                    status = "publie",
                    createdAt = System.currentTimeMillis().toString(),
                    updatedAt = "",
                    tempId = tempId
                )
                val currentMessages = _groupMessages.value.toMutableList()
                currentMessages.add(tempMessage)
                _groupMessages.value = currentMessages

                // 1. Sauvegarder via API REST
                val result = groupsService.createMessage(groupId, content, emptyList())

                if (result.isSuccess) {
                    Log.d("GroupsViewModel", "✅ Message sauvegardé en BDD")
                    // Ne pas recharger la liste, laisser le WebSocket gérer l'ajout réel
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Erreur inconnue"
                    Log.e("GroupsViewModel", "❌ Erreur API: $errorMsg")
                    groupsService.setError("Erreur lors de l'envoi: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("GroupsViewModel", "❌ Exception createMessage", e)
                groupsService.setError("Erreur: ${e.message}")
            }
        }
    }

    // ✅ CRÉER UN MESSAGE AVEC IMAGE - API REST complete
    fun createMessageWithImage(groupId: String, content: String, imageUri: Uri) {
        viewModelScope.launch {
            try {
                Log.d("GroupsViewModel", "📤 Upload image puis envoi message...")

                // ✅ 1. Upload l'image
                val uploadResult = groupsService.uploadMessageImage(imageUri)

                if (uploadResult.isFailure) {
                    val errorMsg = uploadResult.exceptionOrNull()?.message ?: "Erreur upload"
                    Log.e("GroupsViewModel", "❌ Erreur upload image: $errorMsg")
                    groupsService.setError("Erreur lors de l'upload de l'image")
                    return@launch
                }

                val imageUrl = uploadResult.getOrNull()
                if (imageUrl == null) {
                    Log.e("GroupsViewModel", "❌ Image URL null")
                    groupsService.setError("Erreur lors de l'upload de l'image")
                    return@launch
                }

                Log.d("GroupsViewModel", "✅ Image uploadée: $imageUrl")

                // ✅ 2. Créer le message avec l'image via API REST
                val result = groupsService.createMessage(groupId, content, listOf(imageUrl))

                if (result.isSuccess) {
                    Log.d("GroupsViewModel", "✅ Message avec image sauvegardé")
                    
                    // ✅ 3. Recharger les messages
                    loadGroupMessages(groupId)
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Erreur inconnue"
                    Log.e("GroupsViewModel", "❌ Erreur création message: $errorMsg")
                    groupsService.setError("Erreur lors de l'envoi")
                }
            } catch (e: Exception) {
                Log.e("GroupsViewModel", "❌ Exception createMessageWithImage", e)
                groupsService.setError("Erreur: ${e.message}")
            }
        }
    }

    fun deleteMessage(groupId: String, messageId: String) {
        viewModelScope.launch {
            try {
                Log.d("GroupsViewModel", "🗑️ Suppression du message: $messageId")

                // ✅ Suppression via API REST
                val result = groupsService.deleteMessage(groupId, messageId)

                if (result.isSuccess) {
                    Log.d("GroupsViewModel", "✅ Message supprimé du serveur")
                    
                    // ✅ Retirer de la liste locale
                    val currentMessages = _groupMessages.value.toMutableList()
                    currentMessages.removeAll { it.id == messageId }
                    _groupMessages.value = currentMessages
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Erreur inconnue"
                    Log.e("GroupsViewModel", "❌ Erreur suppression: $errorMsg")
                    groupsService.setError("Erreur lors de la suppression")
                }
            } catch (e: Exception) {
                Log.e("GroupsViewModel", "❌ Exception deleteMessage", e)
                groupsService.setError("Erreur: ${e.message}")
            }
        }
    }

    fun updateMessage(groupId: String, messageId: String, newContent: String) {
        viewModelScope.launch {
            try {
                Log.d("GroupsViewModel", "✏️ Modification du message: $messageId")

                // ✅ Modification via API REST
                val result = groupsService.updateMessage(groupId, messageId, newContent)

                if (result.isSuccess) {
                    Log.d("GroupsViewModel", "✅ Message modifié sur le serveur")
                    
                    // ✅ Mettre à jour la liste locale
                    val updatedMessage = result.getOrNull()
                    if (updatedMessage != null) {
                        val currentMessages = _groupMessages.value.toMutableList()
                        val index = currentMessages.indexOfFirst { it.id == messageId }
                        if (index != -1) {
                            currentMessages[index] = updatedMessage
                            _groupMessages.value = currentMessages
                        }
                    }
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Erreur inconnue"
                    Log.e("GroupsViewModel", "❌ Erreur modification: $errorMsg")
                    groupsService.setError("Erreur lors de la modification")
                }
            } catch (e: Exception) {
                Log.e("GroupsViewModel", "❌ Exception updateMessage", e)
                groupsService.setError("Erreur: ${e.message}")
            }
        }
    }

    fun toggleReaction(groupId: String, messageId: String, emoji: String) {
        viewModelScope.launch {
            try {
                // ✅ Éviter les clics multiples rapides
                if (lastReactionMessageId == messageId && lastReactionEmoji == emoji) {
                    Log.w("GroupsViewModel", "⚠️ Reaction déjà en cours pour ce message/emoji")
                    return@launch
                }
                
                lastReactionMessageId = messageId
                lastReactionEmoji = emoji
                
                Log.d("GroupsViewModel", "👍 Toggle réaction via API REST: $emoji sur message $messageId")

                // ✅ Toggle via API REST
                val result = groupsService.toggleReaction(groupId, messageId, emoji)

                if (result.isSuccess) {
                    val updatedMessage = result.getOrNull()
                    if (updatedMessage != null) {
                        Log.d("GroupsViewModel", "✅ Reaction toggle réussie (${updatedMessage.reactions.size} reactions)")
                        
                        // ✅ Mettre à jour le message dans la liste
                        val currentMessages = _groupMessages.value.toMutableList()
                        val index = currentMessages.indexOfFirst { it.id == messageId }
                        if (index != -1) {
                            currentMessages[index] = updatedMessage
                            _groupMessages.value = currentMessages
                        }
                    }
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Erreur inconnue"
                    Log.e("GroupsViewModel", "❌ Erreur toggle reaction: $errorMsg")
                }
                
                // ✅ Réinitialiser les flags après un délai
                kotlinx.coroutines.delay(500)
                lastReactionMessageId = null
                lastReactionEmoji = null
                
            } catch (e: Exception) {
                Log.e("GroupsViewModel", "❌ Exception toggleReaction", e)
                lastReactionMessageId = null
                lastReactionEmoji = null
            }
        }
    }

    fun uploadGroupImage(imageUri: Uri, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = groupsService.uploadGroupImage(imageUri)

            if (result.isSuccess) {
                result.getOrNull()?.let {
                    Log.d("GroupsViewModel", "✅ Group image uploaded: $it")
                    onSuccess(it)
                }
            } else {
                val error = result.exceptionOrNull()?.message ?: "Erreur upload"
                Log.e("GroupsViewModel", "❌ Error uploading group image: $error")
                onError(error)
            }
        }
    }

    fun uploadMessageImage(imageUri: Uri): String? {
        // ✅ Cette fonction est synchrone, on la rend suspend
        // Pour l'instant, on retourne null et on loggue
        Log.w("GroupsViewModel", "⚠️ uploadMessageImage appelée de manière synchrone - utilisez createMessageWithImage à la place")
        return null
    }

    fun setFilterQuery(query: String) {
        _filterQuery.value = query
        applyFiltersAndSort(allGroups.value)
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
        applyFiltersAndSort(allGroups.value)
    }

    private fun applyFiltersAndSort(groups: List<Group>) {
        var filtered = groups

        if (_filterQuery.value.isNotBlank()) {
            filtered = filtered.filter { group ->
                group.name.contains(_filterQuery.value, ignoreCase = true) ||
                        group.description.contains(_filterQuery.value, ignoreCase = true) ||
                        (group.destination?.contains(_filterQuery.value, ignoreCase = true) == true)
            }
        }

        filtered = when (_sortOption.value) {
            SortOption.RECENT -> filtered.sortedByDescending { it.createdAt }
            SortOption.OLDEST -> filtered.sortedBy { it.createdAt }
            SortOption.NAME_AZ -> filtered.sortedBy { it.name.lowercase() }
            SortOption.NAME_ZA -> filtered.sortedByDescending { it.name.lowercase() }
            SortOption.MOST_MEMBERS -> filtered.sortedByDescending { it.memberCount }
            SortOption.LEAST_MEMBERS -> filtered.sortedBy { it.memberCount }
        }

        _filteredGroups.value = filtered
    }

    fun clearError() {
        groupsService.clearError()
        chatSocketService.clearError()
    }

    // ✅ NEW: Public functions to access socket service methods
    fun getGroupMembers(groupId: String) {
        viewModelScope.launch {
            try {
                Log.d("GroupsViewModel", "📥 Loading members for group: $groupId")
                val result = groupsService.getGroupMembers(groupId)
                if (result.isSuccess) {
                    val members = result.getOrNull() ?: emptyList()
                    Log.d("GroupsViewModel", "✅ Loaded ${members.size} members")
                    // Update the members flow from chatSocketService
                    chatSocketService.updateGroupMembers(members)
                } else {
                    Log.e("GroupsViewModel", "❌ Error loading members: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e("GroupsViewModel", "❌ Exception loading members", e)
            }
        }
    }

    fun resetGroupMembers() {
        chatSocketService.resetGroupMembers()
    }

    fun getReactionsByEmoji(messageId: String, emoji: String) {
        chatSocketService.getReactionsByEmoji(messageId, emoji)
    }

    fun resetReactionsByEmoji() {
        chatSocketService.resetReactionsByEmoji()
    }

    // ✅ NEW: Remove member from group
    fun removeMember(groupId: String, userId: String, action: String = "remove") {
        viewModelScope.launch {
            try {
                Log.d("GroupsViewModel", "🗑️ Removing member - groupId: $groupId, userId: $userId, action: $action")
                groupsService.removeMember(groupId, userId, action)
                Log.d("GroupsViewModel", "✅ Member $userId removed/banned from group $groupId")
            } catch (e: Exception) {
                Log.e("GroupsViewModel", "❌ Error removing member: ${e.message}", e)
                e.printStackTrace()
                groupsService.setError("Erreur: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatSocketService.disconnect()
        Log.d("GroupsViewModel", "🧹 ViewModel cleared - WebSocket disconnected")
    }
}

// ✅ Extension function pour copier MessageGroupe
private fun MessageGroupe.copy(
    id: String = this.id,
    groupId: String = this.groupId,
    authorId: AuthorInfo? = this.authorId,
    content: String = this.content,
    images: List<String> = this.images,
    reactions: List<MessageReaction> = this.reactions,
    status: String = this.status,
    createdAt: String = this.createdAt,
    updatedAt: String = this.updatedAt
) = MessageGroupe(
    id = id,
    groupId = groupId,
    authorId = authorId,
    content = content,
    images = images,
    reactions = reactions,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)

enum class SortOption(val label: String) {
    RECENT("Plus récents"),
    OLDEST("Plus anciens"),
    NAME_AZ("Nom (A-Z)"),
    NAME_ZA("Nom (Z-A)"),
    MOST_MEMBERS("Plus de membres"),
    LEAST_MEMBERS("Moins de membres")
}