package com.travelmate.data.socket

import android.content.Context
import android.util.Log
import com.travelmate.data.models.MessageGroupe
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

    fun connect(baseUrl: String = "http://10.0.2.2:3000") {
        try {
            Log.d("GroupChatSocket", "🔍 Début connexion...")

            val prefs = context.getSharedPreferences("travelmate_prefs", Context.MODE_PRIVATE)
            val token = prefs.getString("access_token", null)

            if (token == null) {
                Log.e("GroupChatSocket", "❌ Token manquant")
                _error.value = "Token manquant"
                return
            }

            Log.d("GroupChatSocket", "✅ Token: ${token.take(50)}...")

            val options = IO.Options().apply {
                auth = mapOf("token" to token)
                path = "/groups/socket.io/"
                transports = arrayOf("polling", "websocket")
                reconnection = true
                reconnectionAttempts = 10
                reconnectionDelay = 1000
                reconnectionDelayMax = 5000
                timeout = 30000
                forceNew = true
            }

            Log.d("GroupChatSocket", "🔧 Configuration:")
            Log.d("GroupChatSocket", "   URL: $baseUrl")
            Log.d("GroupChatSocket", "   Path: /groups/socket.io/")
            Log.d("GroupChatSocket", "   Transports: polling → websocket")

            socket = IO.socket(baseUrl, options)

            if (socket == null) {
                Log.e("GroupChatSocket", "❌ Socket null après création")
                return
            }

            setupSocketListeners()

            Log.d("GroupChatSocket", "📡 Connexion lancée...")
            socket?.connect()

        } catch (e: Exception) {
            Log.e("GroupChatSocket", "❌ ERREUR FATALE", e)
            _error.value = "Erreur: ${e.message}"
        }
    }

    private fun setupSocketListeners() {
        socket?.apply {
            on(Socket.EVENT_CONNECT) {
                Log.d("GroupChatSocket", "✅✅✅ CONNECTÉ AU SERVEUR !")
                _connectionState.value = true
                _error.value = null
            }

            on(Socket.EVENT_DISCONNECT) { args ->
                val reason = args.getOrNull(0)?.toString() ?: "unknown"
                Log.d("GroupChatSocket", "❌ Déconnecté: $reason")
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

                    Log.d("GroupChatSocket", "👍 Event 'messageReacted' reçu")

                    if (msgJson != null) {
                        val msg = json.decodeFromString<MessageGroupe>(msgJson)
                        Log.d("GroupChatSocket", "✅ Réaction parsée: ${msg.id} (${msg.reactions.size} réactions)")
                        _messageReacted.value = msg
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
            Log.w("GroupChatSocket", "⚠️ Pas connecté, message non envoyé")
            return
        }

        socket?.emit("sendMessage", JSONObject().apply {
            put("groupId", groupId)
            put("content", content)
            put("images", JSONArray(images))
        })
        Log.d("GroupChatSocket", "📨 Émission 'sendMessage': $content")
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
            Log.w("GroupChatSocket", "⚠️ Pas connecté, réaction non envoyée")
            return
        }

        socket?.emit("reactToMessage", JSONObject().apply {
            put("groupId", groupId)
            put("messageId", messageId)
            put("emoji", emoji)
        })
        Log.d("GroupChatSocket", "👍 Émission 'reactToMessage': $emoji sur message $messageId")
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

    fun clearError() {
        _error.value = null
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        _connectionState.value = false
        _typingUsers.value = emptySet()
        Log.d("GroupChatSocket", "🔌 WebSocket déconnecté")
    }
}