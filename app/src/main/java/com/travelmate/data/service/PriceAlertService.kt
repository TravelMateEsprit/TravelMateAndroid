package com.travelmate.data.service

import com.travelmate.data.models.AlertStatus
import com.travelmate.data.models.FlightOffer
import com.travelmate.data.models.PriceAlert
import com.travelmate.data.models.TripType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceAlertService @Inject constructor() {
    
    private val _alerts = MutableStateFlow<List<PriceAlert>>(emptyList())
    val alerts: StateFlow<List<PriceAlert>> = _alerts.asStateFlow()
    
    val activeAlerts: StateFlow<List<PriceAlert>> = 
        MutableStateFlow(_alerts.value.filter { it.status == AlertStatus.ACTIVE }).asStateFlow()
    
    val triggeredAlerts: StateFlow<List<PriceAlert>> = 
        MutableStateFlow(_alerts.value.filter { it.status == AlertStatus.TRIGGERED }).asStateFlow()
    
    /**
     * Create a new price alert
     */
    suspend fun createAlert(
        origin: String,
        destination: String,
        departureDate: String,
        priceThreshold: Double,
        airline: String? = null,
        returnDate: String? = null,
        tripType: TripType = TripType.ALLER_SIMPLE
    ): PriceAlert {
        val alert = PriceAlert(
            id = generateId(),
            origin = origin,
            destination = destination,
            departureDate = departureDate,
            returnDate = returnDate,
            tripType = tripType,
            airline = airline,
            priceThreshold = priceThreshold,
            status = AlertStatus.ACTIVE
        )
        _alerts.value = _alerts.value + alert
        return alert
    }
    
    /**
     * Create alert from a flight offer
     */
    suspend fun createAlertFromOffer(
        offer: FlightOffer,
        priceThreshold: Double
    ): PriceAlert {
        val fromAirport = offer.getFromAirport()
        val toAirport = offer.getToAirport()
        val departureDate = offer.getDepartureDate() ?: ""
        val returnDate = offer.getReturnDate()
        val tripType = if (offer.getTypeValue().contains("retour", ignoreCase = true)) TripType.ALLER_RETOUR else TripType.ALLER_SIMPLE
        
        return createAlert(
            origin = fromAirport.code,
            destination = toAirport.code,
            departureDate = departureDate,
            priceThreshold = priceThreshold,
            airline = offer.getAirlineName().takeIf { it.isNotEmpty() },
            returnDate = returnDate,
            tripType = tripType
        )
    }
    
    /**
     * Delete an alert
     */
    suspend fun deleteAlert(alertId: String) {
        _alerts.value = _alerts.value.filter { it.id != alertId }
    }
    
    /**
     * Check alerts against current offers and trigger if price <= threshold
     */
    suspend fun checkAlerts(offers: List<FlightOffer>): List<PriceAlert> {
        val updatedAlerts = _alerts.value.toMutableList()
        val triggeredAlerts = mutableListOf<PriceAlert>()
        
        for (alert in updatedAlerts) {
            if (alert.status == AlertStatus.ACTIVE) {
                // Find matching offer
                val matchingOffer = offers.find { offer ->
                    val fromAirport = offer.getFromAirport()
                    val toAirport = offer.getToAirport()
                    val departureDate = offer.getDepartureDate()
                    
                    fromAirport.code.equals(alert.origin, ignoreCase = true) &&
                    toAirport.code.equals(alert.destination, ignoreCase = true) &&
                    departureDate == alert.departureDate &&
                    (alert.airline == null || offer.getAirlineName().equals(alert.airline, ignoreCase = true))
                }
                
                if (matchingOffer != null) {
                    val currentPrice = matchingOffer.getPrice()
                    if (currentPrice > 0 && currentPrice <= alert.priceThreshold) {
                        // Trigger alert
                        val triggeredAlert = alert.copy(
                            status = AlertStatus.TRIGGERED,
                            currentPrice = currentPrice,
                            triggeredAt = System.currentTimeMillis()
                        )
                        val index = updatedAlerts.indexOf(alert)
                        updatedAlerts[index] = triggeredAlert
                        triggeredAlerts.add(triggeredAlert)
                    }
                }
            }
        }
        
        _alerts.value = updatedAlerts
        return triggeredAlerts
    }
    
    /**
     * Get alerts count (active + triggered)
     */
    fun getAlertsCount(): Int {
        return _alerts.value.size
    }
    
    /**
     * Get active alerts count
     */
    fun getActiveAlertsCount(): Int {
        return _alerts.value.count { it.status == AlertStatus.ACTIVE }
    }
    
    /**
     * Get triggered alerts count
     */
    fun getTriggeredAlertsCount(): Int {
        return _alerts.value.count { it.status == AlertStatus.TRIGGERED }
    }
    
    private fun generateId(): String {
        return "alert_${System.currentTimeMillis()}_${(0..9999).random()}"
    }
}

