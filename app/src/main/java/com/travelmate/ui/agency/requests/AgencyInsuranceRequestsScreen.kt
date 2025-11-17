package com.travelmate.ui.agency.requests

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
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
fun AgencyInsuranceRequestsScreen(
    navController: NavController,
    viewModel: AgencyInsuranceRequestsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showFilterMenu by remember { mutableStateOf(false) }
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadRequests()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Demandes d'inscription") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, "Filtrer")
                    }
                    
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Toutes") },
                            onClick = {
                                viewModel.loadRequests(null)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("En attente") },
                            onClick = {
                                viewModel.loadRequests(RequestStatus.PENDING)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Approuvées") },
                            onClick = {
                                viewModel.loadRequests(RequestStatus.APPROVED)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Rejetées") },
                            onClick = {
                                viewModel.loadRequests(RequestStatus.REJECTED)
                                showFilterMenu = false
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (state) {
            is AgencyRequestsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is AgencyRequestsState.Success -> {
                val requests = (state as AgencyRequestsState.Success).requests
                val stats = (state as AgencyRequestsState.Success).stats
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Statistiques
                    if (stats != null) {
                        StatsCard(stats = stats)
                    }
                    
                    // Liste des demandes
                    if (requests.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (selectedStatus != null) {
                                    "Aucune demande avec ce statut"
                                } else {
                                    "Aucune demande"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(requests) { request ->
                                AgencyRequestCard(
                                    request = request,
                                    onClick = {
                                        viewModel.markAsRead(request.id)
                                        navController.navigate("review_request/${request.id}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            is AgencyRequestsState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (state as AgencyRequestsState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun StatsCard(stats: com.travelmate.data.models.InsuranceRequestStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Statistiques",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Total",
                    value = stats.total.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    label = "En attente",
                    value = stats.pending.toString(),
                    color = MaterialTheme.colorScheme.secondary
                )
                StatItem(
                    label = "Approuvées",
                    value = stats.approved.toString(),
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatItem(
                    label = "Rejetées",
                    value = stats.rejected.toString(),
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            if (stats.unreadCount > 0) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "${stats.unreadCount} nouvelle(s) demande(s)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AgencyRequestCard(
    request: InsuranceRequest,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.travelerName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (!request.isRead) {
                        Surface(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.error,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "Nouveau",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                
                StatusChip(status = request.status)
            }
            
            Text(
                text = "Destination: ${request.destination}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Du ${formatDate(request.departureDate)} au ${formatDate(request.returnDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            request.travelPurpose?.let {
                Text(
                    text = "Motif: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "Soumise le ${formatDate(request.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
