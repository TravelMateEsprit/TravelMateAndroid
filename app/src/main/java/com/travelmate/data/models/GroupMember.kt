package com.travelmate.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroupMember(
    @SerialName("_id")
    val id: String = "",

    @SerialName("groupId")
    val groupId: String = "",

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("role")
    val role: String = "member",

    @SerialName("status")
    val status: String = "active",

    @SerialName("joinedAt")
    val joinedAt: String = "",

    @SerialName("leftAt")
    val leftAt: String? = null,

    @SerialName("user")
    val user: UserInfo? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("nom")
    val nom: String = "",

    @SerialName("prenom")
    val prenom: String = "",

    @SerialName("avatar")
    val avatar: String = "",

    @SerialName("email")
    val email: String = "",

    @SerialName("isCreator")
    val isCreator: Boolean = false
) {
    fun isAdmin() = role.equals("admin", ignoreCase = true)
    fun isActive() = status.equals("active", ignoreCase = true)
    fun isPending() = status.equals("pending", ignoreCase = true)
    
    val displayName: String
        get() {
            // Priorité 1: name du membre
            if (!name.isNullOrBlank()) return name
            // Priorité 2: name de user
            if (user?.name?.isNotBlank() == true) return user.name!!
            // Priorité 3: user displayName (sans email)
            val userDisplayName = user?.displayName
            if (userDisplayName != null && userDisplayName.isNotBlank() && userDisplayName != "Utilisateur" && !userDisplayName.contains("@")) {
                return userDisplayName
            }
            // Priorité 4: prenom + nom du membre
            val fullName = "${prenom ?: ""} ${nom ?: ""}".trim()
            if (fullName.isNotBlank()) return fullName
            // Priorité 5: partie avant @ de l'email
            val emailToUse = email.ifBlank { user?.email ?: "" }
            val username = com.travelmate.utils.Constants.extractUsernameFromEmail(emailToUse)
            if (username != null) return username
            // Fallback
            return "Utilisateur"
        }
}

@Serializable
data class UserInfo(
    @SerialName("_id")
    val id: String = "",

    @SerialName("name")
    val name: String? = null,

    @SerialName("nom")
    val nom: String? = null,

    @SerialName("prenom")
    val prenom: String? = null,
    
    @SerialName("firstName")
    val firstName: String? = null,
    
    @SerialName("lastName")
    val lastName: String? = null,

    @SerialName("email")
    val email: String? = null
) {
    val displayName: String
        get() {
            // Priorité 1: name
            if (!name.isNullOrBlank()) return name
            // Priorité 2: firstName + lastName
            if (!firstName.isNullOrBlank() || !lastName.isNullOrBlank()) {
                val fullName = "${firstName ?: ""} ${lastName ?: ""}".trim()
                if (fullName.isNotBlank()) return fullName
            }
            // Priorité 3: prenom + nom
            val fullName = "${prenom ?: ""} ${nom ?: ""}".trim()
            if (fullName.isNotBlank()) return fullName
            // Priorité 4: partie avant @ de l'email
            val username = com.travelmate.utils.Constants.extractUsernameFromEmail(email)
            if (username != null) return username
            // Fallback
            return "Utilisateur"
        }
}