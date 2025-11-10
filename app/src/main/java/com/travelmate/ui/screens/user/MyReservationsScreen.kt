package com.travelmate.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.travelmate.data.models.Reservation
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.ReservationsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReservationsScreen(
    onNavigateBack: () -> Unit = {},
    reservationsViewModel: ReservationsViewModel = hiltViewModel()
) {
    val myReservations by reservationsViewModel.myReservations.collectAsState()
    val isLoading by reservationsViewModel.isLoading.collectAsState()
    val error by reservationsViewModel.error.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Load reservations when screen opens
    LaunchedEffect(Unit) {
        reservationsViewModel.loadMyReservations()
    }
    
    // Show error if any
    error?.let { errorMsg ->
        LaunchedEffect(errorMsg) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = errorMsg,
                    duration = SnackbarDuration.Long
                )
                reservationsViewModel.clearError()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes Réservations") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        if (isLoading && myReservations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ColorPrimary)
            }
        } else if (myReservations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = ColorTextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Aucune réservation",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                    Text(
                        text = "Vous n'avez pas encore de réservations",
                        fontSize = 14.sp,
                        color = ColorTextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(myReservations) { reservation ->
                    ReservationCard(
                        reservation = reservation,
                        onCancelReservation = { reservationId ->
                            reservationsViewModel.cancelReservation(reservationId) { result ->
                                result.fold(
                                    onSuccess = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Réservation annulée avec succès",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    },
                                    onFailure = { exception ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = exception.message ?: "Erreur lors de l'annulation",
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReservationCard(
    reservation: Reservation,
    onCancelReservation: (String) -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Réservation #${reservation.id_reservation.take(8)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                StatusBadge(status = reservation.statut)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Voyage ID
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Flight,
                    contentDescription = null,
                    tint = ColorPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Voyage: ${reservation.getVoyageId()}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Price
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = ColorPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Prix: ${reservation.prix} €",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Date
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = ColorPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Réservé le: ${reservation.date_reservation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorTextSecondary
                )
            }
            
            // Cancel button (only show if reservation is not already cancelled)
            if (reservation.statut != "annulee") {
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = { showCancelDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ColorError
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorError)
                ) {
                    Icon(
                        Icons.Default.Cancel,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Annuler la réservation")
                }
            }
        }
    }
    
    // Cancel confirmation dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = {
                Text(
                    text = "Confirmer l'annulation",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Êtes-vous sûr de vouloir annuler cette réservation ? Cette action ne peut pas être annulée.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelReservation(reservation.id_reservation)
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorError)
                ) {
                    Text("Annuler la réservation")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCancelDialog = false }
                ) {
                    Text("Garder la réservation")
                }
            }
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    val (backgroundColor, textColor, text) = when (status) {
        "confirmee" -> Triple(ColorPrimary.copy(alpha = 0.1f), ColorPrimary, "Confirmée")
        "en_attente" -> Triple(ColorAccent.copy(alpha = 0.1f), ColorAccent, "En attente")
        "annulee" -> Triple(ColorError.copy(alpha = 0.1f), ColorError, "Annulée")
        else -> Triple(ColorTextSecondary.copy(alpha = 0.1f), ColorTextSecondary, status)
    }
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
