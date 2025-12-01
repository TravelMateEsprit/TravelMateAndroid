package com.travelmate.ui.screens.claims

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.travelmate.data.models.Claim
import com.travelmate.data.models.ClaimStatus
import com.travelmate.data.models.ClaimPriority
import com.travelmate.data.models.ClaimCategory
import com.travelmate.utils.Constants
import com.travelmate.viewmodel.ClaimViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimsListScreen(
    navController: NavController,
    viewModel: ClaimViewModel = hiltViewModel()
) {
    val claims by viewModel.myClaims.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMyClaims()
        viewModel.loadUnreadCount()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Mes Tickets")
                        if (unreadCount > 0) {
                            Text(
                                "$unreadCount message${if(unreadCount > 1) "s" else ""} non lu${if(unreadCount > 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Constants.Routes.CREATE_CLAIM) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nouveau Ticket") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading && claims.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                claims.isEmpty() -> {
                    EmptyClaimsState(
                        onCreateClick = { navController.navigate(Constants.Routes.CREATE_CLAIM) }
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(claims) { claim ->
                            ClaimCard(
                                claim = claim,
                                onClick = { navController.navigate("${Constants.Routes.CLAIM_DETAIL}/${claim._id}") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClaimCard(
    claim: Claim,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (claim.unreadByUser > 0) 
                MaterialTheme.colorScheme.secondaryContainer
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = claim.ticketNumber,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = claim.subject,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (claim.unreadByUser > 0) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                if (claim.unreadByUser > 0) {
                    @OptIn(ExperimentalMaterial3Api::class)
                    Badge(
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(claim.unreadByUser.toString())
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(status = claim.status)
                PriorityChip(priority = claim.priority)
                CategoryChip(category = claim.category)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Message,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${claim.messages.size} message${if(claim.messages.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = formatDate(claim.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val statusInfo = ClaimStatus.fromValue(status)
    val color = when (statusInfo) {
        ClaimStatus.OPEN -> Color(0xFFE3F2FD)
        ClaimStatus.IN_PROGRESS -> Color(0xFFFFF3E0)
        ClaimStatus.WAITING_USER -> Color(0xFFFCE4EC)
        ClaimStatus.RESOLVED -> Color(0xFFE8F5E9)
        ClaimStatus.CLOSED -> Color(0xFFEEEEEE)
        else -> Color.LightGray
    }
    
    val textColor = when (statusInfo) {
        ClaimStatus.OPEN -> Color(0xFF1976D2)
        ClaimStatus.IN_PROGRESS -> Color(0xFFF57C00)
        ClaimStatus.WAITING_USER -> Color(0xFFC2185B)
        ClaimStatus.RESOLVED -> Color(0xFF388E3C)
        ClaimStatus.CLOSED -> Color(0xFF616161)
        else -> Color.DarkGray
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color
    ) {
        Text(
            text = statusInfo?.displayName ?: status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PriorityChip(priority: String) {
    val priorityInfo = ClaimPriority.fromValue(priority)
    val color = when (priorityInfo) {
        ClaimPriority.LOW -> Color(0xFFE0E0E0)
        ClaimPriority.MEDIUM -> Color(0xFFFFF9C4)
        ClaimPriority.HIGH -> Color(0xFFFFCCBC)
        ClaimPriority.URGENT -> Color(0xFFFFCDD2)
        else -> Color.LightGray
    }
    
    val icon = when (priorityInfo) {
        ClaimPriority.URGENT -> "!!"
        ClaimPriority.HIGH -> "!"
        else -> null
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (icon != null) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
            Text(
                text = priorityInfo?.displayName ?: priority,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun CategoryChip(category: String) {
    val categoryInfo = ClaimCategory.fromValue(category)
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = categoryInfo?.displayName ?: category,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyClaimsState(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.SupportAgent,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Aucun ticket",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Vous n'avez pas encore créé de ticket de réclamation",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onCreateClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Créer un ticket")
        }
    }
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}
