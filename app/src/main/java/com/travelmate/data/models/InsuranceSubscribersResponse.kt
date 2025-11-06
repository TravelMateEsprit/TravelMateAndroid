package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class InsuranceSubscribersResponse(
    val insuranceName: String,
    val subscribersCount: Int,
    val subscribers: List<User> // Liste complète d'objets User, pas d'IDs
)
