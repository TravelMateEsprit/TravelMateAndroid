package com.travelmate.data.repository

import android.content.Context
import android.util.Log
import com.travelmate.data.api.PacksApi
import com.travelmate.data.models.CreatePackRequest
import com.travelmate.data.models.Pack
import com.travelmate.data.models.UpdatePackRequest
import com.travelmate.utils.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PacksRepository @Inject constructor(
    private val api: PacksApi,
    private val userPreferences: UserPreferences,
    @ApplicationContext val context: Context
) {
    private val TAG = "PacksRepository"

    private fun getAuthToken(): String {
        val token = userPreferences.getAccessToken()
        if (token.isNullOrEmpty()) {
            Log.w(TAG, "Access token is null or empty")
            return ""
        }
        
        val cleanToken = token.removePrefix("Bearer ").trim()
        if (cleanToken.isEmpty()) {
            Log.w(TAG, "Token is empty after cleaning")
            return ""
        }
        
        return "Bearer $cleanToken"
    }

    suspend fun getAllActiveOffers(): List<Pack> {
        return try {
            val offers = api.getAllActiveOffers()
            Log.d(TAG, "Fetched ${offers.size} active offers")
            offers
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching active offers: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getAgencyPacks(): List<Pack> {
        return try {
            val agencyId = userPreferences.getUserId()?.trim().orEmpty()
            Log.d(TAG, "=== GET AGENCY PACKS ===")
            Log.d(TAG, "Current agency ID: '$agencyId'")
            Log.d(TAG, "User Type: ${userPreferences.getUserType()}")

            if (agencyId.isEmpty()) {
                Log.e(TAG, "❌ Agency ID is empty!")
                throw Exception("Agency ID is not set. Please log in again.")
            }

            val token = getAuthToken()
            if (token.isBlank() || token == "Bearer ") {
                Log.e(TAG, "❌ Missing auth token when fetching agency packs")
                throw Exception("Authentication token is missing. Please log in again.")
            }

            Log.d(TAG, "📡 Token exists: ${token.take(20)}...")

            // Prefer authenticated /offers/all endpoint so we get every pack, even inactive
            val offers = try {
                Log.d(TAG, "📡 Fetching /offers/all for agency catalog ...")
                val result = api.getAllOffers(token)
                Log.d(TAG, "✓ Successfully fetched ${result.size} offers from /offers/all")
                result
            } catch (authError: Exception) {
                Log.e(TAG, "⚠️ /offers/all failed (${authError.message})", authError)
                authError.printStackTrace()
                Log.e(TAG, "⚠️ Falling back to /offers endpoint...")
                try {
                    val result = api.getAllActiveOffers()
                    Log.d(TAG, "✓ Successfully fetched ${result.size} offers from /offers")
                    result
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Both endpoints failed: ${e.message}", e)
                    e.printStackTrace()
                    // Check if it's a deserialization error
                    if (e.message?.contains("MissingFieldException") == true || 
                        e.message?.contains("JsonDecodingException") == true ||
                        e.message?.contains("SerializationException") == true) {
                        Log.e(TAG, "❌ DESERIALIZATION ERROR: The API response structure doesn't match the Pack model")
                        Log.e(TAG, "❌ This usually means the API returns different field names or types than expected")
                        Log.e(TAG, "❌ Check: 1) Does API return 'id' or '_id'? 2) Is 'id_agence' a string or object?")
                    }
                    throw Exception("Failed to fetch offers: ${e.message}")
                }
            }

            if (offers.isEmpty()) {
                Log.w(TAG, "⚠️ API returned 0 offers (total)")
                // Return empty list but log for debugging
                Log.d(TAG, "Debug: Checking if this is expected or an error...")
                return emptyList()
            }

            Log.d(TAG, "🔍 Filtering ${offers.size} offers for agency ID: '$agencyId'")
            
            // Check if any packs have agency info - if not, the /offers endpoint doesn't include it
            val hasAgencyInfo = offers.any { it.agenceInfo != null }
            
            if (!hasAgencyInfo) {
                Log.w(TAG, "⚠️ /offers endpoint doesn't include agency info - returning all ${offers.size} packs")
                Log.w(TAG, "  Note: /offers/all endpoint failed (HTTP 500), so using public endpoint")
                Log.w(TAG, "  All packs will be shown since we can't filter by agency ID")
                return offers // Return all packs when agency info is not available
            }
            
            // Debug: Log ALL pack agency IDs to see what we're working with
            offers.forEachIndexed { index, pack ->
                val packAgencyId = (pack.agenceInfo?.id ?: pack.agenceId).trim()
                Log.d(TAG, "  Pack $index: ID=${pack.id}, AgencyID='$packAgencyId', Title='${pack.titre}', agenceInfo=${pack.agenceInfo != null}")
                if (pack.agenceInfo != null) {
                    Log.d(TAG, "    Agency details: id='${pack.agenceInfo.id}', name='${pack.agenceInfo.name}', agencyName='${pack.agenceInfo.agencyName}'")
                }
            }

            val myPacks = offers.filter { pack ->
                val packAgencyId = (pack.agenceInfo?.id ?: pack.agenceId).trim()
                val matches = packAgencyId.equals(agencyId, ignoreCase = true)
                if (!matches && packAgencyId.isNotEmpty()) {
                    Log.v(TAG, "  Pack ${pack.id} agency ID '$packAgencyId' doesn't match '$agencyId'")
                }
                matches
            }

            if (myPacks.isEmpty()) {
                Log.w(TAG, "⚠️ No packs matched agency '$agencyId'")
                Log.w(TAG, "  Total offers received: ${offers.size}")
                val allAgencyIds = offers.map { (it.agenceInfo?.id ?: it.agenceId).trim() }.distinct().filter { it.isNotEmpty() }
                Log.w(TAG, "  Available agency IDs in offers (${allAgencyIds.size} unique): $allAgencyIds")
                // If we have agency info but no matches, return empty list
                // If we don't have agency info, we already returned all packs above
                return emptyList()
            } else {
                Log.d(TAG, "✓ Returning ${myPacks.size} packs for agency '$agencyId'")
            }

            myPacks
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching agency packs: ${e.message}", e)
            e.printStackTrace()
            throw e // Re-throw to let ViewModel handle the error
        }
    }

    suspend fun getOfferById(id: String): Pack? {
        return try {
            api.getOfferById(id)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching offer $id: ${e.message}", e)
            null
        }
    }

    suspend fun createOffer(request: CreatePackRequest): Result<Pack> {
        return try {
            val userType = userPreferences.getUserType()
            val userId = userPreferences.getUserId()
            val rawToken = userPreferences.getAccessToken()
            
            Log.d(TAG, "=== CREATE PACK DEBUG ===")
            Log.d(TAG, "User Type: $userType")
            Log.d(TAG, "User ID: $userId")
            Log.d(TAG, "Raw Token exists: ${rawToken != null}")
            Log.d(TAG, "Raw Token length: ${rawToken?.length ?: 0}")
            Log.d(TAG, "Raw Token preview: ${rawToken?.take(30)}...")
            
            val normalizedUserType = userType?.lowercase()?.trim()
            if (normalizedUserType != "agence" && normalizedUserType != "agency") {
                Log.e(TAG, "User is not an agency. User type: $userType (normalized: $normalizedUserType)")
                return Result.failure(Exception("Vous devez être connecté en tant qu'agence pour créer un pack."))
            }
            
            val token = getAuthToken()
            if (token.isEmpty() || token == "Bearer ") {
                Log.e(TAG, "No authentication token found")
                return Result.failure(Exception("Non authentifié - Token manquant. Veuillez vous reconnecter."))
            }

            Log.d(TAG, "Full token format: ${token.take(50)}...")
            Log.d(TAG, "Request data: title=${request.titre}, destination=${request.destination}, price=${request.prix}")

            val agencyId = userId ?: ""
            val pack = api.createOffer(token, request, agencyId)
            Log.d(TAG, "=== PACK CREATED SUCCESSFULLY ===")
            Log.d(TAG, "Pack ID: ${pack.id}")
            Log.d(TAG, "Pack Title: ${pack.titre}")
            Log.d(TAG, "Pack Agency ID: '${pack.agenceId}'")
            Log.d(TAG, "Current User ID: '$userId'")
            Log.d(TAG, "Agency IDs match: ${pack.agenceId.equals(userId, ignoreCase = true)}")
            Result.success(pack)
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP Exception: Code=${e.code()}, Message=${e.message()}")
            val errorBody = try {
                e.response()?.errorBody()?.string()
            } catch (ex: Exception) {
                null
            }
            Log.e(TAG, "Response body: $errorBody")
            
            when (e.code()) {
                401 -> {
                    Log.e(TAG, "HTTP 401 Unauthorized - Token expired or invalid")
                    Result.failure(Exception("Session expirée. Veuillez vous reconnecter en tant qu'agence."))
                }
                403 -> {
                    Log.e(TAG, "HTTP 403 Forbidden - No permission")
                    val userType = userPreferences.getUserType()
                    val errorMsg = if (userType?.lowercase()?.trim() != "agence" && userType?.lowercase()?.trim() != "agency") {
                        "Votre compte n'est pas configuré comme agence. Veuillez vous reconnecter avec un compte agence."
                    } else {
                        "Le serveur a refusé la création du pack. Votre session a peut-être expiré. Veuillez vous reconnecter."
                    }
                    Result.failure(Exception(errorMsg))
                }
                else -> {
                    Log.e(TAG, "HTTP Error ${e.code()}: ${e.message()}")
                    Result.failure(Exception("Erreur serveur (${e.code()}): ${e.message()}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating pack: ${e.message}", e)
            e.printStackTrace()
            val errorMsg = when {
                e.message?.contains("401", ignoreCase = true) == true -> 
                    "Session expirée. Veuillez vous reconnecter."
                e.message?.contains("Unauthorized", ignoreCase = true) == true -> 
                    "Non autorisé. Veuillez vous reconnecter en tant qu'agence."
                else -> e.message ?: "Erreur lors de la création du pack"
            }
            Result.failure(Exception(errorMsg))
        }
    }

    suspend fun updateOffer(id: String, request: UpdatePackRequest): Result<Pack> {
        return try {
            val token = getAuthToken()
            if (token.isEmpty() || token == "Bearer ") {
                return Result.failure(Exception("Non authentifié"))
            }

            val pack = api.updateOffer(id, token, request)
            Log.d(TAG, "Pack updated successfully: ${pack.id}")
            Result.success(pack)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating pack $id: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteOffer(id: String): Result<Unit> {
        return try {
            val token = getAuthToken()
            if (token.isEmpty() || token == "Bearer ") {
                return Result.failure(Exception("Non authentifié"))
            }

            api.deleteOffer(id, token)
            Log.d(TAG, "Pack deleted successfully: $id")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting pack $id: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun getCurrentAgencyId(): String? = userPreferences.getUserId()
 }
