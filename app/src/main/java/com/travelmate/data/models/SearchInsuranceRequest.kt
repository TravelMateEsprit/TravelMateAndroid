package com.travelmate.data.models

data class SearchInsuranceRequest(
    val searchTerm: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val duration: String? = null,
    val coverage: String? = null,
    val agencyName: String? = null,
    val city: String? = null,
    val country: String? = null,
    val isActive: Boolean? = true,
    val sortBy: String? = "createdAt",
    val sortOrder: String? = "desc",
    val limit: Int? = 20,
    val page: Int? = 0
)
