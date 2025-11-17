package com.travelmate.ui.requests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.travelmate.data.models.InsuranceRequest
import com.travelmate.data.models.RequestStatus
import com.travelmate.ui.user.requests.StatusChip
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailsScreen(
    navController: NavController,
    requestId: String,
    isAgencyView: Boolean = false,
    viewModel: RequestDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val cancelState by viewModel.cancelState.collectAsState()
    var showCancelDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(requestId) {
        viewModel.loadRequestDetails(requestId)
    }
    
    LaunchedEffect(cancelState) {
        if (cancelState is CancelRequestState.Success) {
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails de la demande") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (state) {
            is RequestDetailsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is RequestDetailsState.Success -> {
                val request = (state as RequestDetailsState.Success).request
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Statut
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Statut",
                                style = MaterialTheme.typography.titleMedium
                            )
                            StatusChip(status = request.status)
                        }
                    }
                    
                    // Informations du voyageur
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Informations du voyageur",
                                style = MaterialTheme.typography.titleMedium
                            )
                            DetailRow("Nom", request.travelerName)
                            DetailRow("Email", request.travelerEmail)
                            DetailRow("Téléphone", request.travelerPhone)
                            DetailRow("Date de naissance", formatDate(request.dateOfBirth))
                            DetailRow("Passeport", request.passportNumber)
                            DetailRow("Nationalité", request.nationality)
                        }
                    }
                    
                    // Détails du voyage
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Détails du voyage",
                                style = MaterialTheme.typography.titleMedium
                            )
                            DetailRow("Destination", request.destination)
                            DetailRow("Date de départ", formatDate(request.departureDate))
                            DetailRow("Date de retour", formatDate(request.returnDate))
                            request.travelPurpose?.let {
                                DetailRow("Motif du voyage", it)
                            }
                        }
                    }
                    
                    // Message
                    if (request.message != null) {
                        Card {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Message",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = request.message,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    
                    // Réponse de l'agence
                    if (request.agencyResponse != null) {
                        Card {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Réponse de l'agence",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = request.agencyResponse,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                request.reviewedAt?.let {
                                    Text(
                                        text = "Révisée le ${formatDate(it)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    // Bouton d'annulation pour l'utilisateur
                    if (!isAgencyView && request.status == RequestStatus.PENDING) {
                        OutlinedButton(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = cancelState !is CancelRequestState.Loading
                        ) {
                            Text("Annuler la demande")
                        }
                    }
                    
                    if (cancelState is CancelRequestState.Error) {
                        Text(
                            text = (cancelState as CancelRequestState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            is RequestDetailsState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (state as RequestDetailsState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    
    // Dialog de confirmation d'annulation
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Annuler la demande") },
            text = { Text("Êtes-vous sûr de vouloir annuler cette demande ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelRequest(requestId)
                        showCancelDialog = false
                    }
                ) {
                    Text("Confirmer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString.take(10)
    }
}
