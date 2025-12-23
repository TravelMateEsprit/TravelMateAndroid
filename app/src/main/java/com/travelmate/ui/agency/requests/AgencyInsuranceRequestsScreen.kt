package com.travelmate.ui.agency.requests

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.travelmate.data.models.InsuranceRequest
import com.travelmate.data.models.RequestStatus
import com.travelmate.ui.user.requests.StatusChip
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
                title = { 
                    Text(
                        "Demandes d'inscription",
                        fontWeight = FontWeight.Bold
                    )
                },
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
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
                            contentPadding = PaddingValues(
                                start = 16.dp,
                            end = 16.dp,
                                top = 16.dp,
                                bottom = 96.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
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
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tableau de bord",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (stats.unreadCount > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = CircleShape
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stats.unreadCount.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Modern stats grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EnhancedStatCard(
                        title = "Total",
                        value = stats.total.toString(),
                        icon = Icons.Default.List,
                        color = ColorTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    EnhancedStatCard(
                        title = "En attente",
                        value = stats.pending.toString(),
                        icon = Icons.Default.Pending,
                        color = ColorPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EnhancedStatCard(
                        title = "Approuvées",
                        value = stats.approved.toString(),
                        icon = Icons.Default.CheckCircle,
                        color = ColorSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    
                    EnhancedStatCard(
                        title = "Rejetées",
                        value = stats.rejected.toString(),
                        icon = Icons.Default.Cancel,
                        color = ColorError,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            if (stats.unreadCount > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.error,
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${stats.unreadCount} nouvelle(s) demande(s) non lue(s)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = ColorTextSecondary
            )
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
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar with first letter
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = ColorPrimary.copy(alpha = 0.2f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = request.travelerName.firstOrNull()?.toString()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.titleLarge,
                                color = ColorPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = request.travelerName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (!request.isRead) {
                                Surface(
                                    color = MaterialTheme.colorScheme.error,
                                    shape = CircleShape
                                ) {
                                    Box(
                                        modifier = Modifier.size(8.dp)
                                    )
                                }
                            }
                        }
                        
                        Text(
                            text = "Soumise le ${formatDate(request.createdAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ColorTextSecondary
                        )
                    }
                }
                
                EnhancedStatusChip(status = request.status)
            }
            
            Divider(color = ColorTextSecondary.copy(alpha = 0.2f))
            
            // Trip details with icons
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "Destination",
                    value = request.destination
                )
                
                InfoRow(
                    icon = Icons.Default.DateRange,
                    label = "Période",
                    value = "${formatDate(request.departureDate)} → ${formatDate(request.returnDate)}"
                )
                
                request.travelPurpose?.let {
                    InfoRow(
                        icon = Icons.Default.Info,
                        label = "Motif",
                        value = it
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ColorPrimary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = ColorTextSecondary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = ColorTextPrimary
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
