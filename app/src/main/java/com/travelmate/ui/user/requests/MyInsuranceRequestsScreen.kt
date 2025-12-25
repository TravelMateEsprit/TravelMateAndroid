package com.travelmate.ui.user.requests

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.travelmate.data.models.InsuranceRequest
import com.travelmate.data.models.RequestStatus
import com.travelmate.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MyInsuranceRequestsScreen(
    navController: NavController,
    viewModel: MyInsuranceRequestsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showFilterMenu by remember { mutableStateOf(false) }
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    
    LaunchedEffect(Unit) {
        viewModel.loadRequests()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Mes demandes",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Badge(
                                containerColor = if (selectedStatus != null) colorScheme.primary else Color.Transparent
                            ) {
                                Icon(Icons.Default.FilterList, "Filtrer")
                            }
                        }
                        
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false },
                            containerColor = colorScheme.surface
                        ) {
                            FilterMenuItem(
                                text = "Toutes",
                                icon = Icons.Default.List,
                                isSelected = selectedStatus == null,
                                onClick = {
                                    viewModel.filterByStatus(null)
                                    showFilterMenu = false
                                },
                                colorScheme = colorScheme
                            )
                            Divider(color = colorScheme.outline.copy(alpha = 0.3f))
                            FilterMenuItem(
                                text = "En attente",
                                icon = Icons.Default.HourglassEmpty,
                                isSelected = selectedStatus == RequestStatus.PENDING,
                                onClick = {
                                    viewModel.filterByStatus(RequestStatus.PENDING)
                                    showFilterMenu = false
                                },
                                colorScheme = colorScheme
                            )
                            FilterMenuItem(
                                text = "Approuvées",
                                icon = Icons.Default.CheckCircle,
                                isSelected = selectedStatus == RequestStatus.APPROVED,
                                onClick = {
                                    viewModel.filterByStatus(RequestStatus.APPROVED)
                                    showFilterMenu = false
                                },
                                colorScheme = colorScheme
                            )
                            FilterMenuItem(
                                text = "Rejetées",
                                icon = Icons.Default.Cancel,
                                isSelected = selectedStatus == RequestStatus.REJECTED,
                                onClick = {
                                    viewModel.filterByStatus(RequestStatus.REJECTED)
                                    showFilterMenu = false
                                },
                                colorScheme = colorScheme
                            )
                            FilterMenuItem(
                                text = "Annulées",
                                icon = Icons.Default.Block,
                                isSelected = selectedStatus == RequestStatus.CANCELLED,
                                onClick = {
                                    viewModel.filterByStatus(RequestStatus.CANCELLED)
                                    showFilterMenu = false
                                },
                                colorScheme = colorScheme
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.primary,
                    titleContentColor = colorScheme.onPrimary,
                    navigationIconContentColor = colorScheme.onPrimary,
                    actionIconContentColor = colorScheme.onPrimary
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
            // Filtre actif badge
            AnimatedVisibility(
                visible = selectedStatus != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Filtre actif: ${getStatusText(selectedStatus)}",
                                color = colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        IconButton(
                            onClick = { viewModel.filterByStatus(null) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Supprimer le filtre",
                                tint = colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            
            when (state) {
                is MyRequestsState.Loading -> {
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
                
                is MyRequestsState.Success -> {
                    val requests = (state as MyRequestsState.Success).requests
                    
                    if (requests.isEmpty()) {
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
                                    Icons.Default.Assignment,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                                Text(
                                    text = if (selectedStatus != null) {
                                        "Aucune demande avec ce statut"
                                    } else {
                                        "Aucune demande"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = "Vos demandes d'assurance apparaîtront ici",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
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
                            items(requests, key = { it.id }) { request ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    EnhancedRequestCard(
                                        request = request,
                                        onClick = {
                                            navController.navigate("request_details/${request.id}")
                                        },
                                        colorScheme = colorScheme
                                    )
                                }
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
                                text = (state as MyRequestsState.Error).message,
                                color = colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterMenuItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    colorScheme: ColorScheme
) {
    DropdownMenuItem(
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) colorScheme.primary else colorScheme.onSurface
                )
            }
        },
        onClick = onClick,
        colors = MenuDefaults.itemColors(
            textColor = if (isSelected) colorScheme.primary else colorScheme.onSurface
        )
    )
}

private fun getStatusText(status: RequestStatus?): String {
    return when (status) {
        RequestStatus.PENDING -> "En attente"
        RequestStatus.APPROVED -> "Approuvées"
        RequestStatus.REJECTED -> "Rejetées"
        RequestStatus.CANCELLED -> "Annulées"
        null -> "Toutes"
    }
}

@Composable
fun EnhancedRequestCard(
    request: InsuranceRequest,
    onClick: () -> Unit,
    colorScheme: ColorScheme = MaterialTheme.colorScheme
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header avec nom et statut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = request.travelerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = formatDate(request.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
                
                EnhancedStatusChip(status = request.status, colorScheme = colorScheme)
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            
            // Informations du voyage
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow(
                    icon = Icons.Default.Public,
                    label = "Destination",
                    value = request.destination,
                    colorScheme = colorScheme
                )
                
                InfoRow(
                    icon = Icons.Default.DateRange,
                    label = "Période",
                    value = "${formatDate(request.departureDate)} - ${formatDate(request.returnDate)}",
                    colorScheme = colorScheme
                )
                
                request.travelPurpose?.let { purpose ->
                    InfoRow(
                        icon = Icons.Default.WorkOutline,
                        label = "Motif",
                        value = purpose,
                        colorScheme = colorScheme
                    )
                }
            }
            
            // Bouton d'action selon le statut
            when (request.status) {
                RequestStatus.APPROVED -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        AssistChip(
                            onClick = onClick,
                            label = { Text("Voir détails") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = colorScheme.primaryContainer.copy(alpha = 0.3f),
                                labelColor = colorScheme.primary,
                                leadingIconContentColor = colorScheme.primary
                            )
                        )
                    }
                }
                else -> {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    colorScheme: ColorScheme = MaterialTheme.colorScheme
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun StatusChip(status: RequestStatus) {
    EnhancedStatusChip(status = status)
}

@Composable
fun EnhancedStatusChip(
    status: RequestStatus,
    colorScheme: ColorScheme = MaterialTheme.colorScheme
) {
    val (text, icon, containerColor, contentColor) = when (status) {
        RequestStatus.PENDING -> Quadruple(
            "En attente",
            Icons.Default.HourglassEmpty,
            colorScheme.primary.copy(alpha = 0.15f),
            colorScheme.primary
        )
        RequestStatus.APPROVED -> Quadruple(
            "Approuvée",
            Icons.Default.CheckCircle,
            ColorSuccess.copy(alpha = 0.15f),
            ColorSuccess
        )
        RequestStatus.REJECTED -> Quadruple(
            "Rejetée",
            Icons.Default.Cancel,
            colorScheme.error.copy(alpha = 0.15f),
            colorScheme.error
        )
        RequestStatus.CANCELLED -> Quadruple(
            "Annulée",
            Icons.Default.Block,
            colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
            colorScheme.onSurfaceVariant
        )
    }
    
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 6.dp, end = 12.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

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
