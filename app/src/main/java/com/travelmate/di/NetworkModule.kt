package com.travelmate.di

import android.content.Context
import com.travelmate.config.AppConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.travelmate.data.api.AuthApi
import com.travelmate.data.api.ClaimApi
import com.travelmate.data.api.GroupsApi
import com.travelmate.data.api.InsuranceApi
import com.travelmate.data.api.NotificationApiService
import com.travelmate.data.api.OffersApi
import com.travelmate.data.api.ReviewApi
import com.travelmate.data.api.UserApi
import com.travelmate.data.socket.SocketConfig
import com.travelmate.utils.UserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        explicitNulls = false
        coerceInputValues = true
    }
    
    @Provides
    @Singleton
    fun provideAuthInterceptor(@ApplicationContext context: Context): Interceptor {
        return Interceptor { chain ->
            val userPreferences = UserPreferences(context)
            val token = userPreferences.getAccessToken()
            
            val request = if (token != null) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            
            chain.proceed(request)
        }
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS) // Increased for Gemini API calls
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        
        return Retrofit.Builder()
            .baseUrl(AppConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideInsuranceApi(retrofit: Retrofit): InsuranceApi {
        return retrofit.create(InsuranceApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideOffersApi(retrofit: Retrofit): OffersApi {
        return retrofit.create(OffersApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApiService {
        return retrofit.create(NotificationApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideClaimApi(retrofit: Retrofit): ClaimApi {
        return retrofit.create(ClaimApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideGroupsApi(retrofit: Retrofit): GroupsApi {
        return retrofit.create(GroupsApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideReviewApi(retrofit: Retrofit): ReviewApi {
        return retrofit.create(ReviewApi::class.java)
    }
}
