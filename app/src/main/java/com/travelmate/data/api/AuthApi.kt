package com.travelmate.data.api

import com.travelmate.data.models.AgencyRegistrationRequest
import com.travelmate.data.models.AuthResponse
import com.travelmate.data.models.LoginRequest
import com.travelmate.data.models.UserRegistrationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    
    @POST("api/auth/register/user")
    suspend fun registerUser(
        @Body request: UserRegistrationRequest
    ): Response<AuthResponse>
    
    @POST("api/auth/register/agency")
    suspend fun registerAgency(
        @Body request: AgencyRegistrationRequest
    ): Response<AuthResponse>
    
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>
    
    @GET("api/users/profile")
    suspend fun getUserProfile(
        @Header("Authorization") token: String
    ): Response<Any>
}
