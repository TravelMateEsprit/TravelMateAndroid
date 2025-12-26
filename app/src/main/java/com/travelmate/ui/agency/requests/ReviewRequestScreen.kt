package com.travelmate.ui.agency.requests

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.travelmate.data.models.RequestStatus
import com.travelmate.ui.requests.DetailRow
import com.travelmate.ui.requests.RequestDetailsState
import com.travelmate.ui.requests.RequestDetailsViewModel
import com.travelmate.ui.user.requests.EnhancedStatusChip
import com.travelmate.ui.theme.ColorPrimary
import com.travelmate.ui.theme.ColorSuccess
import com.travelmate.ui.theme.ColorError
import com.travelmate.ui.theme.ColorTextPrimary
import com.travelmate.ui.theme.ColorTextSecondary
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
                title = { 
                    Text(
                        "Réviser la demande",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
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
                    // Status Header Card with large icon
                    StatusHeaderCard(status = request.status)
                    
                    // Informations du voyageur
                    InfoCard(
                        title = "Informations du voyageur",
                        icon = Icons.Default.Person
                    ) {
                        DetailItem(
                            icon = Icons.Default.Person,
                            label = "Nom",
                            value = request.travelerName
                        )
                        DetailItem(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = request.travelerEmail
                        )
                        DetailItem(
                            icon = Icons.Default.Phone,
                            label = "Téléphone",
                            value = request.travelerPhone
                        )
                        DetailItem(
                            icon = Icons.Default.CalendarToday,
                            label = "Date de naissance",
                            value = formatDate(request.dateOfBirth)
                        )
                        DetailItem(
                            icon = Icons.Default.Badge,
                            label = "Passeport",
                            value = request.passportNumber
                        )
                        DetailItem(
                            icon = Icons.Default.Flag,
                            label = "Nationalité",
                            value = request.nationality
                        )
                    }
                    
                    // Détails du voyage
                    InfoCard(
                        title = "Détails du voyage",
                        icon = Icons.Default.Flight
                    ) {
                        DetailItem(
                            icon = Icons.Default.LocationOn,
                            label = "Destination",
                            value = request.destination
                        )
                        DetailItem(
                            icon = Icons.Default.FlightTakeoff,
                            label = "Date de départ",
                            value = formatDate(request.departureDate)
                        )
                        DetailItem(
                            icon = Icons.Default.FlightLand,
                            label = "Date de retour",
                            value = formatDate(request.returnDate)
                        )
                        request.travelPurpose?.let {
                            DetailItem(
                                icon = Icons.Default.Info,
                                label = "Motif du voyage",
                                value = it
                            )
                        }
                    }
                    
                    // Message
                    if (request.message != null) {
                        InfoCard(
                            title = "Message du client",
                            icon = Icons.Default.Message
                        ) {
                            Text(
                                text = request.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    // Boutons d'action (seulement si en attente)
                    if (request.status == RequestStatus.PENDING) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showRejectDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                enabled = reviewState !is ReviewRequestState.Loading,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = ColorError
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    width = 2.dp
                                )
                            ) {
                                Icon(Icons.Default.Close, "Rejeter", modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Rejeter",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Button(
                                onClick = { showApproveDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                enabled = reviewState !is ReviewRequestState.Loading,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ColorSuccess,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    Icons.Default.Check, 
                                    "Approuver", 
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Approuver",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
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
            icon = {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = ColorSuccess.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ColorSuccess,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            },
            title = { 
                Text(
                    "Approuver la demande",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Voulez-vous approuver cette demande ?")
                    OutlinedTextField(
                        value = responseText,
                        onValueChange = { responseText = it },
                        label = { Text("Message pour le client") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        reviewViewModel.approveRequest(requestId, responseText)
                        showApproveDialog = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorSuccess,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.Default.Check, 
                        null, 
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Approuver", 
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showApproveDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Annuler")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    // Dialog de rejet
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            icon = {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = ColorError.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            tint = ColorError,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            },
            title = { 
                Text(
                    "Rejeter la demande",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Voulez-vous rejeter cette demande ?")
                    OutlinedTextField(
                        value = responseText,
                        onValueChange = { responseText = it },
                        label = { Text("Raison du rejet *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        isError = responseText.isBlank()
                    )
                    if (responseText.isBlank()) {
                        Text(
                            text = "La raison du rejet est obligatoire",
                            style = MaterialTheme.typography.bodySmall,
                            color = ColorError
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        reviewViewModel.rejectRequest(requestId, responseText)
                        showRejectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorError,
                        contentColor = Color.White
                    ),
                    enabled = responseText.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Close, 
                        null, 
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Rejeter", 
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRejectDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Annuler")
                }
            },
            shape = RoundedCornerShape(16.dp)
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

@Composable
fun StatusHeaderCard(status: RequestStatus) {
    val statusInfo = when (status) {
        RequestStatus.PENDING -> Triple(Icons.Default.Pending, "En attente", ColorPrimary)
        RequestStatus.APPROVED -> Triple(Icons.Default.CheckCircle, "Approuvée", ColorSuccess)
        RequestStatus.REJECTED -> Triple(Icons.Default.Cancel, "Rejetée", ColorError)
        RequestStatus.CANCELLED -> Triple(Icons.Default.Close, "Annulée", ColorTextSecondary)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = statusInfo.third.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = statusInfo.third.copy(alpha = 0.2f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = statusInfo.first,
                        contentDescription = null,
                        tint = statusInfo.third,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            Text(
                text = "Statut de la demande",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            EnhancedStatusChip(status = status)
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = ColorPrimary.copy(alpha = 0.2f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ColorPrimary,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(24.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            content()
        }
    }
}

@Composable
fun DetailItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ColorPrimary,
            modifier = Modifier.size(20.dp)
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
