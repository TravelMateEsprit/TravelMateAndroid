package com.travelmate.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.travelmate.data.api.AuthApi
import com.travelmate.data.api.GroupsApi
import com.travelmate.data.api.InsuranceApi
import com.travelmate.data.api.UserApi
import com.travelmate.data.socket.SocketConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
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
        ignoreUnknownKeys = true      // Ignore les champs inconnus (comme __v de MongoDB)
        isLenient = true                // Permet un parsing JSON plus flexible
        encodeDefaults = false          // N'encode pas les valeurs par défaut
        explicitNulls = false           // N'inclut pas les valeurs null explicitement
        coerceInputValues = true        // ✅ AJOUTÉ : Convertit automatiquement les valeurs incompatibles
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
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
            .baseUrl(SocketConfig.SERVER_URL)
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
    fun provideGroupsApi(retrofit: Retrofit): GroupsApi {
        return retrofit.create(GroupsApi::class.java)
    }
}