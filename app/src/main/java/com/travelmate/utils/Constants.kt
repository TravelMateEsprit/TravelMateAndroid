package com.travelmate.utils

object Constants {
    
    // Navigation Routes
    object Routes {
        const val WELCOME = "welcome"
        const val USER_REGISTRATION = "user_registration"
        const val AGENCY_REGISTRATION = "agency_registration"
        const val LOGIN = "login"
        const val HOME = "home"
        const val USER_HOME = "user_home"
        const val AGENCY_DASHBOARD = "agency_dashboard"

        const val GROUPS = "groups"
    }
    
    // SharedPreferences Keys
    object PrefsKeys {
        const val AUTH_TOKEN = "auth_token"
        const val USER_ID = "user_id"
        const val USER_TYPE = "user_type"
        const val USER_EMAIL = "user_email"
    }
    
    // User Types
    object UserTypes {
        const val USER = "USER"
        const val AGENCY = "AGENCY"
        const val ADMIN = "ADMIN"
    }
    
    // Date Formats
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val DISPLAY_DATE_FORMAT = "dd/MM/yyyy"
    
    // Validation
    const val MIN_PASSWORD_LENGTH = 8
    const val SIRET_LENGTH = 14
    const val POSTAL_CODE_LENGTH = 5
    
    // Image URL Helper
    fun buildImageUrl(imagePath: String?): String? {
        if (imagePath.isNullOrBlank()) return null
        
        // Si c'est déjà une URL complète, retourner telle quelle
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            android.util.Log.d("Constants", "✅ Image URL complète: $imagePath")
            return imagePath
        }
        
        // Si c'est un chemin relatif, construire l'URL complète
        val baseUrl = com.travelmate.data.socket.SocketConfig.SERVER_URL.trimEnd('/')
        val path = imagePath.trimStart('/')
        
        // Gérer différents formats de chemins
        val finalPath = when {
            path.startsWith("uploads/") -> path
            path.startsWith("/uploads/") -> path.trimStart('/')
            path.contains("/") -> path // Si contient déjà un slash, utiliser tel quel
            else -> {
                // Si c'est juste un nom de fichier, essayer de deviner le type
                when {
                    path.contains("group") -> "uploads/groups/$path"
                    path.contains("message") -> "uploads/messages/$path"
                    else -> "uploads/$path"
                }
            }
        }
        
        val fullUrl = "$baseUrl/$finalPath"
        android.util.Log.d("Constants", "🔗 Image path original: $imagePath")
        android.util.Log.d("Constants", "🔗 Image URL construite: $fullUrl")
        return fullUrl
    }
    
    // Extract username from email (part before @)
    fun extractUsernameFromEmail(email: String?): String? {
        if (email.isNullOrBlank()) return null
        return email.substringBefore("@").takeIf { it.isNotBlank() }
    }
}
