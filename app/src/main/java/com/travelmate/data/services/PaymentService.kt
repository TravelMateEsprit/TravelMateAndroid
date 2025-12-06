package com.travelmate.data.services

import android.content.Context
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.travelmate.data.api.InsuranceApi
import com.travelmate.data.models.ConfirmPaymentRequest
import com.travelmate.data.models.ConfirmPaymentResponse
import com.travelmate.data.models.CreatePaymentResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val insuranceApi: InsuranceApi
) {
    
    companion object {
        // Clé publique Stripe de test
        private const val STRIPE_PUBLISHABLE_KEY = "pk_test_51SUGR4JJWTAdvnOKJdImfKrKxd7YommBn5lZjahj64KllGBR8Q9lCPta30s59RlUhdVhbnorFHi15JqMzU2lz55700CcxWuG6D"
    }
    
    init {
        // Initialiser Stripe avec la clé publique
        PaymentConfiguration.init(context, STRIPE_PUBLISHABLE_KEY)
    }
    
    /**
     * Créer un PaymentIntent côté serveur et retourner les informations de paiement
     */
    suspend fun createPaymentIntent(requestId: String, token: String): Result<CreatePaymentResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = insuranceApi.createPayment(requestId)
                
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Erreur lors de la création du paiement: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Confirmer le paiement côté serveur après succès
     */
    suspend fun confirmPayment(requestId: String, paymentIntentId: String, token: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ConfirmPaymentRequest(paymentIntentId = paymentIntentId)
                val response = insuranceApi.confirmPayment(requestId, request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Result.success(Unit)
                } else {
                    val errorMsg = response.body()?.message ?: response.message()
                    Result.failure(Exception("Erreur lors de la confirmation: $errorMsg"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Traiter le résultat du PaymentSheet
     */
    fun handlePaymentResult(
        result: PaymentSheetResult,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit
    ) {
        when (result) {
            is PaymentSheetResult.Completed -> {
                // Paiement réussi
                onSuccess()
            }
            is PaymentSheetResult.Canceled -> {
                // Utilisateur a annulé
                onCancelled()
            }
            is PaymentSheetResult.Failed -> {
                // Erreur de paiement
                onError(result.error.localizedMessage ?: "Erreur de paiement")
            }
        }
    }
}
