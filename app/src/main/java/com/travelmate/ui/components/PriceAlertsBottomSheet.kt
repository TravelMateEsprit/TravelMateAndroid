package com.travelmate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.travelmate.data.models.AlertStatus
import com.travelmate.data.models.FlightOffer
import com.travelmate.data.models.PriceAlert
import com.travelmate.data.models.TripType
import com.travelmate.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceAlertsBottomSheet(
    alerts: List<PriceAlert>,
    offers: List<FlightOffer>,
    onDismiss: () -> Unit,
    onDeleteAlert: (String) -> Unit,
    onCreateAlert: (FlightOffer, Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var showCreateAlertDialog by remember { mutableStateOf(false) }
    var selectedOffer by remember { mutableStateOf<FlightOffer?>(null) }
    var priceThreshold by remember { mutableStateOf("") }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Alertes de prix",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Create alert button
            Button(
                onClick = {
                    // Show dialog to select offer and set price
                    if (offers.isNotEmpty()) {
                        selectedOffer = offers.first()
                        showCreateAlertDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Créer une alerte")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Alerts list
            if (alerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.NotificationsOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Aucune alerte pour le moment",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(alerts) { alert ->
                        PriceAlertCard(
                            alert = alert,
                            onDelete = { onDeleteAlert(alert.id) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Create alert dialog
    if (showCreateAlertDialog && selectedOffer != null) {
        AlertDialog(
            onDismissRequest = { showCreateAlertDialog = false },
            title = { Text("Créer une alerte de prix") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val fromAirport = selectedOffer!!.getFromAirport()
                    val toAirport = selectedOffer!!.getToAirport()
                    val fromName = if (fromAirport.name.isNotEmpty()) fromAirport.name else fromAirport.code
                    val toName = if (toAirport.name.isNotEmpty()) toAirport.name else toAirport.code
                    val departDate = selectedOffer!!.getDepartureDate() ?: "-"
                    val retDate = selectedOffer!!.getReturnDate()
                    Text("Vol: $fromName → $toName")
                    Text("Date départ: $departDate")
                    if (retDate != null) {
                        Text("Retour: $toName → $fromName")
                        Text("Date retour: $retDate")
                    }
                    Text("Prix actuel: ${selectedOffer!!.getFormattedPrice()}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = priceThreshold,
                        onValueChange = { priceThreshold = it },
                        label = { Text("Prix seuil") },
                        placeholder = { Text("Ex: 250") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val threshold = priceThreshold.toDoubleOrNull()
                        if (threshold != null && selectedOffer != null) {
                            onCreateAlert(selectedOffer!!, threshold)
                            showCreateAlertDialog = false
                            priceThreshold = ""
                        }
                    }
                ) {
                    Text("Créer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateAlertDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun PriceAlertCard(
    alert: PriceAlert,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (alert.status == AlertStatus.TRIGGERED) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
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
                        text = if (alert.tripType == TripType.ALLER_RETOUR) "${alert.origin} ⇄ ${alert.destination}" else "${alert.origin} → ${alert.destination}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Départ: ${alert.departureDate}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (alert.returnDate != null) {
                        Text(
                            text = "Retour: ${alert.returnDate}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Seuil: ${alert.priceThreshold.toInt()} TND",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (alert.currentPrice != null) {
                        Text(
                            text = "Prix actuel: ${alert.currentPrice.toInt()} TND",
                            fontSize = 12.sp,
                            color = if (alert.status == AlertStatus.TRIGGERED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (alert.status == AlertStatus.TRIGGERED) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status badge
                    Surface(
                        color = when (alert.status) {
                            AlertStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
                            AlertStatus.TRIGGERED -> MaterialTheme.colorScheme.tertiaryContainer
                        },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = when (alert.status) {
                                AlertStatus.ACTIVE -> "Active"
                                AlertStatus.TRIGGERED -> "Déclenchée"
                            },
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = when (alert.status) {
                                AlertStatus.ACTIVE -> MaterialTheme.colorScheme.onPrimaryContainer
                                AlertStatus.TRIGGERED -> MaterialTheme.colorScheme.onTertiaryContainer
                            }
                        )
                    }
                    
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

