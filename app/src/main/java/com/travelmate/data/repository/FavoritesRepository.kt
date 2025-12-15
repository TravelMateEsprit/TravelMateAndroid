package com.travelmate.data.repository

import android.util.Log
import com.travelmate.data.api.FavoritesApi
import com.travelmate.data.models.FavoriteDto
import com.travelmate.data.models.Pack
import com.travelmate.utils.UserPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepository @Inject constructor(
    private val api: FavoritesApi,
    private val userPreferences: UserPreferences
) {
    private val tag = "FavoritesRepository"

    private fun authHeader(): String? {
        val token = userPreferences.getAccessToken()?.removePrefix("Bearer ")?.trim()
        return token?.takeIf { it.isNotEmpty() }?.let { "Bearer $it" }
    }

    suspend fun fetchFavorites(): List<Pack> {
        val header = authHeader() ?: return emptyList()
        return runCatching {
            api.getMyFavorites(header).mapNotNull(FavoriteDto::offer)
        }.getOrElse {
            Log.e(tag, "Failed to load favorites", it)
            emptyList()
        }
    }

    suspend fun isFavorite(packId: String): Boolean {
        val header = authHeader() ?: return false
        return runCatching {
            api.isFavorite(packId, header).isFavorite
        }.getOrElse {
            Log.e(tag, "Failed to check favorite", it)
            false
        }
    }

    suspend fun addFavorite(packId: String): Result<Unit> {
        val header = authHeader() ?: return Result.failure(Exception("Non authentifié"))
        return runCatching {
            api.addToFavorites(packId, header)
        }.onFailure { Log.e(tag, "Failed to add favorite", it) }
    }

    suspend fun removeFavorite(packId: String): Result<Unit> {
        val header = authHeader() ?: return Result.failure(Exception("Non authentifié"))
        return runCatching {
            api.removeFromFavorites(packId, header)
        }.onFailure { Log.e(tag, "Failed to remove favorite", it) }
    }
}

