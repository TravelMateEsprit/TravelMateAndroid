package com.travelmate.data.socket

import com.travelmate.config.AppConfig
import io.socket.client.IO

/**
 * Configuration WebSocket
 * Utilise automatiquement AppConfig pour gérer dev/prod
 */
object SocketConfig {
    
    /**
     * URL du serveur WebSocket
     * S'adapte automatiquement selon l'environnement
     */
    val SERVER_URL: String
        get() = AppConfig.Socket.SERVER_URL
    
    /**
     * Options de configuration Socket.IO
     */
    fun getSocketOptions(): IO.Options {
        return AppConfig.Socket.getOptions()
    }
}
