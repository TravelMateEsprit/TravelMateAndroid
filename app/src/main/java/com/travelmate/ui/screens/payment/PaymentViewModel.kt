package com.travelmate.ui.screens.payment

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.travelmate.data.models.InsuranceRequest
import com.travelmate.data.repository.InsuranceRequestRepository
import com.travelmate.data.repository.PaymentRepository
import com.travelmate.data.services.PaymentService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentUiState(
    val isLoading: Boolean = false,
    val isProcessingPayment: Boolean = false,
    val request: InsuranceRequest? = null,
    val error: String? = null,
    val clientSecret: String? = null,
    val paymentSuccess: Boolean = false,
    val currentPaymentIntentId: String? = null
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val insuranceRequestRepository: InsuranceRequestRepository,
    private val paymentService: PaymentService,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("travelmate_prefs", Context.MODE_PRIVATE)
    
    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()
    
    private val TAG = "PaymentViewModel"
    
    private fun getAuthToken(): String {
        val token = prefs.getString("access_token", "") ?: ""
        return token
    }
    
    /**
     * Charger les détails de la demande d'assurance
     */
    fun loadRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val token = getAuthToken()
                val response = insuranceRequestRepository.getRequestById(token, requestId)
                
                if (response.isSuccessful && response.body() != null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            request = response.body(),
                            error = null
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Erreur lors du chargement: ${response.message()}"
                        ) 
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading request", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Erreur inattendue"
                    ) 
                }
            }
        }
    }
    
    /**
     * Initier le processus de paiement
     */
    fun initiatePayment(requestId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingPayment = true, error = null) }
            
            try {
                val token = getAuthToken()
                
                // 1. Créer le PaymentIntent côté serveur
                paymentRepository.createPaymentIntent(requestId, token)
                    .onSuccess { paymentResponse ->
                        Log.d(TAG, "PaymentIntent created: ${paymentResponse.paymentIntentId}")
                        
                        // 2. Sauvegarder le PaymentIntentId
                        _uiState.update { 
                            it.copy(currentPaymentIntentId = paymentResponse.paymentIntentId) 
                        }
                        
                        // 3. Déclencher l'affichage du PaymentSheet avec le clientSecret
                        _uiState.update { 
                            it.copy(
                                isProcessingPayment = false,
                                clientSecret = paymentResponse.clientSecret,
                                error = null
                            ) 
                        }
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "Error creating payment intent", exception)
                        _uiState.update { 
                            it.copy(
                                isProcessingPayment = false,
                                error = exception.message ?: "Erreur lors de la création du paiement"
                            ) 
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error initiating payment", e)
                _uiState.update { 
                    it.copy(
                        isProcessingPayment = false,
                        error = e.message ?: "Erreur inattendue"
                    ) 
                }
            }
        }
    }
    
    /**
     * Gérer le résultat du PaymentSheet
     */
    fun handlePaymentResult(result: PaymentSheetResult) {
        paymentService.handlePaymentResult(
            result = result,
            onSuccess = {
                Log.d(TAG, "Payment successful, confirming...")
                confirmPaymentWithServer()
            },
            onError = { errorMessage ->
                Log.e(TAG, "Payment failed: $errorMessage")
                _uiState.update { 
                    it.copy(
                        isProcessingPayment = false,
                        error = "Paiement échoué: $errorMessage"
                    ) 
                }
            },
            onCancelled = {
                Log.d(TAG, "Payment cancelled by user")
                _uiState.update { 
                    it.copy(
                        isProcessingPayment = false
                    ) 
                }
            }
        )
    }
    
    /**
     * Confirmer le paiement côté serveur
     */
    private fun confirmPaymentWithServer() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingPayment = true) }
            
            try {
                val requestId = _uiState.value.request?.id
                val paymentIntentId = _uiState.value.currentPaymentIntentId
                val token = getAuthToken()
                
                if (requestId == null || paymentIntentId == null) {
                    _uiState.update { 
                        it.copy(
                            isProcessingPayment = false,
                            error = "Informations de paiement manquantes"
                        ) 
                    }
                    return@launch
                }
                
                paymentRepository.confirmPayment(requestId, paymentIntentId, token)
                    .onSuccess {
                        Log.d(TAG, "Payment confirmed on server")
                        _uiState.update { 
                            it.copy(
                                isProcessingPayment = false,
                                paymentSuccess = true,
                                error = null
                            ) 
                        }
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "Error confirming payment", exception)
                        _uiState.update { 
                            it.copy(
                                isProcessingPayment = false,
                                error = "Paiement effectué mais erreur de confirmation: ${exception.message}"
                            ) 
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error confirming payment", e)
                _uiState.update { 
                    it.copy(
                        isProcessingPayment = false,
                        error = "Erreur inattendue lors de la confirmation"
                    ) 
                }
            }
        }
    }
    
    /**
     * Réinitialiser l'état du paiement
     */
    fun resetPaymentState() {
        _uiState.update { 
            PaymentUiState(request = it.request) 
        }
    }
}
