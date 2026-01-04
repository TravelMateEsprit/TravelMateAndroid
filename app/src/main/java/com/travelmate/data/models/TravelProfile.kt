package com.travelmate.data.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class TravelFrequency {
    @SerialName("rare")
    @SerializedName("rare")
    RARE,           // 1-2 fois par an
    
    @SerialName("occasional")
    @SerializedName("occasional")
    OCCASIONAL,     // 3-5 fois par an
    
    @SerialName("frequent")
    @SerializedName("frequent")
    FREQUENT        // 6+ fois par an
}

@Serializable
enum class BudgetRange {
    @SerialName("budget")
    @SerializedName("budget")
    BUDGET,         // Économique
    
    @SerialName("medium")
    @SerializedName("medium")
    MEDIUM,         // Moyen
    
    @SerialName("premium")
    @SerializedName("premium")
    PREMIUM         // Premium/Luxe
}

@Serializable
enum class TravelPurpose {
    @SerialName("leisure")
    @SerializedName("leisure")
    LEISURE,        // Loisirs
    
    @SerialName("business")
    @SerializedName("business")
    BUSINESS,       // Affaires
    
    @SerialName("both")
    @SerializedName("both")
    BOTH            // Les deux
}

@Serializable
enum class CompanionType {
    @SerialName("solo")
    @SerializedName("solo")
    SOLO,           // Seul
    
    @SerialName("couple")
    @SerializedName("couple")
    COUPLE,         // En couple
    
    @SerialName("family")
    @SerializedName("family")
    FAMILY,         // En famille
    
    @SerialName("group")
    @SerializedName("group")
    GROUP           // En groupe
}

@Serializable
data class TravelProfile(
    val age: Int? = null,
    val travelFrequency: TravelFrequency? = null,
    val preferredDestinations: List<String>? = null,
    val budgetRange: BudgetRange? = null,
    val travelPurpose: TravelPurpose? = null,
    val companionType: CompanionType? = null,
    val hasHealthConditions: Boolean? = null
)
