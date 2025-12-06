package com.travelmate.config

import io.socket.client.IO

/**
 * Configuration centralisée de l'application TravelMate
 * 
 * Ce fichier gère automatiquement les URLs selon l'environnement (dev/prod)
 * Pour passer en production : changer IS_PRODUCTION = true
 */
object AppConfig {
    
    /**
     * ⚠️ CONFIGURATION ENVIRONNEMENT
     * Mettre à true pour utiliser le backend en production
     * Mettre à false pour développement local
     */
    private const val IS_PRODUCTION = false  // ⬅️ CHANGER EN true POUR PRODUCTION
    
    /**
     * URLs Backend
     */
    // URL de production (déployé sur Render)
    private const val PROD_URL = "https://backendtravelmate-2ipv.onrender.com"
    
    // URLs de développement local
    private const val DEV_EMULATOR_URL = "http://10.0.2.2:3000"  // Pour émulateur Android
    private const val DEV_PHYSICAL_URL = "http://172.17.0.1:3000"  // Pour device physique (remplacer par votre IP)
    
    // Par défaut, utiliser émulateur en développement
    private const val DEV_URL = PROD_URL
    
    /**
     * URL de base selon l'environnement
     */
    val BASE_URL: String
        get() = if (IS_PRODUCTION) PROD_URL else DEV_URL
    
    /**
     * URL de l'API REST
     */
    val API_URL: String
        get() = "$BASE_URL/api"
    
    /**
     * Timeout pour les requêtes réseau (en secondes)
     */
    const val NETWORK_TIMEOUT = 30L
    
    /**
     * Vérifier si on est en mode production
     */
    val isProduction: Boolean
        get() = IS_PRODUCTION
    
    /**
     * Configuration WebSocket
     */
    object Socket {
        /**
         * URL du serveur WebSocket
         */
        val SERVER_URL: String
            get() = BASE_URL
        
        /**
         * Délai de reconnexion (ms)
         */
        const val RECONNECT_DELAY = 1000L
        
        /**
         * Délai maximum de reconnexion (ms)
         */
        const val RECONNECT_DELAY_MAX = 5000L
        
        /**
         * Options Socket.IO
         */
        fun getOptions(): IO.Options {
            return IO.Options.builder()
                .setTransports(arrayOf("websocket"))
                .setReconnection(true)
                .setReconnectionDelay(RECONNECT_DELAY)
                .setReconnectionDelayMax(RECONNECT_DELAY_MAX)
                .setReconnectionAttempts(Int.MAX_VALUE)
                .build()
        }
    }
    
    /**
     * Configuration des uploads
     */
    object Upload {
        /**
         * Taille maximale des fichiers (MB)
         */
        const val MAX_FILE_SIZE_MB = 10
        
        /**
         * URL pour accéder aux fichiers uploadés
         */
        val UPLOAD_URL: String
            get() = "$BASE_URL/uploads"
        
        /**
         * Construire l'URL complète d'un fichier uploadé
         */
        fun getFileUrl(relativePath: String): String {
            return "$UPLOAD_URL/$relativePath"
        }
    }
    
    /**
     * Configuration des timeouts spécifiques
     */
    object Timeouts {
        const val CONNECT = 30L  // secondes
        const val READ = 30L     // secondes
        const val WRITE = 30L    // secondes
        const val UPLOAD = 60L   // secondes (pour fichiers volumineux)
    }
    
    /**
     * Logs de débogage
     */
    object Debug {
        /**
         * Activer les logs détaillés
         */
        val ENABLE_LOGS: Boolean
            get() = !IS_PRODUCTION
        
        /**
         * Tag pour Logcat
         */
        const val LOG_TAG = "TravelMate"
    }
}
