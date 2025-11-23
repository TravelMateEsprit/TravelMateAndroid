package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Preferences(
    val tripType: String, // "aller-retour" or "aller-simple"
    val countryOrCity: String,
    val maxBudget: Int, // Always in TND (dinar tunisien)
    val directOnly: Boolean
)

