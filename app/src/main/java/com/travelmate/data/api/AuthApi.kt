package com.travelmate.data.api

import com.travelmate.data.models.AgencyRegistrationRequest
import com.travelmate.data.models.AuthResponse
import com.travelmate.data.models.ForgotPasswordRequest
import com.travelmate.data.models.MessageResponse
import com.travelmate.data.models.LoginRequest
import com.travelmate.data.models.UserRegistrationRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

import com.travelmate.data.models.UpdateAgencyProfileRequest
import com.travelmate.data.models.UpdateProfileRequest
import com.travelmate.data.models.User

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
    
    @PUT("auth/reset-password")
    suspend fun resetPassword(
        @Body request: Map<String, String>
    ): Response<MessageResponse>
    
    @GET("auth/profile")
    suspend fun getUserProfile(): Response<User>

    @PUT("auth/profile")
    suspend fun updateUserProfile(
        @Body request: UpdateProfileRequest
    ): Response<User>

    @GET("agencies/profile")
    suspend fun getAgencyProfile(): Response<User>

    @PUT("agencies/profile")
    suspend fun updateAgencyProfile(
        @Body request: UpdateAgencyProfileRequest
    ): Response<User>

    @Multipart
    @PUT("auth/agency/upload-signature")
    suspend fun uploadSignature(
        @Part signature: MultipartBody.Part
    ): Response<MessageResponse>

    @PUT("auth/agency/update-signature-name")
    suspend fun updateSignatureName(
        @Body body: Map<String, String>
    ): Response<MessageResponse>
}
