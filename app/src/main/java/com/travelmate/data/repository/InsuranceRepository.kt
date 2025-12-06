package com.travelmate.data.repository

import com.travelmate.data.api.InsuranceApi
import com.travelmate.data.models.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsuranceRepository @Inject constructor(
    private val insuranceApi: InsuranceApi
) {
    
    suspend fun getAllInsurances(token: String): Result<List<Insurance>> {
        return try {
            val response = insuranceApi.getAllInsurances()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch insurances"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchInsurances(
        token: String,
        searchRequest: SearchInsuranceRequest
    ): Result<SearchInsuranceResponse> {
        return try {
            val response = insuranceApi.searchInsurances(
                searchTerm = searchRequest.searchTerm,
                minPrice = searchRequest.minPrice,
                maxPrice = searchRequest.maxPrice,
                duration = searchRequest.duration,
                coverage = searchRequest.coverage,
                agencyName = searchRequest.agencyName,
                city = searchRequest.city,
                country = searchRequest.country,
                isActive = searchRequest.isActive,
                sortBy = searchRequest.sortBy,
                sortOrder = searchRequest.sortOrder,
                limit = searchRequest.limit,
                page = searchRequest.page
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to search insurances"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMySubscriptions(token: String): Result<List<Insurance>> {
        return try {
            val response = insuranceApi.getMySubscriptions()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch subscriptions"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun subscribeToInsurance(token: String, insuranceId: String): Result<Insurance> {
        return try {
            val response = insuranceApi.subscribeToInsurance(insuranceId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to subscribe"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun unsubscribeFromInsurance(token: String, insuranceId: String): Result<Insurance> {
        return try {
            val response = insuranceApi.unsubscribeFromInsurance(insuranceId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to unsubscribe"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== Agency endpoints ==========
    
    suspend fun createInsurance(token: String, request: CreateInsuranceRequest): Result<Insurance> {
        return try {
            val response = insuranceApi.createInsurance(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to create insurance"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMyInsurances(token: String): Result<List<Insurance>> {
        return try {
            val response = insuranceApi.getMyInsurances()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch my insurances"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateInsurance(
        token: String,
        insuranceId: String,
        request: UpdateInsuranceRequest
    ): Result<Insurance> {
        return try {
            val response = insuranceApi.updateInsurance(insuranceId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to update insurance"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteInsurance(token: String, insuranceId: String): Result<Unit> {
        return try {
            val response = insuranceApi.deleteInsurance(insuranceId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to delete insurance"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getInsuranceSubscribers(
        token: String,
        insuranceId: String
    ): Result<InsuranceSubscribersResponse> {
        return try {
            val response = insuranceApi.getInsuranceSubscribers(insuranceId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch subscribers"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
