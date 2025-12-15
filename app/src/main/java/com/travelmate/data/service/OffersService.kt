package com.travelmate.data.service

import android.util.Log
import com.travelmate.data.api.OffersApi
import com.travelmate.data.models.FlightOffer
import com.travelmate.data.models.Preferences
import com.travelmate.data.models.RecommendationsRequest
import com.travelmate.data.models.RecommendedOffer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OffersService @Inject constructor(
    private val offersApi: OffersApi,
    private val json: Json
) {
    private val _offers = MutableStateFlow<List<FlightOffer>>(emptyList())
    val offers: StateFlow<List<FlightOffer>> = _offers.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _recommendations = MutableStateFlow<List<RecommendedOffer>>(emptyList())
    val recommendations: StateFlow<List<RecommendedOffer>> = _recommendations.asStateFlow()
    
    private val _isLoadingRecommendations = MutableStateFlow(false)
    val isLoadingRecommendations: StateFlow<Boolean> = _isLoadingRecommendations.asStateFlow()
    
    /**
     * Get all offers without filters
     */
    suspend fun getAllOffers(): Result<List<FlightOffer>> {
        return getOffers()
    }
    
    /**
     * Get offers with search, filter, and sort parameters
     */
    suspend fun getOffers(
        q: String? = null,
        type: String? = null,
        from: String? = null,
        to: String? = null,
        direct: Boolean? = null,
        date_depart: String? = null,
        date_return: String? = null,
        sort: String? = null
    ): Result<List<FlightOffer>> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            Log.d("OffersService", "=== GET OFFERS ===")
            Log.d("OffersService", "q=$q, type=$type, from=$from, to=$to, direct=$direct, date_depart=$date_depart, date_return=$date_return, sort=$sort")
            
            val response = offersApi.getOffers(
                q = q,
                type = type,
                from = from,
                to = to,
                direct = direct,
                date_depart = date_depart,
                date_return = date_return,
                sort = sort
            )
            
            Log.d("OffersService", "Response code: ${response.code()}")
            Log.d("OffersService", "Response message: ${response.message()}")
            Log.d("OffersService", "Is successful: ${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("OffersService", "Response body is null: ${body == null}")
                
                if (body != null) {
                    Log.d("OffersService", "Received ${body.size} offers")
                    body.forEachIndexed { index, offer ->
                        val fromAirport = offer.getFromAirport()
                        val toAirport = offer.getToAirport()
                        Log.d("OffersService", "[$index] ${offer.airline} - ${fromAirport.code} to ${toAirport.code} - ${offer.getFormattedPrice()}")
                    }
                    
                    _offers.value = body
                    Result.success(body)
                } else {
                    val errorMsg = "Response body is null"
                    Log.e("OffersService", errorMsg)
                    _error.value = errorMsg
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                Log.e("OffersService", errorMsg)
                Log.e("OffersService", "Error body: $errorBody")
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Exception: ${e.javaClass.simpleName} - ${e.message}"
            Log.e("OffersService", errorMsg, e)
            e.printStackTrace()
            _error.value = errorMsg
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Example: Get round-trips from Tunis to Paris on a specific date
     */
    suspend fun getRoundTripsFromTunisToParis(dateDepart: String, dateReturn: String): Result<List<FlightOffer>> {
        return getOffers(
            type = "aller-retour",
            from = "TUN",
            to = "ORY",
            date_depart = dateDepart,
            date_return = dateReturn
        )
    }
    
    /**
     * Example: Search with keyword, filter, and sort
     * e.g., "q=paris&type=aller-retour&direct=true&sort=price"
     */
    suspend fun searchOffers(
        query: String,
        type: String? = null,
        directOnly: Boolean? = null,
        sortBy: String? = null
    ): Result<List<FlightOffer>> {
        return getOffers(
            q = query,
            type = type,
            direct = directOnly,
            sort = sortBy
        )
    }
    
    /**
     * Get AI-powered recommendations based on user preferences
     */
    suspend fun getRecommendations(preferences: Preferences): Result<List<RecommendedOffer>> {
        return try {
            _isLoadingRecommendations.value = true
            _error.value = null
            
            Log.d("OffersService", "=== GET RECOMMENDATIONS ===")
            Log.d("OffersService", "Preferences: tripType=${preferences.tripType}, countryOrCity=${preferences.countryOrCity}, maxBudget=${preferences.maxBudget}, directOnly=${preferences.directOnly}")
            
            val request = RecommendationsRequest(preferences)
            val response = offersApi.getRecommendations(request)
            
            Log.d("OffersService", "Response code: ${response.code()}")
            Log.d("OffersService", "Response message: ${response.message()}")
            Log.d("OffersService", "Is successful: ${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val jsonElement = response.body()
                Log.d("OffersService", "Response body is null: ${jsonElement == null}")
                
                if (jsonElement != null) {
                    Log.d("OffersService", "Response type: ${jsonElement::class.simpleName}")
                    Log.d("OffersService", "Response content: $jsonElement")
                    
                    val recommendations = try {
                        val parsedRecommendations = when {
                            // If it's an array directly (expected format from backend)
                            jsonElement is JsonArray -> {
                                Log.d("OffersService", "Parsing as array (${jsonElement.size} items)")
                                jsonElement.mapNotNull { item ->
                                    try {
                                        json.decodeFromJsonElement(serializer<RecommendedOffer>(), item)
                                    } catch (e: Exception) {
                                        Log.e("OffersService", "Failed to parse recommendation item: ${e.message}")
                                        null
                                    }
                                }
                            }
                            // If it's an object with "recommendations" field
                            jsonElement is JsonObject && jsonElement.containsKey("recommendations") -> {
                                val recommendationsArray = jsonElement["recommendations"]?.jsonArray
                                if (recommendationsArray != null) {
                                    Log.d("OffersService", "Parsing as object with recommendations field (${recommendationsArray.size} items)")
                                    recommendationsArray.mapNotNull { item ->
                                        try {
                                            json.decodeFromJsonElement(serializer<RecommendedOffer>(), item)
                                        } catch (e: Exception) {
                                            Log.e("OffersService", "Failed to parse recommendation item: ${e.message}")
                                            null
                                        }
                                    }
                                } else {
                                    emptyList()
                                }
                            }
                            // If it's an object, try to parse directly
                            jsonElement is JsonObject -> {
                                Log.d("OffersService", "Trying to parse object directly")
                                try {
                                    listOf(json.decodeFromJsonElement(serializer<RecommendedOffer>(), jsonElement))
                                } catch (e: Exception) {
                                    Log.e("OffersService", "Failed to parse object directly: ${e.message}")
                                    emptyList()
                                }
                            }
                            else -> {
                                Log.e("OffersService", "Unexpected JSON structure")
                                emptyList()
                            }
                        }
                        
                        // Now populate the actual FlightOffer objects from the offers list using offerId
                        val offersList = _offers.value
                        parsedRecommendations.map { recommendation ->
                            val matchingOffer = offersList.find { offer ->
                                offer.getIdValue() == recommendation.offerId || 
                                offer.id == recommendation.offerId ||
                                offer._id == recommendation.offerId
                            }
                            if (matchingOffer != null) {
                                // Set the flightOffer property
                                recommendation.flightOffer = matchingOffer
                                Log.d("OffersService", "Found matching offer for ID: ${recommendation.offerId}")
                            } else {
                                Log.w("OffersService", "Could not find offer with ID: ${recommendation.offerId}")
                            }
                            recommendation
                        }
                    } catch (e: Exception) {
                        Log.e("OffersService", "Error parsing recommendations: ${e.message}", e)
                        _error.value = "Failed to parse recommendations: ${e.message}"
                        return Result.failure(e)
                    }
                    
                    if (recommendations.isNotEmpty()) {
                        Log.d("OffersService", "Successfully parsed ${recommendations.size} recommendations")
                        _recommendations.value = recommendations
                        return Result.success(recommendations)
                    } else {
                        val errorMsg = "No recommendations found in response"
                        Log.e("OffersService", errorMsg)
                        _error.value = errorMsg
                        return Result.failure(Exception(errorMsg))
                    }
                } else {
                    val errorMsg = "Response body is null"
                    Log.e("OffersService", errorMsg)
                    _error.value = errorMsg
                    return Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                Log.e("OffersService", errorMsg)
                Log.e("OffersService", "Error body: $errorBody")
                _error.value = errorMsg
                return Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = when {
                e is java.net.SocketTimeoutException -> {
                    "La requête a pris trop de temps. Veuillez réessayer. (L'IA peut prendre quelques instants)"
                }
                e.message?.contains("timeout", ignoreCase = true) == true -> {
                    "La requête a expiré. Veuillez réessayer."
                }
                else -> {
                    "Erreur: ${e.javaClass.simpleName} - ${e.message ?: "Erreur inconnue"}"
                }
            }
            Log.e("OffersService", errorMsg, e)
            Log.e("OffersService", "Exception stack trace:", e)
            e.printStackTrace()
            _error.value = errorMsg
            Result.failure(e)
        } finally {
            _isLoadingRecommendations.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearRecommendations() {
        _recommendations.value = emptyList()
    }
    
}

