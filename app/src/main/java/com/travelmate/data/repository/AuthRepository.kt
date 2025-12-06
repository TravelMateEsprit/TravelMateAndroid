package com.travelmate.data.repository

import com.travelmate.data.api.AuthApi
import com.travelmate.data.models.AgencyRegistrationRequest
import com.travelmate.data.models.AuthResponse
import com.travelmate.data.models.ForgotPasswordRequest
import com.travelmate.data.models.MessageResponse
import com.travelmate.data.models.LoginRequest
import com.travelmate.data.models.UserRegistrationRequest
import javax.inject.Inject
import javax.inject.Singleton

import com.travelmate.data.models.UpdateAgencyProfileRequest
import com.travelmate.data.models.UpdateProfileRequest
import com.travelmate.data.models.User

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi
) {
    
    suspend fun getUserProfile(token: String): Result<User> {
        return try {
            val response = authApi.getUserProfile("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(token: String, request: UpdateProfileRequest): Result<User> {
        return try {
            val response = authApi.updateUserProfile("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to update profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAgencyProfile(token: String): Result<User> {
        return try {
            val response = authApi.getAgencyProfile("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch agency profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAgencyProfile(token: String, request: UpdateAgencyProfileRequest): Result<User> {
        return try {
            val response = authApi.updateAgencyProfile("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to update agency profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerUser(request: UserRegistrationRequest): Result<AuthResponse> {
        return try {
            val response = authApi.registerUser(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun registerAgency(request: AgencyRegistrationRequest): Result<AuthResponse> {
        return try {
            val response = authApi.registerAgency(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val request = LoginRequest(email, password)
            val response = authApi.login(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun forgotPassword(email: String): Result<MessageResponse> {
        return try {
            val request = ForgotPasswordRequest(email)
            val response = authApi.forgotPassword(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: response.message() ?: "Failed to send reset email"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
