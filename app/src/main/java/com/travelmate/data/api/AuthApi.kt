package com.travelmate.data.api

import com.travelmate.data.models.AgencyRegistrationRequest
import com.travelmate.data.models.AuthResponse
import com.travelmate.data.models.ForgotPasswordRequest
import com.travelmate.data.models.MessageResponse
import com.travelmate.data.models.LoginRequest
import com.travelmate.data.models.UserRegistrationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    
    @POST("auth/signup")
    suspend fun registerUser(
        @Body request: UserRegistrationRequest
    ): Response<AuthResponse>
    
    @POST("auth/signup/agency")
    suspend fun registerAgency(
        @Body request: AgencyRegistrationRequest
    ): Response<AuthResponse>
    
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>
    
    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<MessageResponse>
    
    @GET("api/users/profile")
    suspend fun getUserProfile(
        @Header("Authorization") token: String
    ): Response<Any>
}
