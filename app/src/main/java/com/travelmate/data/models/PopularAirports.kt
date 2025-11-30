package com.travelmate.data.models

/**
 * Liste d'aéroports populaires pour l'autocomplete
 */
object PopularAirports {
    val airports = listOf(
        Airport(code = "TUN", name = "Tunis Carthage", city = "Tunis", country = "Tunisia"),
        Airport(code = "FCO", name = "Rome Fiumicino", city = "Rome", country = "Italy"),
        Airport(code = "IST", name = "Istanbul Airport", city = "Istanbul", country = "Turkey"),
        Airport(code = "CDG", name = "Paris Charles de Gaulle", city = "Paris", country = "France"),
        Airport(code = "ORY", name = "Paris Orly", city = "Paris", country = "France"),
        Airport(code = "LHR", name = "London Heathrow", city = "London", country = "United Kingdom"),
        Airport(code = "LGW", name = "London Gatwick", city = "London", country = "United Kingdom"),
        Airport(code = "MAD", name = "Madrid Barajas", city = "Madrid", country = "Spain"),
        Airport(code = "BCN", name = "Barcelona El Prat", city = "Barcelona", country = "Spain"),
        Airport(code = "DXB", name = "Dubai International", city = "Dubai", country = "UAE"),
        Airport(code = "JED", name = "King Abdulaziz", city = "Jeddah", country = "Saudi Arabia"),
        Airport(code = "CAI", name = "Cairo International", city = "Cairo", country = "Egypt"),
        Airport(code = "CMN", name = "Mohammed V", city = "Casablanca", country = "Morocco"),
        Airport(code = "ALG", name = "Houari Boumediene", city = "Algiers", country = "Algeria"),
        Airport(code = "FRA", name = "Frankfurt", city = "Frankfurt", country = "Germany"),
        Airport(code = "AMS", name = "Amsterdam Schiphol", city = "Amsterdam", country = "Netherlands"),
        Airport(code = "MUC", name = "Munich", city = "Munich", country = "Germany"),
        Airport(code = "FCO", name = "Leonardo da Vinci", city = "Rome", country = "Italy"),
        Airport(code = "MXP", name = "Milan Malpensa", city = "Milan", country = "Italy"),
        Airport(code = "ATH", name = "Athens International", city = "Athens", country = "Greece"),
        Airport(code = "VIE", name = "Vienna International", city = "Vienna", country = "Austria"),
        Airport(code = "ZRH", name = "Zurich", city = "Zurich", country = "Switzerland"),
        Airport(code = "GVA", name = "Geneva", city = "Geneva", country = "Switzerland"),
        Airport(code = "BRU", name = "Brussels", city = "Brussels", country = "Belgium"),
        Airport(code = "LIS", name = "Lisbon", city = "Lisbon", country = "Portugal"),
        Airport(code = "OPO", name = "Porto", city = "Porto", country = "Portugal"),
        Airport(code = "DOH", name = "Hamad International", city = "Doha", country = "Qatar"),
        Airport(code = "AUH", name = "Abu Dhabi International", city = "Abu Dhabi", country = "UAE"),
        Airport(code = "KWI", name = "Kuwait International", city = "Kuwait City", country = "Kuwait"),
        Airport(code = "BAH", name = "Bahrain International", city = "Manama", country = "Bahrain")
    )
    
    /**
     * Recherche d'aéroports par code, nom, ville ou pays
     */
    fun search(query: String): List<Airport> {
        if (query.isBlank()) return airports
        
        val queryLower = query.lowercase().trim()
        return airports.filter { airport ->
            airport.code.lowercase().contains(queryLower) ||
            airport.name.lowercase().contains(queryLower) ||
            airport.city?.lowercase()?.contains(queryLower) == true ||
            airport.country?.lowercase()?.contains(queryLower) == true
        }
    }
    
    /**
     * Trouve un aéroport par son code IATA
     */
    fun findByCode(code: String): Airport? {
        return airports.find { it.code.equals(code, ignoreCase = true) }
    }
}

