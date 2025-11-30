package com.travelmate.data.socket

import io.socket.client.IO

object SocketConfig {
    // Pour émulateur Android
   const val SERVER_URL = "http://10.0.2.2:3000"
    //const val SERVER_URL = "http://192.168.100.37:3000"

    // Pour device physique, décommenter et remplacer par votre IP locale :
   // const val SERVER_URL = "http://192.168.100.20:3000"
    
    fun getSocketOptions(): IO.Options {
        return IO.Options.builder()
            .setTransports(arrayOf("websocket"))
            .setReconnection(true)
            .setReconnectionDelay(1000)
            .setReconnectionDelayMax(5000)
            .setReconnectionAttempts(Int.MAX_VALUE)
            .build()
    }
}
