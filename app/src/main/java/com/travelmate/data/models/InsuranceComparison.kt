package com.travelmate.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CompareInsurancesRequest(
    val insuranceIds: List<String>
)

@Serializable
data class InsuranceComparisonDetail(
    val id: String,
    val name: String,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val overallScore: Double
)

@Serializable
data class BestChoice(
    val id: String,
    val name: String,
    val reason: String
)

@Serializable
data class Recommendations(
    val budgetConscious: String,
    val maxCoverage: String,
    val balanced: String
)

@Serializable
data class ComparisonResult(
    val insurances: List<InsuranceComparisonDetail>,
    val summary: String,
    val bestChoice: BestChoice,
    val keyDifferences: List<String>,
    val importantChecks: List<String>,
    val recommendations: Recommendations
)
