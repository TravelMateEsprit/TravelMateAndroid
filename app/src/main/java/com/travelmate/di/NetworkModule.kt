package com.travelmate.di

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.travelmate.data.api.AuthApi
import com.travelmate.data.api.FavoritesApi
import com.travelmate.data.api.InsuranceApi
import com.travelmate.data.api.OffersApi
import com.travelmate.data.api.PacksApi
import com.travelmate.data.api.ReservationsApi
import com.travelmate.data.api.UserApi
import com.travelmate.data.repository.FavoritesRepository
import com.travelmate.data.repository.PacksRepository
import com.travelmate.data.repository.ReservationsRepository
import com.travelmate.data.socket.SocketConfig
import com.travelmate.utils.UserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
        explicitNulls = false
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
            .readTimeout(90, TimeUnit.SECONDS)
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
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideInsuranceApi(retrofit: Retrofit): InsuranceApi =
        retrofit.create(InsuranceApi::class.java)

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)

    @Provides
    @Singleton
    fun provideOffersApi(retrofit: Retrofit): OffersApi =
        retrofit.create(OffersApi::class.java)

    @Provides
    @Singleton
    fun providePacksApi(retrofit: Retrofit): PacksApi =
        retrofit.create(PacksApi::class.java)

    @Provides
    @Singleton
    fun provideReservationsApi(retrofit: Retrofit): ReservationsApi =
        retrofit.create(ReservationsApi::class.java)

    @Provides
    @Singleton
    fun provideFavoritesApi(retrofit: Retrofit): FavoritesApi =
        retrofit.create(FavoritesApi::class.java)

    @Provides
    @Singleton
    fun providePacksRepository(
        api: PacksApi,
        userPreferences: UserPreferences,
        @ApplicationContext context: Context
    ): PacksRepository =
        PacksRepository(api, userPreferences, context)

    @Provides
    @Singleton
    fun provideReservationsRepository(
        api: ReservationsApi,
        userPreferences: UserPreferences
    ): ReservationsRepository = ReservationsRepository(api, userPreferences)

    @Provides
    @Singleton
    fun provideFavoritesRepository(
        api: FavoritesApi,
        userPreferences: UserPreferences
    ): FavoritesRepository = FavoritesRepository(api, userPreferences)
}