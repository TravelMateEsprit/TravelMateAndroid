package com.travelmate.utils

import org.osmdroid.util.GeoPoint

object CityCoordinates {
    val coordinates =
            mapOf(
                    "Tunis" to GeoPoint(36.8065, 10.1815),
                    "Paris" to GeoPoint(48.8566, 2.3522),
                    "London" to GeoPoint(51.5074, -0.1278),
                    "Rome" to GeoPoint(41.9028, 12.4964),
                    "Madrid" to GeoPoint(40.4168, -3.7038),
                    "Istanbul" to GeoPoint(41.0082, 28.9784),
                    "Cairo" to GeoPoint(30.0444, 31.2357),
                    "Dubai" to GeoPoint(25.2048, 55.2708)
                    // Add more cities as needed
                    )

    fun getCoordinates(cityName: String): GeoPoint? {
        return coordinates[cityName]
    }
}
