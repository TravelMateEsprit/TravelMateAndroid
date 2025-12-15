package com.travelmate.data.socket

import android.util.Log
import com.travelmate.data.models.AgencyRegistrationRequest
import com.travelmate.data.models.ChatMessage
import com.travelmate.data.models.LoginRequest
import com.travelmate.data.models.SocketErrorResponse
import com.travelmate.data.models.UserRegistrationRequest
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketService @Inject constructor() {
    private var socket: Socket? = null
    
    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState.asStateFlow()
    
    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages.asStateFlow()
    
    private val _registrationSuccess = MutableStateFlow<String?>(null)
    val registrationSuccess: StateFlow<String?> = _registrationSuccess.asStateFlow()
    
    private val _registrationError = MutableStateFlow<SocketErrorResponse?>(null)
    val registrationError: StateFlow<SocketErrorResponse?> = _registrationError.asStateFlow()
    
    private val _loginSuccess = MutableStateFlow<String?>(null)
    val loginSuccess: StateFlow<String?> = _loginSuccess.asStateFlow()
    
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()
    
    private val _incomingMessages = MutableStateFlow<ChatMessage?>(null)
    val incomingMessages: StateFlow<ChatMessage?> = _incomingMessages.asStateFlow()
    
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun connect() {
        if (socket == null || !socket!!.connected()) {
            try {
                val options = SocketConfig.getSocketOptions()
                socket = IO.socket(SocketConfig.SERVER_URL, options)
                
                socket?.apply {
                    // Événement de connexion
                    on(Socket.EVENT_CONNECT) {
                        _connectionState.value = true
                        Log.d(TAG, "Connected to server: ${SocketConfig.SERVER_URL}")
                    }
                    
                    // Événement de déconnexion
                    on(Socket.EVENT_DISCONNECT) {
                        _connectionState.value = false
                        Log.d(TAG, "Disconnected from server")
                    }
                    
                    // Événement d'erreur de connexion
                    on(Socket.EVENT_CONNECT_ERROR) { args ->
                        _connectionState.value = false
                        Log.e(TAG, "Connection error: ${args.getOrNull(0)}")
                    }
                    
                    // Événement de notification générique
                    on("notification") { args ->
                        if (args.isNotEmpty()) {
                            val message = args[0].toString()
                            _messages.value = _messages.value.plus(message)
                            Log.d(TAG, "Notification: $message")
                        }
                    }
                    
                    // Événement de bienvenue
                    on("welcome") { args ->
                        if (args.isNotEmpty()) {
                            val message = args[0].toString()
                            _messages.value = _messages.value.plus(message)
                            Log.d(TAG, "Welcome: $message")
                        }
                    }
                    
                    // Événement de succès d'inscription
                    on("registration:success") { args ->
                        if (args.isNotEmpty()) {
                            val response = args[0].toString()
                            _registrationSuccess.value = response
                            Log.d(TAG, "Registration success: $response")
                        }
                    }
                    
                    // Événement d'erreur d'inscription
                    on("registration:error") { args ->
                        if (args.isNotEmpty()) {
                            try {
                                val errorJson = args[0].toString()
                                val error = json.decodeFromString<SocketErrorResponse>(errorJson)
                                _registrationError.value = error
                                Log.e(TAG, "Registration error: ${error.message}")
                            } catch (e: Exception) {
                                val simpleError = SocketErrorResponse(args[0].toString())
                                _registrationError.value = simpleError
                                Log.e(TAG, "Registration error (parse failed): ${args[0]}")
                            }
                        }
                    }
                    
                    // Événement de succès de connexion
                    on("login:success") { args ->
                        if (args.isNotEmpty()) {
                            val response = args[0].toString()
                            _loginSuccess.value = response
                            Log.d(TAG, "Login success: $response")
                        }
                    }
                    
                    // Événement d'erreur de connexion
                    on("login:error") { args ->
                        if (args.isNotEmpty()) {
                            val error = args[0].toString()
                            _loginError.value = error
                            Log.e(TAG, "Login error: $error")
                        }
                    }
                    
                    // Événement de message reçu
                    on("message:received") { args ->
                        if (args.isNotEmpty()) {
                            try {
                                val messageJson = args[0].toString()
                                val message = json.decodeFromString<ChatMessage>(messageJson)
                                _incomingMessages.value = message
                                Log.d(TAG, "Message received: ${message.message}")
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to parse message: ${e.message}")
                            }
                        }
                    }
                    
                    connect()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create socket: ${e.message}", e)
            }
        }
    }
    
    fun emitUserRegistration(data: UserRegistrationRequest) {
        if (socket?.connected() == true) {
            val jsonData = data.toJson()
            socket?.emit("user:register", jsonData)
            Log.d(TAG, "Emitting user:register")
        } else {
            Log.e(TAG, "Cannot emit user:register - socket not connected")
        }
    }
    
    fun emitAgencyRegistration(data: AgencyRegistrationRequest) {
        if (socket?.connected() == true) {
            val jsonData = data.toJson()
            socket?.emit("agency:register", jsonData)
            Log.d(TAG, "Emitting agency:register")
        } else {
            Log.e(TAG, "Cannot emit agency:register - socket not connected")
        }
    }
    
    fun emitLogin(email: String, password: String) {
        if (socket?.connected() == true) {
            val data = LoginRequest(email, password)
            val jsonData = Json.encodeToString(LoginRequest.serializer(), data)
            socket?.emit("user:login", jsonData)
            Log.d(TAG, "Emitting user:login")
        } else {
            Log.e(TAG, "Cannot emit user:login - socket not connected")
        }
    }
    
    fun resetRegistrationState() {
        _registrationSuccess.value = null
        _registrationError.value = null
    }
    
    fun resetLoginState() {
        _loginSuccess.value = null
        _loginError.value = null
    }
    
    /**
     * Join a conversation room
     */
    fun joinConversation(receiverId: String, packId: String?) {
        if (socket?.connected() == true) {
            val roomId = if (packId != null) {
                "pack_${packId}_${receiverId}"
            } else {
                "agency_${receiverId}"
            }
            socket?.emit("join:conversation", roomId)
            Log.d(TAG, "Joining conversation: $roomId")
        } else {
            Log.e(TAG, "Cannot join conversation - socket not connected")
        }
    }

    /**
     * Send a chat message
     */
    fun sendMessage(message: ChatMessage) {
        if (socket?.connected() == true) {
            try {
                val messageJson = json.encodeToString(ChatMessage.serializer(), message)
                socket?.emit("message:send", messageJson)
                Log.d(TAG, "Sending message: ${message.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send message: ${e.message}")
            }
        } else {
            Log.e(TAG, "Cannot send message - socket not connected")
        }
    }

    /**
     * Leave a conversation room
     */
    fun leaveConversation(receiverId: String, packId: String?) {
        if (socket?.connected() == true) {
            val roomId = if (packId != null) {
                "pack_${packId}_${receiverId}"
            } else {
                "agency_${receiverId}"
            }
            socket?.emit("leave:conversation", roomId)
            Log.d(TAG, "Leaving conversation: $roomId")
        }
    }
    
    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        _connectionState.value = false
        Log.d(TAG, "Socket disconnected and cleaned up")
    }
    
    companion object {
        private const val TAG = "SocketService"
    }
}
