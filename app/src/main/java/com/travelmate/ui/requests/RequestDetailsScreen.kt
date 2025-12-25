package com.travelmate.ui.requests

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.travelmate.data.models.InsuranceRequest
import com.travelmate.data.models.RequestStatus
import com.travelmate.ui.user.requests.EnhancedStatusChip
import com.travelmate.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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
    val colorScheme = MaterialTheme.colorScheme
    
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
                title = { 
                    Text(
                        "Détails de la demande",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.primary,
                    titleContentColor = colorScheme.onPrimary,
                    navigationIconContentColor = colorScheme.onPrimary
                )
            )
        },
        containerColor = colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorScheme.background)
        ) {
            when (state) {
                is RequestDetailsState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = colorScheme.primary)
                            Text(
                                "Chargement...",
                                color = colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                is RequestDetailsState.Success -> {
                    val request = (state as RequestDetailsState.Success).request
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // En-tête avec statut
                        StatusHeaderCard(request = request, colorScheme = colorScheme)
                        
                        // Informations du voyageur
                        InfoCard(
                            title = "Informations du voyageur",
                            icon = Icons.Default.Person,
                            colorScheme = colorScheme
                        ) {
                            DetailItem(
                                icon = Icons.Default.Badge,
                                label = "Nom",
                                value = request.travelerName,
                                colorScheme = colorScheme
                            )
                            DetailItem(
                                icon = Icons.Default.Email,
                                label = "Email",
                                value = request.travelerEmail,
                                colorScheme = colorScheme
                            )
                            DetailItem(
                                icon = Icons.Default.Phone,
                                label = "Téléphone",
                                value = request.travelerPhone,
                                colorScheme = colorScheme
                            )
                            DetailItem(
                                icon = Icons.Default.Cake,
                                label = "Date de naissance",
                                value = formatDate(request.dateOfBirth),
                                colorScheme = colorScheme
                            )
                            DetailItem(
                                icon = Icons.Default.CreditCard,
                                label = "Passeport",
                                value = request.passportNumber,
                                colorScheme = colorScheme
                            )
                            DetailItem(
                                icon = Icons.Default.Flag,
                                label = "Nationalité",
                                value = request.nationality,
                                colorScheme = colorScheme
                            )
                        }
                        
                        // Détails du voyage
                        InfoCard(
                            title = "Détails du voyage",
                            icon = Icons.Default.Flight,
                            colorScheme = colorScheme
                        ) {
                            DetailItem(
                                icon = Icons.Default.Public,
                                label = "Destination",
                                value = request.destination,
                                colorScheme = colorScheme
                            )
                            DetailItem(
                                icon = Icons.Default.FlightTakeoff,
                                label = "Date de départ",
                                value = formatDate(request.departureDate),
                                colorScheme = colorScheme
                            )
                            DetailItem(
                                icon = Icons.Default.FlightLand,
                                label = "Date de retour",
                                value = formatDate(request.returnDate),
                                colorScheme = colorScheme
                            )
                            request.travelPurpose?.let {
                                DetailItem(
                                    icon = Icons.Default.WorkOutline,
                                    label = "Motif du voyage",
                                    value = it,
                                    colorScheme = colorScheme
                                )
                            }
                        }
                        
                        // Message
                        if (request.message != null) {
                            InfoCard(
                                title = "Message",
                                icon = Icons.Default.Message,
                                colorScheme = colorScheme
                            ) {
                                Text(
                                    text = request.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                        
                        // Réponse de l'agence
                        if (request.agencyResponse != null) {
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + expandVertically()
                            ) {
                                InfoCard(
                                    title = "Réponse de l'agence",
                                    icon = Icons.Default.Business,
                                    containerColor = when (request.status) {
                                        RequestStatus.APPROVED -> ColorSuccess.copy(alpha = 0.1f)
                                        RequestStatus.REJECTED -> colorScheme.error.copy(alpha = 0.1f)
                                        else -> colorScheme.surface
                                    },
                                    colorScheme = colorScheme
                                ) {
                                    Text(
                                        text = request.agencyResponse,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colorScheme.onSurface,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                    request.reviewedAt?.let {
                                        Text(
                                            text = "Révisée le ${formatDate(it)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Bouton de paiement pour l'utilisateur (si approuvé et pas encore payé)
                        if (!isAgencyView && request.status == RequestStatus.APPROVED) {
                            val paymentNotCompleted = request.paymentStatus == null || 
                                                     request.paymentStatus != "succeeded"
                            
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + expandVertically()
                            ) {
                                if (paymentNotCompleted) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = ColorSuccess
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Button(
                                            onClick = { 
                                                navController.navigate("payment/${request.id}")
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ColorSuccess
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Payment,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                "Procéder au paiement",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                } else {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = ColorSuccess.copy(alpha = 0.1f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = ColorSuccess,
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = "Paiement effectué",
                                                color = ColorSuccess,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Bouton d'annulation pour l'utilisateur
                        if (!isAgencyView && request.status == RequestStatus.PENDING) {
                            OutlinedButton(
                                onClick = { showCancelDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = cancelState !is CancelRequestState.Loading,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colorScheme.error
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Cancel,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Annuler la demande")
                            }
                        }
                        
                        if (cancelState is CancelRequestState.Error) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = colorScheme.error.copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint = colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = (cancelState as CancelRequestState.Error).message,
                                        color = colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = colorScheme.error
                            )
                            Text(
                                text = (state as RequestDetailsState.Error).message,
                                color = colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Dialog de confirmation d'annulation
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { 
                Text(
                    "Annuler la demande",
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            },
            text = { 
                Text(
                    "Êtes-vous sûr de vouloir annuler cette demande ? Cette action est irréversible.",
                    color = colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelRequest(requestId)
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.error
                    )
                ) {
                    Text("Confirmer l'annulation")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelDialog = false }) {
                    Text("Retour")
                }
            },
            containerColor = colorScheme.surface
        )
    }
}

@Composable
private fun StatusHeaderCard(
    request: InsuranceRequest,
    colorScheme: ColorScheme = MaterialTheme.colorScheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        when (request.status) {
                            RequestStatus.APPROVED -> ColorSuccess.copy(alpha = 0.1f)
                            RequestStatus.REJECTED -> colorScheme.error.copy(alpha = 0.1f)
                            RequestStatus.PENDING -> colorScheme.primary.copy(alpha = 0.1f)
                            RequestStatus.CANCELLED -> colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (request.status) {
                        RequestStatus.APPROVED -> Icons.Default.CheckCircle
                        RequestStatus.REJECTED -> Icons.Default.Cancel
                        RequestStatus.PENDING -> Icons.Default.HourglassEmpty
                        RequestStatus.CANCELLED -> Icons.Default.Block
                    },
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = when (request.status) {
                        RequestStatus.APPROVED -> ColorSuccess
                        RequestStatus.REJECTED -> colorScheme.error
                        RequestStatus.PENDING -> colorScheme.primary
                        RequestStatus.CANCELLED -> colorScheme.onSurfaceVariant
                    }
                )
            }
            
            EnhancedStatusChip(status = request.status, colorScheme = colorScheme)
            
            Text(
                text = "Demande soumise le ${formatDate(request.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: ImageVector,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            }
            
            Divider(
                modifier = Modifier.padding(bottom = 16.dp),
                color = colorScheme.outline.copy(alpha = 0.3f)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun DetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    colorScheme: ColorScheme = MaterialTheme.colorScheme
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    DetailItem(
        icon = Icons.Default.Info,
        label = label,
        value = value
    )
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
