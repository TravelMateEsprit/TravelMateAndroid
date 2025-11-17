package com.travelmate.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonObject

@Serializable
data class FlightOffer(
    val id: String? = null,
    val airline: String? = null,
    val flightNumber: String? = null,
    val type: String? = null, // "aller-retour", "aller-simple", "multi-destin"
    val from: JsonElement? = null, // Can be Airport object or string
    val to: JsonElement? = null, // Can be Airport object or string
    val departure: JsonElement? = null, // Can be FlightSegment object or missing
    val returnSegment: JsonElement? = null, // For round trips
    val price: JsonElement? = null, // Can be number or string like "199€"
    val currency: String? = "TND",
    val duration: String? = null, // Total duration in format "2h 40min"
    val direct: Boolean? = true,
    val stops: Int? = 0,
    val availableSeats: Int? = null,
    val imageUrl: String? = null,
    val createdAt: String? = null,
    // Alternative field names that might be used by backend
    val _id: String? = null,
    val date_depart: String? = null,
    val date_return: String? = null,
    val dateDepart: String? = null,
    val dateReturn: String? = null,
    val prix: JsonElement? = null,
    val prix_estime: JsonElement? = null
) {
    fun getIdValue(): String {
        return id ?: _id ?: ""
    }
    
    fun getAirlineName(): String {
        return airline ?: ""
    }
    
    fun getTypeValue(): String {
        return type ?: "aller-simple"
    }
    
    fun getFromAirport(): Airport {
        return from?.let { parseAirport(it) } ?: Airport()
    }
    
    fun getToAirport(): Airport {
        return to?.let { parseAirport(it) } ?: Airport()
    }
    
    /**
     * Get the departure segment, parsing from JsonElement if needed
     */
    fun getDepartureSegment(): FlightSegment? {
        return departure?.let { parseFlightSegment(it) }
    }
    
    /**
     * Get the return segment, parsing from JsonElement if needed
     */
    fun getReturnSegment(): FlightSegment? {
        return returnSegment?.let { parseFlightSegment(it) }
    }
    
    /**
     * Get the price as a Double value
     */
    fun getPrice(): Double {
        val priceElement = price ?: prix ?: prix_estime
        return priceElement?.let { parsePrice(it) } ?: 0.0
    }
    
    /**
     * Get the price as a formatted string with currency
     */
    fun getFormattedPrice(): String {
        val priceValue = getPrice()
        val currencyStr = currency ?: "TND"
        return "${priceValue.toInt()} $currencyStr"
    }
    
    /**
     * Get departure date
     */
    fun getDepartureDate(): String? {
        return date_depart ?: dateDepart
    }
    
    /**
     * Get return date
     */
    fun getReturnDate(): String? {
        return date_return ?: dateReturn
    }
    
    private fun parseFlightSegment(element: JsonElement): FlightSegment? {
        return try {
            if (element is JsonObject) {
                // Try to parse as FlightSegment object
                val departureDetails = element["departure"]?.let { parseSegmentDetails(it) }
                    ?: SegmentDetails(time = "", airport = kotlinx.serialization.json.JsonPrimitive(""))
                val arrivalDetails = element["arrival"]?.let { parseSegmentDetails(it) }
                    ?: SegmentDetails(time = "", airport = kotlinx.serialization.json.JsonPrimitive(""))
                
                FlightSegment(
                    flightNumber = element["flightNumber"]?.jsonPrimitive?.content,
                    airline = element["airline"]?.jsonPrimitive?.content,
                    departure = departureDetails,
                    arrival = arrivalDetails,
                    duration = element["duration"]?.jsonPrimitive?.content ?: "",
                    direct = element["direct"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: true,
                    stops = element["stops"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseSegmentDetails(element: JsonElement): SegmentDetails {
        return try {
            if (element is JsonObject) {
                SegmentDetails(
                    time = element["time"]?.jsonPrimitive?.content ?: "",
                    airport = element["airport"] ?: kotlinx.serialization.json.JsonPrimitive(""),
                    date = element["date"]?.jsonPrimitive?.content
                )
            } else {
                SegmentDetails(time = "", airport = kotlinx.serialization.json.JsonPrimitive(""))
            }
        } catch (e: Exception) {
            SegmentDetails(time = "", airport = kotlinx.serialization.json.JsonPrimitive(""))
        }
    }
    
    private fun parsePrice(element: JsonElement): Double {
        return try {
            when {
                element is kotlinx.serialization.json.JsonPrimitive -> {
                    if (element.isString) {
                        // It's a string like "199€" or "199 EUR" or "199"
                        val priceStr = element.content
                        // Remove currency symbols and whitespace, then extract number
                        val cleaned = priceStr
                            .replace("€", "")
                            .replace("EUR", "")
                            .replace("TND", "")
                            .replace("USD", "")
                            .replace("$", "")
                            .replace(",", ".") // Handle comma as decimal separator
                            .trim()
                            .replace(Regex("[^0-9.]"), "") // Keep only digits and dots
                        
                        cleaned.toDoubleOrNull() ?: 0.0
                    } else {
                        // It's a number - try to parse as double
                        element.content.toDoubleOrNull() ?: 0.0
                    }
                }
                else -> 0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }
    
    private fun parseAirport(element: JsonElement): Airport {
        return try {
            if (element is JsonObject) {
                // It's an object - try different possible field names
                val code = element["code"]?.jsonPrimitive?.content
                    ?: element["airportCode"]?.jsonPrimitive?.content
                    ?: element["Code"]?.jsonPrimitive?.content
                    ?: element["CODE"]?.jsonPrimitive?.content
                    ?: ""
                
                val name = element["name"]?.jsonPrimitive?.content
                    ?: element["airportName"]?.jsonPrimitive?.content
                    ?: element["Name"]?.jsonPrimitive?.content
                    ?: element["NAME"]?.jsonPrimitive?.content
                    ?: element["fullName"]?.jsonPrimitive?.content
                    ?: code // Fallback to code if name not found
                
                Airport(
                    code = code,
                    name = name,
                    city = element["city"]?.jsonPrimitive?.content
                        ?: element["City"]?.jsonPrimitive?.content,
                    country = element["country"]?.jsonPrimitive?.content
                        ?: element["Country"]?.jsonPrimitive?.content
                )
            } else {
                // It's a string (airport code)
                val code = element.jsonPrimitive.content
                Airport(
                    code = code,
                    name = code // Use code as name if name is not provided
                )
            }
        } catch (e: Exception) {
            // Fallback: create airport with empty values
            Airport(code = "", name = "")
        }
    }
}

@Serializable
data class Airport(
    val code: String = "", // Airport code (e.g., "TUN", "ORY")
    val name: String = "", // Full name (e.g., "Tunis Carthage", "Paris Orly")
    val city: String? = null,
    val country: String? = null
)

@Serializable
data class FlightSegment(
    val flightNumber: String? = null,
    val airline: String? = null,
    val departure: SegmentDetails? = null,
    val arrival: SegmentDetails? = null,
    val duration: String? = null, // Format: "2h 40min"
    val direct: Boolean? = true,
    val stops: Int? = 0
) {
    fun getDepartureDetails(): SegmentDetails {
        return departure ?: SegmentDetails(time = "", airport = kotlinx.serialization.json.JsonPrimitive(""))
    }
    
    fun getArrivalDetails(): SegmentDetails {
        return arrival ?: SegmentDetails(time = "", airport = kotlinx.serialization.json.JsonPrimitive(""))
    }
    
    fun getDurationValue(): String {
        return duration ?: ""
    }
    
    fun isDirect(): Boolean {
        return direct ?: true
    }
    
    fun getStops(): Int {
        return stops ?: 0
    }
}

@Serializable
data class SegmentDetails(
    val time: String? = null, // Format: "10:30" or "10:30 AM"
    val airport: JsonElement? = null, // Can be Airport object or string
    val date: String? = null // Format: "2024-11-18" or "mardi 18 novembre"
) {
    fun getTimeValue(): String {
        return time ?: ""
    }
    
    fun getAirport(): Airport {
        return airport?.let { parseAirportFromElement(it) } ?: Airport()
    }
    
    private fun parseAirportFromElement(element: JsonElement): Airport {
        return try {
            if (element is JsonObject) {
                // Try different possible field names
                val code = element["code"]?.jsonPrimitive?.content
                    ?: element["airportCode"]?.jsonPrimitive?.content
                    ?: element["Code"]?.jsonPrimitive?.content
                    ?: element["CODE"]?.jsonPrimitive?.content
                    ?: ""
                
                val name = element["name"]?.jsonPrimitive?.content
                    ?: element["airportName"]?.jsonPrimitive?.content
                    ?: element["Name"]?.jsonPrimitive?.content
                    ?: element["NAME"]?.jsonPrimitive?.content
                    ?: element["fullName"]?.jsonPrimitive?.content
                    ?: code // Fallback to code if name not found
                
                Airport(
                    code = code,
                    name = name,
                    city = element["city"]?.jsonPrimitive?.content
                        ?: element["City"]?.jsonPrimitive?.content,
                    country = element["country"]?.jsonPrimitive?.content
                        ?: element["Country"]?.jsonPrimitive?.content
                )
            } else {
                val code = element.jsonPrimitive.content
                Airport(code = code, name = code)
            }
        } catch (e: Exception) {
            Airport(code = "", name = "")
        }
    }
}

