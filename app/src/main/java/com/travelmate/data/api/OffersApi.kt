package com.travelmate.data.api

import com.travelmate.data.models.FlightOffer
import com.travelmate.data.models.RecommendationsRequest
import kotlinx.serialization.json.JsonElement
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface OffersApi {
    
    /**
     * Get all flight offers with optional query parameters
     * 
     * @param q Search query (keyword search)
     * @param type Flight type: "aller-retour", "aller-simple", "multi-destin"
     * @param from Departure airport code (e.g., "TUN")
     * @param to Arrival airport code (e.g., "ORY")
     * @param direct Filter for direct flights only (true/false)
     * @param date_depart Departure date (format: "YYYY-MM-DD" or "2024-11-18")
     * @param date_return Return date for round trips (format: "YYYY-MM-DD" or "2024-11-25")
     * @param sort Sort by: "price", "duration", "departure_time"
     * @param origin Amadeus origin airport code (IATA code like "TUN")
     * @param destination Amadeus destination airport code (IATA code like "FCO")
     * @param departureDate Amadeus departure date (format: "YYYY-MM-DD")
     * @param returnDate Amadeus return date for round trips (format: "YYYY-MM-DD")
     * @param adults Number of adults for Amadeus search (default: 1)
     */
    @GET("offers")
    suspend fun getOffers(
        @Query("q") q: String? = null,
        @Query("type") type: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("direct") direct: Boolean? = null,
        @Query("date_depart") date_depart: String? = null,
        @Query("date_return") date_return: String? = null,
        @Query("sort") sort: String? = null,
        @Query("origin") origin: String? = null,
        @Query("destination") destination: String? = null,
        @Query("departureDate") departureDate: String? = null,
        @Query("returnDate") returnDate: String? = null,
        @Query("adults") adults: Int? = null
    ): Response<List<FlightOffer>>
    
    /**
     * Get AI-powered flight recommendations based on user preferences
     * Returns JsonElement to handle both array and object responses
     */
    @POST("offers/recommendations")
    suspend fun getRecommendations(
        @Body request: RecommendationsRequest
    ): Response<JsonElement>
}

