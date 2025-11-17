package com.travelmate.data.repository

import com.travelmate.data.models.CreatePaymentResponse
import com.travelmate.data.services.PaymentService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val paymentService: PaymentService
) {
    
    /**
     * Créer un PaymentIntent pour une demande d'assurance
     */
    suspend fun createPaymentIntent(requestId: String, token: String): Result<CreatePaymentResponse> {
        return paymentService.createPaymentIntent(requestId, token)
    }
    
    /**
     * Confirmer le paiement après succès
     */
    suspend fun confirmPayment(requestId: String, paymentIntentId: String, token: String): Result<Unit> {
        return paymentService.confirmPayment(requestId, paymentIntentId, token)
    }
}
