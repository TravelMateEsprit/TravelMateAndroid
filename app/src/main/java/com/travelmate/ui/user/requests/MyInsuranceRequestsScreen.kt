package com.travelmate.ui.user.requests

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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyInsuranceRequestsScreen(
    navController: NavController,
    viewModel: MyInsuranceRequestsViewModel = hiltViewModel()
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
                title = { Text("Mes demandes") },
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
                                viewModel.filterByStatus(null)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("En attente") },
                            onClick = {
                                viewModel.filterByStatus(RequestStatus.PENDING)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Approuvées") },
                            onClick = {
                                viewModel.filterByStatus(RequestStatus.APPROVED)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Rejetées") },
                            onClick = {
                                viewModel.filterByStatus(RequestStatus.REJECTED)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Annulées") },
                            onClick = {
                                viewModel.filterByStatus(RequestStatus.CANCELLED)
                                showFilterMenu = false
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (state) {
            is MyRequestsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is MyRequestsState.Success -> {
                val requests = (state as MyRequestsState.Success).requests
                
                if (requests.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
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
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(requests) { request ->
                            RequestCard(
                                request = request,
                                onClick = {
                                    navController.navigate("request_details/${request.id}")
                                }
                            )
                        }
                    }
                }
            }
            
            is MyRequestsState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (state as MyRequestsState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun RequestCard(
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
                Text(
                    text = request.travelerName,
                    style = MaterialTheme.typography.titleMedium
                )
                
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
            
            Text(
                text = "Soumise le ${formatDate(request.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatusChip(status: RequestStatus) {
    val (text, color) = when (status) {
        RequestStatus.PENDING -> "En attente" to MaterialTheme.colorScheme.primary
        RequestStatus.APPROVED -> "Approuvée" to MaterialTheme.colorScheme.tertiary
        RequestStatus.REJECTED -> "Rejetée" to MaterialTheme.colorScheme.error
        RequestStatus.CANCELLED -> "Annulée" to MaterialTheme.colorScheme.surfaceVariant
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        contentColor = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
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
        dateString.take(10) // Fallback: juste la date
    }
}
