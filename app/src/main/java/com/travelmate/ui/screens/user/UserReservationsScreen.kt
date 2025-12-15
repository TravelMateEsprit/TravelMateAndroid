package com.travelmate.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.travelmate.data.models.Reservation
import com.travelmate.data.models.ReservationStatus
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.UserPacksViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * User reservations screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserReservationsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPackDetails: (String) -> Unit,
    viewModel: UserPacksViewModel = hiltViewModel()
) {
    val reservations by viewModel.reservations.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadReservations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Mes Réservations (${reservations.size})",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Retour", tint = Color.White)
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
        if (reservations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.EventNote,
                        contentDescription = null,
                        tint = ColorTextSecondary,
                        modifier = Modifier.size(80.dp)
                    )
                    Text(
                        "Aucune réservation",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                    Text(
                        "Vos réservations apparaîtront ici",
                        fontSize = 14.sp,
                        color = ColorTextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reservations, key = { it.id }) { reservation ->
                    ReservationCard(
                        reservation = reservation,
                        onClick = {
                            onNavigateToPackDetails(reservation.packId)
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        reservation.packName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        reservation.packDestination,
                        fontSize = 14.sp,
                        color = ColorTextSecondary
                    )
                }
                
                // Status badge
                StatusBadge(status = reservation.status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Divider()
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = ColorTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        formatDate(reservation.createdAt),
                        fontSize = 12.sp,
                        color = ColorTextSecondary
                    )
                }
                
                Text(
                    "${reservation.packPrice.toInt()} DT",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorPrimary
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val reservationStatus = runCatching { ReservationStatus.valueOf(status) }.getOrNull()
    val (text, color) = when (reservationStatus) {
        ReservationStatus.PENDING -> "En attente" to Color(0xFFFFA726)
        ReservationStatus.ACCEPTED -> "Acceptée" to Color(0xFF66BB6A)
        ReservationStatus.REJECTED -> "Refusée" to Color(0xFFEF5350)
        ReservationStatus.CANCELLED -> "Annulée" to Color(0xFF78909C)
        null -> "Inconnu" to Color.Gray
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
