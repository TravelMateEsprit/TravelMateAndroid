package com.travelmate.data.service

import android.util.Log
import com.travelmate.data.api.OffersApi
import com.travelmate.data.models.FlightOffer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OffersService @Inject constructor(
    private val offersApi: OffersApi
) {
    private val _offers = MutableStateFlow<List<FlightOffer>>(emptyList())
    val offers: StateFlow<List<FlightOffer>> = _offers.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
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
    
    fun clearError() {
        _error.value = null
    }
}

