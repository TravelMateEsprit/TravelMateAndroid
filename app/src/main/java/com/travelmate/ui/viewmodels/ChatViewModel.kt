package com.travelmate.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.data.models.ChatMessage
import com.travelmate.data.models.ChatConversation
import com.travelmate.data.socket.SocketService
import com.travelmate.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val socketService: SocketService,
    private val userPreferences: UserPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentAgencyId: String? = null
    private var currentPackId: String? = null
    private var currentAgencyName: String? = null

    private val conversationsPrefs: SharedPreferences = 
        context.getSharedPreferences("chat_conversations", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    init {
        // Ensure WebSocket is connected when the ViewModel starts
        try {
            socketService.connect()
        } catch (e: Exception) {
            android.util.Log.e("ChatViewModel", "Failed to connect socket: ${e.message}", e)
        }

        // Listen for incoming messages via WebSocket
        viewModelScope.launch {
            socketService.incomingMessages.collect { message ->
                message?.let { msg ->
                    val userId = userPreferences.getUserId()
                    if (msg.receiverId == userId) {
                        _messages.value = _messages.value + msg
                        // Save message to storage
                        saveMessageToStorage(msg)
                        // Update conversation when receiving a message
                        updateConversationFromMessage(msg, isFromMe = false)
                    }
                }
            }
        }
    }

    /**
     * Load conversation with agency
     */
    fun loadConversation(agencyId: String, packId: String?, agencyName: String = "Agence") {
        viewModelScope.launch {
            try {
                // Validate agencyId
                if (agencyId.isEmpty()) {
                    android.util.Log.e("ChatViewModel", "Cannot load conversation: agencyId is empty")
                    _error.value = "ID d'agence invalide"
                    return@launch
                }
                
                _isLoading.value = true
                _error.value = null
                
                currentAgencyId = agencyId
                currentPackId = packId
                currentAgencyName = agencyName
                
                // Load conversation history from local storage
                _messages.value = loadMessagesFromStorage(agencyId, packId)
                
                // Join conversation room via WebSocket (non-blocking)
                try {
                    socketService.joinConversation(agencyId, packId)
                } catch (e: Exception) {
                    android.util.Log.w("ChatViewModel", "Failed to join conversation via WebSocket: ${e.message}")
                    // Don't fail the whole operation if WebSocket fails
                }

                // Create or update conversation record even if no messages exist yet
                createOrUpdateConversation(agencyId, packId, agencyName)
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Error loading conversation: ${e.message}", e)
                _error.value = e.message ?: "Erreur lors du chargement"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Send a message
     */
    fun sendMessage(receiverId: String, messageText: String, packId: String?) {
        viewModelScope.launch {
            try {
                val senderId = userPreferences.getUserId() ?: return@launch
                
                val message = ChatMessage(
                    senderId = senderId,
                    receiverId = receiverId,
                    message = messageText,
                    packId = packId,
                    timestamp = System.currentTimeMillis()
                )

                // Send via WebSocket
                socketService.sendMessage(message)
                
                // Add to local messages immediately
                _messages.value = _messages.value + message
                
                // Save message to storage
                saveMessageToStorage(message)
                
                // Update or create conversation with agency name
                // Use current agency name if available, otherwise try to get from existing conversation
                val agencyName = currentAgencyName ?: getAgencyNameFromConversations(receiverId, packId)
                updateConversationFromMessage(message, isFromMe = true, agencyName = agencyName)
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de l'envoi"
            }
        }
    }

    /**
     * Get agency name from existing conversations
     */
    private fun getAgencyNameFromConversations(agencyId: String, packId: String?): String? {
        val conversations = loadConversationsFromStorage()
        return conversations.find { 
            it.agencyId == agencyId && it.packId == packId 
        }?.agencyName
    }

    /**
     * Check if message is from current user
     */
    fun isMessageFromMe(message: ChatMessage): Boolean {
        return message.senderId == userPreferences.getUserId()
    }

    /**
     * Load all conversations for the current user
     * Only shows conversations that have messages
     */
    fun loadConversations() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // Load conversations from local storage
                _conversations.value = loadConversationsFromStorage()
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors du chargement"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Create or update conversation when joining (even without messages)
     */
    private fun createOrUpdateConversation(agencyId: String, packId: String?, agencyName: String) {
        try {
            val userId = userPreferences.getUserId() ?: return
            if (agencyId.isEmpty()) {
                android.util.Log.w("ChatViewModel", "Cannot create conversation: agencyId is empty")
                return
            }

            // Check if conversation already exists
            val existingConversations = loadConversationsFromStorage()
            val existingConv = existingConversations.find {
                it.agencyId == agencyId && it.packId == packId
            }

            if (existingConv != null) {
                // Conversation already exists, no need to create
                return
            }

            // Create new conversation
            val conversation = ChatConversation(
                id = "${userId}_${agencyId}_${packId ?: "general"}",
                userId = userId,
                userName = userPreferences.getUserName() ?: "Utilisateur",
                agencyId = agencyId,
                agencyName = agencyName,
                packId = packId,
                lastMessage = null, // No messages yet
                lastMessageTime = System.currentTimeMillis(),
                unreadCount = 0
            )

            saveConversationToStorage(conversation)

            // Update the conversations list
            val updatedConversations = loadConversationsFromStorage()
            _conversations.value = updatedConversations

            android.util.Log.d("ChatViewModel", "Created new conversation: ${conversation.id}")
        } catch (e: Exception) {
            android.util.Log.e("ChatViewModel", "Error creating conversation: ${e.message}", e)
        }
    }

    /**
     * Update or create conversation from a message
     */
    private fun updateConversationFromMessage(
        message: ChatMessage,
        isFromMe: Boolean,
        agencyName: String? = null
    ) {
        try {
            val userId = userPreferences.getUserId() ?: return
            val agencyId = if (isFromMe) message.receiverId else message.senderId
            
            // Validate agencyId
            if (agencyId.isEmpty()) {
                android.util.Log.w("ChatViewModel", "Cannot update conversation: agencyId is empty")
                return
            }
            
            // Get agency name from existing conversation or use provided name
            val existingConversations = loadConversationsFromStorage()
            val existingConv = existingConversations.find {
                it.agencyId == agencyId && it.packId == message.packId
            }
            val finalAgencyName = agencyName ?: existingConv?.agencyName ?: currentAgencyName ?: "Agence"
            
            val conversation = ChatConversation(
                id = existingConv?.id ?: "${userId}_${agencyId}_${message.packId ?: "general"}",
                userId = userId,
                userName = userPreferences.getUserName() ?: "Utilisateur",
                agencyId = agencyId,
                agencyName = finalAgencyName,
                packId = message.packId,
                lastMessage = message.message,
                lastMessageTime = message.timestamp,
                unreadCount = if (isFromMe) 0 else (existingConv?.unreadCount ?: 0) + 1
            )
            
            saveConversationToStorage(conversation)
            
            // Update the conversations list
            val updatedConversations = loadConversationsFromStorage()
            _conversations.value = updatedConversations
        } catch (e: Exception) {
            android.util.Log.e("ChatViewModel", "Error updating conversation from message: ${e.message}", e)
        }
    }

    /**
     * Save conversation to local storage
     */
    private fun saveConversationToStorage(conversation: ChatConversation) {
        try {
            // Validate conversation data
            if (conversation.agencyId.isEmpty()) {
                android.util.Log.w("ChatViewModel", "Cannot save conversation: agencyId is empty")
                return
            }
            
            val key = "conv_${conversation.agencyId}_${conversation.packId ?: "general"}"
            val jsonString = json.encodeToString(ChatConversation.serializer(), conversation)
            conversationsPrefs.edit().putString(key, jsonString).apply()
        } catch (e: Exception) {
            android.util.Log.e("ChatViewModel", "Error saving conversation: ${e.message}", e)
        }
    }

    /**
     * Load all conversations from local storage
     * Only returns conversations that have messages
     */
    private fun loadConversationsFromStorage(): List<ChatConversation> {
        val conversations = mutableListOf<ChatConversation>()
        try {
            val allEntries = conversationsPrefs.all
            for ((key, value) in allEntries) {
                if (key.startsWith("conv_") && value is String) {
                    try {
                        val conversation = json.decodeFromString(ChatConversation.serializer(), value)
                        // Only include conversations that have a last message (meaning messages were exchanged)
                        if (conversation.lastMessage != null && conversation.lastMessageTime > 0) {
                            conversations.add(conversation)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ChatViewModel", "Error parsing conversation: ${e.message}", e)
                    }
                }
            }
            // Sort by last message time (most recent first)
            return conversations.sortedByDescending { it.lastMessageTime }
        } catch (e: Exception) {
            android.util.Log.e("ChatViewModel", "Error loading conversations: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Save message to local storage
     */
    private fun saveMessageToStorage(message: ChatMessage) {
        try {
            val agencyId = if (message.senderId == userPreferences.getUserId()) {
                message.receiverId
            } else {
                message.senderId
            }
            
            // Validate agencyId
            if (agencyId.isEmpty()) {
                android.util.Log.w("ChatViewModel", "Cannot save message: agencyId is empty")
                return
            }
            
            val key = "msg_${agencyId}_${message.packId ?: "general"}"
            val existingMessages = loadMessagesFromStorage(agencyId, message.packId).toMutableList()
            existingMessages.add(message)
            
            // Keep only last 100 messages per conversation
            val messagesToSave = existingMessages.takeLast(100)
            val jsonString = json.encodeToString(serializer<List<ChatMessage>>(), messagesToSave)
            conversationsPrefs.edit().putString(key, jsonString).apply()
        } catch (e: Exception) {
            android.util.Log.e("ChatViewModel", "Error saving message: ${e.message}", e)
        }
    }

    /**
     * Load messages from local storage
     */
    private fun loadMessagesFromStorage(agencyId: String, packId: String?): List<ChatMessage> {
        try {
            if (agencyId.isEmpty()) {
                android.util.Log.w("ChatViewModel", "Cannot load messages: agencyId is empty")
                return emptyList()
            }
            
            val key = "msg_${agencyId}_${packId ?: "general"}"
            val jsonString = conversationsPrefs.getString(key, null) ?: return emptyList()
            
            if (jsonString.isEmpty()) {
                return emptyList()
            }
            
            return json.decodeFromString(serializer<List<ChatMessage>>(), jsonString)
        } catch (e: Exception) {
            android.util.Log.e("ChatViewModel", "Error loading messages: ${e.message}", e)
            // Return empty list instead of crashing
            return emptyList()
        }
    }

    /**
     * Clear error
     */
    fun clearError() {
        _error.value = null
    }
}

