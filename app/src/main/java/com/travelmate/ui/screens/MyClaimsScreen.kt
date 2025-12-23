@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.travelmate.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.travelmate.data.models.Claim
import com.travelmate.ui.components.*
import com.travelmate.utils.Constants
import com.travelmate.viewmodel.ClaimViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MyClaimsScreen(
    navController: NavController,
    viewModel: ClaimViewModel = hiltViewModel()
) {
    val claims by viewModel.myClaims.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var selectedFilter by remember { mutableStateOf("TOUS") }
    
    val filteredClaims = when (selectedFilter) {
        "OUVERT" -> claims.filter { it.status == "OUVERT" }
        "EN_COURS" -> claims.filter { it.status == "EN_COURS" }
        "RESOLU" -> claims.filter { it.status == "RESOLU" || it.status == "FERME" }
        else -> claims
    }
    
    LaunchedEffect(Unit) {
        viewModel.loadMyClaims()
        viewModel.loadUnreadCount()
    }
    
    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1E88E5),
                                    Color(0xFF1976D2)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Support Client",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${claims.size} ticket(s) • ${claims.count { !it.isReadByUser }} non lu(s)",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        
                        IconButton(
                            onClick = { viewModel.loadMyClaims() },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Constants.Routes.CREATE_CLAIM) },
                containerColor = Color(0xFF1976D2),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nouveau Ticket", fontWeight = FontWeight.SemiBold) },
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                )
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F7FA))
        ) {
            // Filtres modernes
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChipModern(
                        label = "Tous",
                        count = claims.size,
                        selected = selectedFilter == "TOUS",
                        onClick = { selectedFilter = "TOUS" },
                        color = Color(0xFF1976D2)
                    )
                    FilterChipModern(
                        label = "Ouverts",
                        count = claims.count { it.status == "OUVERT" },
                        selected = selectedFilter == "OUVERT",
                        onClick = { selectedFilter = "OUVERT" },
                        color = Color(0xFFFF9800)
                    )
                    FilterChipModern(
                        label = "En cours",
                        count = claims.count { it.status == "EN_COURS" },
                        selected = selectedFilter == "EN_COURS",
                        onClick = { selectedFilter = "EN_COURS" },
                        color = Color(0xFF2196F3)
                    )
                    FilterChipModern(
                        label = "Résolus",
                        count = claims.count { it.status == "RESOLU" || it.status == "FERME" },
                        selected = selectedFilter == "RESOLU",
                        onClick = { selectedFilter = "RESOLU" },
                        color = Color(0xFF4CAF50)
                    )
                }
            }
            
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF1976D2))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Chargement des tickets...",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                error != null -> {
                    EmptyStateView(
                        icon = Icons.Default.Warning,
                        title = "Erreur de chargement",
                        message = error ?: "Une erreur est survenue",
                        actionLabel = "Réessayer",
                        onAction = { viewModel.loadMyClaims() },
                        color = Color(0xFFF44336)
                    )
                }
                filteredClaims.isEmpty() -> {
                    EmptyStateView(
                        icon = Icons.Outlined.ConfirmationNumber,
                        title = if (selectedFilter == "TOUS") "Aucun ticket" else "Aucun ticket $selectedFilter",
                        message = if (selectedFilter == "TOUS") 
                            "Créez votre premier ticket de support" 
                        else 
                            "Aucun ticket avec ce statut",
                        actionLabel = if (selectedFilter == "TOUS") "Créer un ticket" else null,
                        onAction = if (selectedFilter == "TOUS") {
                            { navController.navigate(Constants.Routes.CREATE_CLAIM) }
                        } else null,
                        color = Color(0xFF1976D2)
                    )
                }
                else -> {
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
                        items(filteredClaims) { claim ->
                            ModernTicketCard(
                                claim = claim,
                                onClick = { navController.navigate("${Constants.Routes.CLAIM_DETAIL}/${claim._id}") }
                            )
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipModern(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    color: Color
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) color else Color.White,
        border = if (!selected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)) else null,
        shadowElevation = if (selected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else Color(0xFF424242)
            )
            if (count > 0) {
                Surface(
                    shape = CircleShape,
                    color = if (selected) Color.White.copy(alpha = 0.3f) else color.copy(alpha = 0.15f),
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$count",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else color
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernTicketCard(claim: Claim, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // En-tête avec numéro de ticket
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.ConfirmationNumber,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF1976D2)
                    )
                    Text(
                        text = claim.ticketNumber ?: "#${claim._id.take(8)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                }
                
                if (!claim.isReadByUser) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF44336)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .padding(2.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Sujet du ticket
            Text(
                text = claim.subject,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description
            Text(
                text = claim.description,
                fontSize = 14.sp,
                color = Color(0xFF757575),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Métadonnées
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModernStatusBadge(status = claim.status)
                
                if (claim.priority != "BASSE") {
                    ModernPriorityBadge(priority = claim.priority)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Divider(color = Color(0xFFEEEEEE))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Footer avec date et réponses
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF9E9E9E)
                    )
                    Text(
                        text = formatRelativeTime(claim.createdAt),
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
                
                if (claim.responseCount > 0 || claim.agencyResponse != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .background(
                                Color(0xFF4CAF50).copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(start = 10.dp, top = 4.dp, end = 10.dp, bottom = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF4CAF50)
                        )
                        Text(
                            text = "Répondu",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernStatusBadge(status: String) {
    val (icon, backgroundColor, textColor, label) = when (status) {
        "OUVERT" -> Quadruple(
            Icons.Outlined.WatchLater,
            Color(0xFFFFF3E0),
            Color(0xFFE65100),
            "Ouvert"
        )
        "EN_COURS" -> Quadruple(
            Icons.Outlined.AutoMode,
            Color(0xFFE3F2FD),
            Color(0xFF1976D2),
            "En cours"
        )
        "EN_ATTENTE_CLIENT" -> Quadruple(
            Icons.Outlined.HourglassEmpty,
            Color(0xFFFFF9C4),
            Color(0xFFF57F17),
            "En attente"
        )
        "RESOLU" -> Quadruple(
            Icons.Outlined.CheckCircle,
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            "Résolu"
        )
        "FERME" -> Quadruple(
            Icons.Outlined.Lock,
            Color(0xFFECEFF1),
            Color(0xFF455A64),
            "Fermé"
        )
        else -> Quadruple(
            Icons.Outlined.Info,
            Color(0xFFF5F5F5),
            Color(0xFF616161),
            status
        )
    }
    
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, top = 6.dp, end = 10.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = textColor
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

@Composable
fun ModernPriorityBadge(priority: String) {
    val (icon, backgroundColor, textColor, label) = when (priority) {
        "BASSE" -> Quadruple(
            Icons.Outlined.KeyboardArrowDown,
            Color(0xFFE8F5E9),
            Color(0xFF388E3C),
            "Basse"
        )
        "MOYENNE" -> Quadruple(
            Icons.Outlined.DragHandle,
            Color(0xFFFFF9C4),
            Color(0xFFF57F17),
            "Moyenne"
        )
        "HAUTE" -> Quadruple(
            Icons.Outlined.KeyboardArrowUp,
            Color(0xFFFFE0B2),
            Color(0xFFE65100),
            "Haute"
        )
        "URGENTE" -> Quadruple(
            Icons.Outlined.PriorityHigh,
            Color(0xFFFFCDD2),
            Color(0xFFC62828),
            "Urgent"
        )
        else -> Quadruple(
            Icons.Outlined.Info,
            Color(0xFFF5F5F5),
            Color(0xFF616161),
            priority
        )
    }
    
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, top = 6.dp, end = 10.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = textColor
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

@Composable
fun EmptyStateView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    color: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = color
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            fontSize = 14.sp,
            color = Color(0xFF757575),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = color),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = actionLabel,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
