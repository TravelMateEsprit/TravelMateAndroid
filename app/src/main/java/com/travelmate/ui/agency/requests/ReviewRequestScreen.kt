package com.travelmate.ui.agency.requests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.travelmate.data.models.RequestStatus
import com.travelmate.ui.requests.DetailRow
import com.travelmate.ui.requests.RequestDetailsState
import com.travelmate.ui.requests.RequestDetailsViewModel
import com.travelmate.ui.user.requests.StatusChip
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewRequestScreen(
    navController: NavController,
    requestId: String,
    detailsViewModel: RequestDetailsViewModel = hiltViewModel(),
    reviewViewModel: AgencyInsuranceRequestsViewModel = hiltViewModel()
) {
    val detailsState by detailsViewModel.state.collectAsState()
    val reviewState by reviewViewModel.reviewState.collectAsState()
    
    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var responseText by remember { mutableStateOf("") }
    
    LaunchedEffect(requestId) {
        detailsViewModel.loadRequestDetails(requestId)
    }
    
    LaunchedEffect(reviewState) {
        if (reviewState is ReviewRequestState.Success) {
            reviewViewModel.resetReviewState()
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Réviser la demande") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (detailsState) {
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
                val request = (detailsState as RequestDetailsState.Success).request
                
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
                                    text = "Message du client",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = request.message,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    
                    // Boutons d'action (seulement si en attente)
                    if (request.status == RequestStatus.PENDING) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showRejectDialog = true },
                                modifier = Modifier.weight(1f),
                                enabled = reviewState !is ReviewRequestState.Loading,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Close, "Rejeter")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Rejeter")
                            }
                            
                            Button(
                                onClick = { showApproveDialog = true },
                                modifier = Modifier.weight(1f),
                                enabled = reviewState !is ReviewRequestState.Loading
                            ) {
                                Icon(Icons.Default.Check, "Approuver")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Approuver")
                            }
                        }
                    }
                    
                    // Réponse de l'agence (si déjà traitée)
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
                    
                    if (reviewState is ReviewRequestState.Error) {
                        Text(
                            text = (reviewState as ReviewRequestState.Error).message,
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
                        text = (detailsState as RequestDetailsState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    
    // Dialog d'approbation
    if (showApproveDialog) {
        AlertDialog(
            onDismissRequest = { showApproveDialog = false },
            title = { Text("Approuver la demande") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Voulez-vous approuver cette demande ?")
                    OutlinedTextField(
                        value = responseText,
                        onValueChange = { responseText = it },
                        label = { Text("Message pour le client") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        reviewViewModel.approveRequest(requestId, responseText)
                        showApproveDialog = false
                    }
                ) {
                    Text("Approuver")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
    
    // Dialog de rejet
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Rejeter la demande") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Voulez-vous rejeter cette demande ?")
                    OutlinedTextField(
                        value = responseText,
                        onValueChange = { responseText = it },
                        label = { Text("Raison du rejet *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        reviewViewModel.rejectRequest(requestId, responseText)
                        showRejectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = responseText.isNotBlank()
                ) {
                    Text("Rejeter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Annuler")
                }
            }
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
