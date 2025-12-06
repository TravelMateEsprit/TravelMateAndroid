package com.travelmate.ui.screens.payment

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetContract
import com.travelmate.data.models.InsuranceRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    requestId: String,
    onNavigateBack: () -> Unit,
    onPaymentSuccess: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Launcher pour PaymentSheet de Stripe
    val paymentSheetLauncher = rememberLauncherForActivityResult(
        contract = PaymentSheetContract(),
        onResult = { result ->
            viewModel.handlePaymentResult(result)
        }
    )
    
    // Charger les détails de la demande au démarrage
    LaunchedEffect(requestId) {
        viewModel.loadRequest(requestId)
    }
    
    // Gérer les événements
    LaunchedEffect(uiState.paymentSuccess) {
        if (uiState.paymentSuccess) {
            onPaymentSuccess()
        }
    }
    
    // Lancer PaymentSheet quand clientSecret est disponible
    LaunchedEffect(uiState.clientSecret) {
        uiState.clientSecret?.let { clientSecret ->
            val customerName = uiState.request?.travelerName ?: "Client"
            val configuration = PaymentSheet.Configuration(
                merchantDisplayName = "TravelMate Assurances",
                defaultBillingDetails = PaymentSheet.BillingDetails(
                    name = customerName
                )
            )
            
            paymentSheetLauncher.launch(
                PaymentSheetContract.Args.createPaymentIntentArgs(
                    clientSecret = clientSecret,
                    config = configuration
                )
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paiement sécurisé") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    ErrorContent(
                        error = uiState.error!!,
                        onRetry = { viewModel.loadRequest(requestId) },
                        onBack = onNavigateBack
                    )
                }
                uiState.request != null -> {
                    PaymentContent(
                        request = uiState.request!!,
                        isProcessing = uiState.isProcessingPayment,
                        onPayClick = { viewModel.initiatePayment(requestId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentContent(
    request: InsuranceRequest,
    isProcessing: Boolean,
    onPayClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // En-tête avec icône de sécurité
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "Paiement sécurisé",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Propulsé par Stripe",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Détails de l'assurance
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Détails de votre souscription",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Divider()
                
                PaymentDetailRow(
                    icon = Icons.Default.Info,
                    label = "Assurance",
                    value = request.insuranceId // TODO: Afficher le nom
                )
                
                PaymentDetailRow(
                    icon = Icons.Default.Person,
                    label = "Voyageur",
                    value = request.travelerName
                )
                
                PaymentDetailRow(
                    icon = Icons.Default.Place,
                    label = "Destination",
                    value = request.destination
                )
                
                PaymentDetailRow(
                    icon = Icons.Default.DateRange,
                    label = "Dates",
                    value = "${request.departureDate} → ${request.returnDate}"
                )
            }
        }
        
        // Montant à payer
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Montant à payer",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                // TODO: Récupérer le prix réel de l'assurance
                val amount = request.paymentAmount ?: 0.0
                
                Text(
                    text = "${String.format("%.2f", amount)} TND",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Informations de sécurité
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981)
                )
                Column {
                    Text(
                        text = "Vos informations sont protégées",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Paiement crypté et sécurisé via Stripe",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Bouton de paiement
        Button(
            onClick = onPayClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isProcessing,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Préparation du paiement...")
            } else {
                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Procéder au paiement",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        
        // Note en bas
        Text(
            text = "En continuant, vous acceptez que le montant soit débité de votre carte.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun PaymentDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Erreur",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onBack) {
                Text("Retour")
            }
            Button(onClick = onRetry) {
                Text("Réessayer")
            }
        }
    }
}
