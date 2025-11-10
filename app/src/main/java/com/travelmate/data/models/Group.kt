package com.travelmate.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val _id: String,
    val name: String,
    val description: String,
    val image: String? = null,
    val destination: String? = null,

    @SerialName("status")
    val status: String = "active",  // Utiliser String au lieu d'enum

    val createdBy: String? = null,  // Rendre optionnel
    val members: List<String> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null,

    @SerialName("__v")
    val version: Int? = null,  // Ignorer le champ __v de MongoDB

    // Champs calculés/UI
    var memberCount: Int = members.size,
    var isUserMember: Boolean = false
) {
    // Helper pour vérifier si actif
    fun isActive() = status.equals("active", ignoreCase = true)
}