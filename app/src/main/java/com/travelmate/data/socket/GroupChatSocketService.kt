package com.travelmate.data.socket

import android.content.Context
import android.util.Log
import com.travelmate.data.models.GroupMember
import com.travelmate.data.models.MessageGroupe
import com.travelmate.data.models.ReactionDetail
import dagger.hilt.android.qualifiers.ApplicationContext
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupChatSocketService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var socket: Socket? = null
    private val json = Json { ignoreUnknownKeys = true }
    private var isListenersSetup = false
    private var isConnecting = false

    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _newMessage = MutableStateFlow<MessageGroupe?>(null)
    val newMessage: StateFlow<MessageGroupe?> = _newMessage.asStateFlow()

    private val _messageDeleted = MutableStateFlow<String?>(null)
    val messageDeleted: StateFlow<String?> = _messageDeleted.asStateFlow()

    private val _messageUpdated = MutableStateFlow<MessageGroupe?>(null)
    val messageUpdated: StateFlow<MessageGroupe?> = _messageUpdated.asStateFlow()

    private val _messageReacted = MutableStateFlow<MessageGroupe?>(null)
    val messageReacted: StateFlow<MessageGroupe?> = _messageReacted.asStateFlow()

    private val _typingUsers = MutableStateFlow<Set<String>>(emptySet())
    val typingUsers: StateFlow<Set<String>> = _typingUsers.asStateFlow()

    // ✅ NEW: Group members
    private val _groupMembers = MutableStateFlow<List<GroupMember>>(emptyList())
    val groupMembers: StateFlow<List<GroupMember>> = _groupMembers.asStateFlow()

    // ✅ NEW: Reactions by emoji
    private val _reactionsByEmoji = MutableStateFlow<List<ReactionDetail>>(emptyList())
    val reactionsByEmoji: StateFlow<List<ReactionDetail>> = _reactionsByEmoji.asStateFlow()

    // ✅ NEW: Member removed event
    data class MemberRemovedEvent(
        val groupId: String,
        val targetUserId: String,
        val action: String
    )
    private val _memberRemoved = MutableStateFlow<MemberRemovedEvent?>(null)
    val memberRemoved: StateFlow<MemberRemovedEvent?> = _memberRemoved.asStateFlow()

    fun connect(baseUrl: String = "http://10.0.2.2:3000") {
        try {
            if (isConnecting) {
                Log.d("GroupChatSocket", "⏳ Connection already in progress, skip")
                return
            }

            if (socket?.connected() == true) {
                Log.d("GroupChatSocket", "✅ Socket already connected, skip")
                return
            }

            isConnecting = true
            Log.d("GroupChatSocket", "🔍 Starting connection...")

            val prefs = context.getSharedPreferences("travelmate_prefs", Context.MODE_PRIVATE)
            val token = prefs.getString("access_token", null)

            if (token == null) {
                Log.e("GroupChatSocket", "❌ Token manquant")
                _error.value = "Token manquant"
                isConnecting = false
                return
            }

            Log.d("GroupChatSocket", "✅ Token: ${token.take(50)}...")

            val options = IO.Options().apply {
                auth = mapOf("token" to token)
                path = "/socket.io"
                transports = arrayOf("websocket", "polling")
                reconnection = true
                reconnectionAttempts = 10
                reconnectionDelay = 1000
                reconnectionDelayMax = 5000
                timeout = 30000
                forceNew = false
                upgrade = true
                rememberUpgrade = true
            }

            Log.d("GroupChatSocket", "🔧 Configuration:")
            Log.d("GroupChatSocket", "   URL: $baseUrl/groups")
            Log.d("GroupChatSocket", "   Path: /socket.io")
            Log.d("GroupChatSocket", "   Transports: websocket -> polling")

            if (socket == null) {
                socket = IO.socket("$baseUrl/groups", options)
                Log.d("GroupChatSocket", "✅ Socket cree")
            } else {
                Log.d("GroupChatSocket", "✅ Socket reutilise")
            }

            if (socket == null) {
                Log.e("GroupChatSocket", "❌ Socket null apres creation")
                return
            }

            if (!isListenersSetup) {
                setupSocketListeners()
                isListenersSetup = true
                Log.d("GroupChatSocket", "✅✅✅ Listeners registered ONCE")
            } else {
                Log.d("GroupChatSocket", "✅ Listeners already registered, skip")
            }

            Log.d("GroupChatSocket", "📡 Starting connection...")
            socket?.connect()
            isConnecting = false

        } catch (e: Exception) {
            Log.e("GroupChatSocket", "❌ FATAL ERROR", e)
            _error.value = "Error: ${e.message}"
            isConnecting = false
        }
    }

    private fun setupSocketListeners() {
        socket?.apply {
            off(Socket.EVENT_CONNECT)
            off(Socket.EVENT_DISCONNECT)
            off(Socket.EVENT_CONNECT_ERROR)
            off("connected")
            off("joinedGroup")
            off("leftGroup")
            off("newMessage")
            off("messageDeleted")
            off("messageUpdated")
            off("messageReacted")
            off("userTyping")
            off("error")
            off("groupMembers")
            off("reactionsByEmoji")
            off("memberRemoved")

            on(Socket.EVENT_CONNECT) {
                Log.d("GroupChatSocket", "✅✅✅ CONNECTE AU SERVEUR !")
                _connectionState.value = true
                _error.value = null
            }

            on(Socket.EVENT_DISCONNECT) { args ->
                val reason = args.getOrNull(0)?.toString() ?: "unknown"
                Log.d("GroupChatSocket", "❌ Deconnecte: $reason")
                _connectionState.value = false
            }

            on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = args.getOrNull(0)
                Log.e("GroupChatSocket", "❌ Erreur connexion: $error")
                _connectionState.value = false
            }

            on("connected") { args ->
                val data = args.getOrNull(0) as? JSONObject
                Log.d("GroupChatSocket", "✅ Event 'connected': $data")
            }

            on("joinedGroup") { args ->
                val data = args.getOrNull(0) as? JSONObject
                val groupId = data?.optString("groupId")
                Log.d("GroupChatSocket", "🚪 Groupe rejoint: $groupId")
            }

            on("leftGroup") { args ->
                val data = args.getOrNull(0) as? JSONObject
                val groupId = data?.optString("groupId")
                Log.d("GroupChatSocket", "🚶 Groupe quitté: $groupId")
            }

            on("newMessage") { args ->
                try {
                    val data = args.getOrNull(0) as? JSONObject
                    val msgJson = data?.optJSONObject("message")?.toString()

                    Log.d("GroupChatSocket", "📨 Event 'newMessage' reçu")

                    if (msgJson != null) {
                        val msg = json.decodeFromString<MessageGroupe>(msgJson)
                        Log.d("GroupChatSocket", "✅ Message parsé: ${msg.id}")
                        _newMessage.value = msg
                    } else {
                        Log.w("GroupChatSocket", "⚠️ messageJson est null")
                    }
                } catch (e: Exception) {
                    Log.e("GroupChatSocket", "❌ Erreur newMessage", e)
                }
            }

            on("messageDeleted") { args ->
                try {
                    val data = args.getOrNull(0) as? JSONObject
                    val id = data?.optString("messageId")
                    if (id != null) {
                        Log.d("GroupChatSocket", "🗑️ Message supprimé: $id")
                        _messageDeleted.value = id
                    }
                } catch (e: Exception) {
                    Log.e("GroupChatSocket", "❌ Erreur messageDeleted", e)
                }
            }

            on("messageUpdated") { args ->
                try {
                    val data = args.getOrNull(0) as? JSONObject
                    val msgJson = data?.optJSONObject("message")?.toString()
                    if (msgJson != null) {
                        val msg = json.decodeFromString<MessageGroupe>(msgJson)
                        Log.d("GroupChatSocket", "✏️ Message modifié: ${msg.id}")
                        _messageUpdated.value = msg
                    }
                } catch (e: Exception) {
                    Log.e("GroupChatSocket", "❌ Erreur messageUpdated", e)
                }
            }

            on("messageReacted") { args ->
                try {
                    val data = args.getOrNull(0) as? JSONObject
                    val msgJson = data?.optJSONObject("message")?.toString()
                    val timestamp = System.currentTimeMillis()

                    Log.d("GroupChatSocket", "👍 Event 'messageReacted' reçu [$timestamp]")

                    if (msgJson != null) {
                        val msg = json.decodeFromString<MessageGroupe>(msgJson)
                        Log.d("GroupChatSocket", "✅ Reaction parsee: ${msg.id} (${msg.reactions.size} reactions) [$timestamp]")
                        Log.d("GroupChatSocket", "   Setting _messageReacted.value = ${msg.id}")
                        _messageReacted.value = msg
                        Log.d("GroupChatSocket", "   _messageReacted.value set successfully")
                    } else {
                        Log.w("GroupChatSocket", "⚠️ messageJson est null dans messageReacted")
                    }
                } catch (e: Exception) {
                    Log.e("GroupChatSocket", "❌ Erreur messageReacted", e)
                }
            }

            on("userTyping") { args ->
                try {
                    val data = args.getOrNull(0) as? JSONObject
                    val userId = data?.optString("userId")
                    val isTyping = data?.optBoolean("isTyping") ?: false

                    if (userId != null) {
                        _typingUsers.value = if (isTyping) {
                            _typingUsers.value + userId
                        } else {
                            _typingUsers.value - userId
                        }
                        Log.d("GroupChatSocket", "⌨️ User $userId typing: $isTyping")
                    }
                } catch (e: Exception) {
                    Log.e("GroupChatSocket", "❌ Erreur userTyping", e)
                }
            }

            on("error") { args ->
                val data = args.getOrNull(0) as? JSONObject
                val msg = data?.optString("message") ?: "Erreur inconnue"
                Log.e("GroupChatSocket", "❌ Erreur serveur: $msg")
                _error.value = msg
            }

            // ✅ NEW: Listen for group members
            on("groupMembers") { args ->
                try {
                    val data = args.getOrNull(0) as? JSONObject
                    val membersArray = data?.optJSONArray("members")
                    
                    if (membersArray != null) {
                        val members = mutableListOf<GroupMember>()
                        for (i in 0 until membersArray.length()) {
                            val memberObj = membersArray.getJSONObject(i)
                            members.add(
                                GroupMember(
                                    id = memberObj.optString("_id"),
                                    nom = memberObj.optString("nom"),
                                    prenom = memberObj.optString("prenom"),
                                    avatar = memberObj.optString("avatar"),
                                    email = memberObj.optString("email"),
                                    isCreator = memberObj.optBoolean("isCreator")
                                )
                            )
                        }
                        Log.d("GroupChatSocket", "👥 Received ${members.size} members")
                        _groupMembers.value = members
                    }
                } catch (e: Exception) {
                    Log.e("GroupChatSocket", "❌ Error parsing groupMembers", e)
                }
            }

            // ✅ NEW: Listen for reactions by emoji
            on("reactionsByEmoji") { args ->
                try {
                    val data = args.getOrNull(0) as? JSONObject
                    val reactionsArray = data?.optJSONArray("reactions")
                    val emoji = data?.optString("emoji")
                    
                    if (reactionsArray != null && emoji != null) {
                        val reactions = mutableListOf<ReactionDetail>()
                        for (i in 0 until reactionsArray.length()) {
                            val reactionObj = reactionsArray.getJSONObject(i)
                            val userObj = reactionObj.optJSONObject("userId")
                            
                            if (userObj != null) {
                                reactions.add(
                                    ReactionDetail(
                                        userId = userObj.optString("_id"),
                                        emoji = emoji,
                                        nom = userObj.optString("nom"),
                                        prenom = userObj.optString("prenom"),
                                        avatar = userObj.optString("avatar"),
                                        reactedAt = reactionObj.optString("reactedAt")
                                    )
                                )
                            }
                        }
                        Log.d("GroupChatSocket", "😊 Received ${reactions.size} reactions for emoji $emoji")
                        _reactionsByEmoji.value = reactions
                    }
                } catch (e: Exception) {
                    Log.e("GroupChatSocket", "❌ Error parsing reactionsByEmoji", e)
                }
            }

            // ✅ NEW: Listen for member removed event
            on("memberRemoved") { args ->
                try {
                    val data = args.getOrNull(0) as? JSONObject
                    val groupId = data?.optString("groupId")
                    val targetUserId = data?.optString("targetUserId")
                    val action = data?.optString("action") ?: "remove"
                    
                    if (groupId != null && targetUserId != null) {
                        Log.d("GroupChatSocket", "🗑️ Member removed: $targetUserId from group $groupId (action: $action)")
                        _memberRemoved.value = MemberRemovedEvent(groupId, targetUserId, action)
                    }
                } catch (e: Exception) {
                    Log.e("GroupChatSocket", "❌ Error parsing memberRemoved", e)
                }
            }
        }
    }

    fun joinGroup(groupId: String) {
        if (socket?.connected() != true) {
            Log.w("GroupChatSocket", "⚠️ Pas connecté, impossible de rejoindre le groupe")
            return
        }

        socket?.emit("joinGroup", JSONObject().put("groupId", groupId))
        Log.d("GroupChatSocket", "🚪 Émission 'joinGroup': $groupId")
    }

    fun leaveGroup(groupId: String) {
        socket?.emit("leaveGroup", JSONObject().put("groupId", groupId))
        Log.d("GroupChatSocket", "🚶 Émission 'leaveGroup': $groupId")
    }

    // ✅ NOUVELLE VERSION : Émet seulement via WebSocket
    // La sauvegarde en BDD se fait via GroupsViewModel qui appelle l'API REST
    fun sendMessage(groupId: String, content: String, images: List<String> = emptyList()) {
        if (socket?.connected() != true) {
            Log.w("GroupChatSocket", "⚠️ Not connected, message not sent")
            return
        }

        Log.d("GroupChatSocket", "📨 BEFORE EMIT sendMessage: content=${content.length} chars, images=${images.size}")
        socket?.emit("sendMessage", JSONObject().apply {
            put("groupId", groupId)
            put("content", content)
            put("images", JSONArray(images))
        })
        Log.d("GroupChatSocket", "📨 AFTER EMIT sendMessage")
    }

    fun deleteMessage(groupId: String, messageId: String) {
        socket?.emit("deleteMessage", JSONObject().apply {
            put("groupId", groupId)
            put("messageId", messageId)
        })
        Log.d("GroupChatSocket", "🗑️ Émission 'deleteMessage': $messageId")
    }

    fun updateMessage(groupId: String, messageId: String, content: String) {
        socket?.emit("updateMessage", JSONObject().apply {
            put("groupId", groupId)
            put("messageId", messageId)
            put("content", content)
        })
        Log.d("GroupChatSocket", "✏️ Émission 'updateMessage': $messageId")
    }

    fun sendReaction(groupId: String, messageId: String, emoji: String) {
        if (socket?.connected() != true) {
            Log.w("GroupChatSocket", "⚠️ Not connected, reaction not sent")
            return
        }

        Log.d("GroupChatSocket", "👍 BEFORE EMIT reactToMessage: $emoji on message $messageId")
        socket?.emit("reactToMessage", JSONObject().apply {
            put("groupId", groupId)
            put("messageId", messageId)
            put("emoji", emoji)
        })
        Log.d("GroupChatSocket", "👍 AFTER EMIT reactToMessage: $emoji")
    }

    // ✅ NEW: Request group members
    fun getGroupMembers(groupId: String) {
        if (socket?.connected() != true) {
            Log.w("GroupChatSocket", "⚠️ Not connected, cannot get members")
            return
        }

        Log.d("GroupChatSocket", "👥 Requesting members for group $groupId")
        socket?.emit("getGroupMembers", JSONObject().apply {
            put("groupId", groupId)
        })
    }

    // ✅ NEW: Request reactions by emoji
    fun getReactionsByEmoji(messageId: String, emoji: String) {
        if (socket?.connected() != true) {
            Log.w("GroupChatSocket", "⚠️ Not connected, cannot get reactions")
            return
        }

        Log.d("GroupChatSocket", "😊 Requesting reactions for emoji $emoji on message $messageId")
        socket?.emit("getReactionsByEmoji", JSONObject().apply {
            put("messageId", messageId)
            put("emoji", emoji)
        })
    }

    fun sendTypingIndicator(groupId: String, isTyping: Boolean) {
        socket?.emit("typing", JSONObject().apply {
            put("groupId", groupId)
            put("isTyping", isTyping)
        })
    }

    fun resetNewMessage() {
        _newMessage.value = null
    }

    fun resetMessageDeleted() {
        _messageDeleted.value = null
    }

    fun resetMessageUpdated() {
        _messageUpdated.value = null
    }

    fun resetMessageReacted() {
        _messageReacted.value = null
    }

    // ✅ NEW: Reset functions
    fun resetGroupMembers() {
        _groupMembers.value = emptyList()
    }

    fun resetReactionsByEmoji() {
        _reactionsByEmoji.value = emptyList()
    }

    fun clearError() {
        _error.value = null
    }

    fun disconnect() {
        // ✅ CLEAN UP ALL LISTENERS BEFORE DISCONNECT
        socket?.apply {
            off(Socket.EVENT_CONNECT)
            off(Socket.EVENT_DISCONNECT)
            off(Socket.EVENT_CONNECT_ERROR)
            off("connected")
            off("joinedGroup")
            off("leftGroup")
            off("newMessage")
            off("messageDeleted")
            off("messageUpdated")
            off("messageReacted")
            off("userTyping")
            off("error")
            off("groupMembers")
            off("reactionsByEmoji")
        }
        
        socket?.disconnect()
        socket?.off()
        socket = null
        _connectionState.value = false
        _typingUsers.value = emptySet()
        _groupMembers.value = emptyList()
        _reactionsByEmoji.value = emptyList()
        isListenersSetup = false  // ✅ RESET FLAG
        Log.d("GroupChatSocket", "🔌 WebSocket disconnected and listeners cleaned")
    }
}