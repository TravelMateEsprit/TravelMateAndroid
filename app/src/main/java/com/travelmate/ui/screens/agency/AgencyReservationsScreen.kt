package com.travelmate.ui.screens.agency

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.travelmate.data.models.Reservation
import com.travelmate.data.models.ReservationStatus
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.AgencyReservationsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyReservationsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPacks: () -> Unit,
    onOpenReservation: (Reservation) -> Unit = {},
    viewModel: AgencyReservationsViewModel = hiltViewModel()
) {
    val reservations by viewModel.reservations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showFilters by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(ReservationStatus.PENDING) }

    LaunchedEffect(Unit) {
        viewModel.loadReservations()
    }

    val filteredReservations = remember(reservations, selectedFilter) {
        reservations.filter { res ->
            when (selectedFilter) {
                ReservationStatus.PENDING -> res.status == ReservationStatus.PENDING.name
                ReservationStatus.ACCEPTED -> res.status == ReservationStatus.ACCEPTED.name
                ReservationStatus.REJECTED -> res.status == ReservationStatus.REJECTED.name
                ReservationStatus.CANCELLED -> res.status == ReservationStatus.CANCELLED.name
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Demandes de réservation", fontWeight = FontWeight.Bold)
                        Text(
                            "${reservations.count { it.status == ReservationStatus.PENDING.name }} en attente",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrer", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToPacks, containerColor = ColorPrimary) {
                Icon(Icons.Default.ShoppingBag, contentDescription = "Mes packs", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F6FB))
        ) {
            when {
                isLoading -> LoadingState()
                error != null -> ErrorState(message = error, onRetry = { viewModel.loadReservations() })
                filteredReservations.isEmpty() -> EmptyState()
                else -> ReservationsList(
                    reservations = filteredReservations,
                    onAccept = viewModel::acceptReservation,
                    onReject = viewModel::rejectReservation,
                    onOpenReservation = onOpenReservation
                )
            }
        }
    }

    if (showFilters) {
        ReservationFiltersDialog(
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it },
            onDismiss = { showFilters = false }
        )
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = ColorPrimary)
    }
}

@Composable
private fun ErrorState(message: String?, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Event, contentDescription = null, tint = ColorPrimary, modifier = Modifier.size(72.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(message ?: "Erreur inconnue", color = ColorTextPrimary)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)) {
            Text("Réessayer")
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Event, contentDescription = null, tint = ColorTextSecondary, modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Aucune demande", fontWeight = FontWeight.Bold, color = ColorTextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Les réservations de vos packs apparaîtront ici", color = ColorTextSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ReservationsList(
    reservations: List<Reservation>,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    onOpenReservation: (Reservation) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(reservations, key = { it.id }) { reservation ->
            ReservationCard(
                reservation = reservation,
                onAccept = { onAccept(reservation.id) },
                onReject = { onReject(reservation.id) },
                onOpenReservation = { onOpenReservation(reservation) }
            )
        }
    }
}

@Composable
private fun ReservationCard(
    reservation: Reservation,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onOpenReservation: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onOpenReservation() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(reservation.packName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ColorTextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Demande de ${reservation.userName}", color = ColorTextSecondary)
                }
                StatusChip(status = ReservationStatus.valueOf(reservation.status))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Person, contentDescription = null, tint = ColorPrimary)
                Column {
                    Text(reservation.userName, fontWeight = FontWeight.Medium, color = ColorTextPrimary)
                    Text(reservation.userEmail, color = ColorTextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = ColorPrimary)
                Column {
                    Text("Demande envoyée", color = ColorTextSecondary, fontSize = 12.sp)
                    Text(formatDate(System.currentTimeMillis()), fontWeight = FontWeight.SemiBold, color = ColorTextPrimary)
                }
            }

            if (reservation.packName.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = ColorPrimary)
                    Text("Voir le pack", color = ColorPrimary, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (ReservationStatus.valueOf(reservation.status) == ReservationStatus.PENDING) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorError)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Refuser")
                    }
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorSuccess)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Accepter")
                    }
                }
            } else {
                Text(
                    text = when (ReservationStatus.valueOf(reservation.status)) {
                        ReservationStatus.ACCEPTED -> "Réservation acceptée"
                        ReservationStatus.REJECTED -> "Réservation refusée"
                        ReservationStatus.CANCELLED -> "Réservation annulée"
                        else -> ""
                    },
                    color = ColorTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: ReservationStatus) {
    val option = when (status) {
        ReservationStatus.PENDING -> StatusOption("En attente", Color(0xFFFFA726))
        ReservationStatus.ACCEPTED -> StatusOption("Acceptée", Color(0xFF4CAF50))
        ReservationStatus.REJECTED -> StatusOption("Refusée", Color(0xFFF44336))
        ReservationStatus.CANCELLED -> StatusOption("Annulées", Color(0xFF607D8B))
    }

    Surface(color = option.color.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)) {
        Text(
            option.label,
            color = option.color,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontWeight = FontWeight.Bold
        )
    }
}

data class StatusOption(val label: String, val color: Color)

@Composable
private fun ReservationFiltersDialog(
    selectedFilter: ReservationStatus,
    onFilterSelected: (ReservationStatus) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrer les demandes") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ReservationStatus.values().forEach { status ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onFilterSelected(status)
                            },
                        color = if (selectedFilter == status) ColorPrimary.copy(alpha = 0.1f) else Color.Transparent
                    ) {
                        Text(
                            when (status) {
                                ReservationStatus.PENDING -> "En attente"
                                ReservationStatus.ACCEPTED -> "Acceptées"
                                ReservationStatus.REJECTED -> "Refusées"
                                ReservationStatus.CANCELLED -> "Annulées"
                            },
                            modifier = Modifier.padding(12.dp),
                            color = if (selectedFilter == status) ColorPrimary else ColorTextPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy à HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
