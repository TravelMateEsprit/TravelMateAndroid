package com.travelmate.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Group(
    @SerialName("_id")
    val _id: String,

    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String,

    @SerialName("image")
    val image: String? = null,

    @SerialName("destination")
    val destination: String? = null,

    @SerialName("status")
    val status: String = "active",

    @SerialName("createdBy")
    val createdBy: String? = null,

    @SerialName("members")
    val members: List<String> = emptyList(),

    @SerialName("createdAt")
    val createdAt: String? = null,

    @SerialName("updatedAt")
    val updatedAt: String? = null,

    @SerialName("__v")
    val version: Int? = null,

    @SerialName("membershipStatus")
    val membershipStatus: String? = null
) {
    @Transient
    var memberCount: Int = members.size

    @Transient
    var isUserMember: Boolean = false

    fun isActive() = status.equals("active", ignoreCase = true)
}