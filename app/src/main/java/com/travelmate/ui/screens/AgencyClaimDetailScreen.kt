package com.travelmate.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.travelmate.data.models.Claim
import com.travelmate.data.models.ClaimMessage
import com.travelmate.ui.components.*
import com.travelmate.viewmodel.ClaimViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyClaimDetailScreen(
    navController: NavController,
    claimId: String,
    viewModel: ClaimViewModel = hiltViewModel()
) {
    val selectedClaim by viewModel.selectedClaim.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var messageText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(claimId) {
        viewModel.loadClaimById(claimId)
    }
    
    // Scroll to bottom when messages change
    LaunchedEffect(selectedClaim?.messages?.size) {
        selectedClaim?.messages?.let {
            if (it.isNotEmpty()) {
                coroutineScope.launch {
                    listState.animateScrollToItem(it.size)
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            AgencyChatTopBar(
                claim = selectedClaim,
                onBackClick = { navController.popBackStack() },
                onInfoClick = { showDetails = !showDetails },
                onStatusClick = { showStatusMenu = true }
            )
        },
        bottomBar = {
            if (selectedClaim != null) {
                if (selectedClaim?.status == "FERME") {
                    // Barre d'information pour ticket fermé
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF8F9FA),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Ce ticket a été clôturé",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                } else {
                    Column {
                        // Bouton pour terminer le ticket si résolu
                        if (selectedClaim?.status == "RESOLU") {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFF0F9FF),
                                shadowElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Problème résolu",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF1E293B)
                                        )
                                    }
                                    
                                    Button(
                                        onClick = {
                                            viewModel.updateClaimStatus(claimId, "FERME")
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF4CAF50)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Terminer le ticket")
                                    }
                                }
                            }
                        }
                        
                        AgencyMessageInputBar(
                            messageText = messageText,
                            onMessageChange = { messageText = it },
                            onSendClick = {
                                if (messageText.isNotBlank()) {
                                    isSending = true
                                    viewModel.addMessage(claimId, messageText)
                                    messageText = ""
                                    isSending = false
                                }
                            },
                            isSending = isSending,
                            enabled = !isSending
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
        ) {
            when {
                isLoading -> {
                    LoadingView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        color = Color(0xFF2196F3)
                    )
                }
                error != null -> {
                    ErrorView(
                        error = error ?: "Erreur inconnue",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
                selectedClaim != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        AnimatedVisibility(
                            visible = showDetails,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            AgencyClaimInfoPanel(claim = selectedClaim!!)
                        }
                        
                        AgencyChatMessagesView(
                            claim = selectedClaim!!,
                            listState = listState,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Status change menu
            if (showStatusMenu && selectedClaim != null) {
                StatusChangeDialog(
                    currentStatus = selectedClaim!!.status,
                    onDismiss = { showStatusMenu = false },
                    onStatusChange = { newStatus ->
                        viewModel.updateClaimStatus(claimId, newStatus)
                        showStatusMenu = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyChatTopBar(
    claim: Claim?,
    onBackClick: () -> Unit,
    onInfoClick: () -> Unit,
    onStatusClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2196F3),
                        Color(0xFF1976D2)
                    )
                )
            )
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 12.dp, end = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Retour",
                    tint = Color.White
                )
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = claim?.ticketNumber ?: "...",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (claim != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = when (claim.status) {
                                "OUVERT" -> Color(0xFF2196F3).copy(alpha = 0.3f)
                                "EN_COURS" -> Color(0xFFFF9800).copy(alpha = 0.3f)
                                "RESOLU" -> Color(0xFF4CAF50).copy(alpha = 0.3f)
                                "FERME" -> Color(0xFF64748B).copy(alpha = 0.3f)
                                else -> Color(0xFF2196F3).copy(alpha = 0.3f)
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = when (claim.status) {
                                    "OUVERT" -> "Nouveau"
                                    "EN_COURS" -> "En cours"
                                    "RESOLU" -> "Résolu"
                                    "FERME" -> "Fermé"
                                    else -> claim.status
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(start = 6.dp, top = 2.dp, end = 6.dp, bottom = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = claim?.insuranceRequestId?.travelerName ?: "Client",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            
            // Bouton pour changer le statut - Plus visible
            IconButton(
                onClick = onStatusClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Changer le statut"
                )
            }
            
            IconButton(onClick = onInfoClick) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = "Informations",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun AgencyClaimInfoPanel(claim: Claim) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    text = "Informations client",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                
                ModernPriorityBadge(priority = claim.priority)
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Subject
            InfoDetailRow(
                icon = Icons.Outlined.Assignment,
                label = "Objet",
                value = claim.subject,
                iconColor = Color(0xFF2196F3)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Description
            InfoDetailRow(
                icon = Icons.Outlined.Description,
                label = "Description",
                value = claim.description,
                iconColor = Color(0xFF2196F3)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Category
            InfoDetailRow(
                icon = Icons.Outlined.Category,
                label = "Catégorie",
                value = claim.category.toString(),
                iconColor = Color(0xFF2196F3)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Date
            InfoDetailRow(
                icon = Icons.Outlined.Schedule,
                label = "Date de création",
                value = formatDate(claim.createdAt),
                iconColor = Color(0xFF2196F3)
            )
            
            // Insurance details
            if (claim.insuranceRequestId != null) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                InfoDetailRow(
                    icon = Icons.Outlined.Shield,
                    label = "Destination",
                    value = claim.insuranceRequestId.destination,
                    iconColor = Color(0xFF2196F3)
                )
            }
        }
    }
}

@Composable
fun AgencyChatMessagesView(
    claim: Claim,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (claim.messages.isEmpty()) {
            AgencyEmptyChatView()
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 4.dp, top = 18.dp, end = 4.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Initial message (claim description)
                item {
                    AgencyMessageBubble(
                        messageText = claim.description,
                        senderName = claim.insuranceRequestId?.travelerName ?: "Client",
                        timestamp = claim.createdAt,
                        isAgency = false
                    )
                }
                
                // All subsequent messages
                items(claim.messages) { message ->
                    AgencyMessageBubble(
                        messageText = message.message,
                        senderName = message.sender.name,
                        timestamp = message.createdAt,
                        isAgency = message.senderRole == "agency"
                    )
                }
            }
        }
    }
}

@Composable
fun AgencyEmptyChatView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.ChatBubbleOutline,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFFCBD5E1)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Pas encore de conversation",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Envoyez le premier message au client",
            fontSize = 14.sp,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AgencyMessageBubble(
    messageText: String,
    senderName: String?,
    timestamp: String,
    isAgency: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp),
        horizontalArrangement = if (isAgency) Arrangement.End else Arrangement.Start
    ) {
        if (!isAgency) {
            // Client avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E88E5),
                                Color(0xFF1976D2)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }
        
        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isAgency) Alignment.End else Alignment.Start
        ) {
            // Sender name
            if (senderName != null) {
                Text(
                    text = senderName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(start = 14.dp, top = 4.dp, end = 14.dp, bottom = 4.dp)
                )
            }
            
            Card(
                shape = RoundedCornerShape(
                    topStart = if (isAgency) 20.dp else 4.dp,
                    topEnd = if (isAgency) 4.dp else 20.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAgency) Color(0xFF2196F3) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 12.dp)
                ) {
                    Text(
                        text = messageText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isAgency) Color.White else Color(0xFF1E293B),
                        lineHeight = 22.sp
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = formatRelativeTime(timestamp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isAgency) Color.White.copy(alpha = 0.8f) else Color(0xFF94A3B8)
                    )
                }
            }
        }
        
        if (isAgency) {
            Spacer(modifier = Modifier.width(10.dp))
            // Agency avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF2196F3),
                                Color(0xFF1976D2)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Business,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AgencyMessageInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isSending: Boolean,
    enabled: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { 
                    Text(
                        "Réponse au client...",
                        color = Color(0xFF94A3B8)
                    ) 
                },
                enabled = enabled,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedContainerColor = Color(0xFFF8FAFC)
                ),
                maxLines = 5,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            FloatingActionButton(
                onClick = onSendClick,
                modifier = Modifier.size(52.dp),
                containerColor = if (messageText.isBlank()) Color(0xFFE2E8F0) else Color(0xFF2196F3),
                contentColor = if (messageText.isBlank()) Color(0xFF94A3B8) else Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = if (messageText.isBlank()) 0.dp else 6.dp,
                    pressedElevation = 12.dp
                ),
                shape = CircleShape
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Énoncer",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusChangeDialog(
    currentStatus: String,
    onDismiss: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Changer le statut du ticket",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Statut actuel : ${when (currentStatus) {
                        "OUVERT" -> "🔵 Nouveau"
                        "EN_COURS" -> "🟡 En cours"
                        "RESOLU" -> "🟢 Résolu"
                        "FERME" -> "🔒 Fermé"
                        else -> currentStatus
                    }}",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Normal
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Info card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF0F9FF)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pour fermer un ticket, marquez-le d'abord comme résolu.",
                            fontSize = 12.sp,
                            color = Color(0xFF1E293B),
                            lineHeight = 16.sp
                        )
                    }
                }
                
                val statuses = listOf(
                    Triple("EN_COURS", "En cours", "Le problème est en cours de traitement"),
                    Triple("RESOLU", "Résolu", "Le problème a été résolu"),
                    Triple("FERME", "Fermé", "Ticket clôturé - plus de messages possibles")
                )
                
                statuses.forEach { (status, label, description) ->
                    val isDisabled = status == "FERME" && currentStatus != "RESOLU"
                    val isSelected = currentStatus == status
                    
                    Card(
                        onClick = {
                            if (!isDisabled) {
                                onStatusChange(status)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .alpha(if (isDisabled) 0.5f else 1f),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isSelected -> Color(0xFF2196F3).copy(alpha = 0.12f)
                                isDisabled -> Color(0xFFF5F5F5)
                                else -> Color.White
                            }
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = when {
                                isSelected -> Color(0xFF2196F3)
                                isDisabled -> Color(0xFFE0E0E0)
                                else -> Color(0xFFE2E8F0)
                            }
                        ),
                        enabled = !isDisabled
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                when (status) {
                                    "EN_COURS" -> Icons.Outlined.Autorenew
                                    "RESOLU" -> Icons.Outlined.CheckCircle
                                    "FERME" -> Icons.Outlined.Lock
                                    else -> Icons.Outlined.Circle
                                },
                                contentDescription = null,
                                tint = when {
                                    isSelected -> Color(0xFF2196F3)
                                    isDisabled -> Color(0xFFBDBDBD)
                                    else -> Color(0xFF64748B)
                                },
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = label,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = when {
                                        isSelected -> Color(0xFF2196F3)
                                        isDisabled -> Color(0xFFBDBDBD)
                                        else -> Color(0xFF1E293B)
                                    }
                                )
                                if (isDisabled && status == "FERME") {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "⚠️ Résolvez d'abord",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFF9800),
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = Color(0xFF64748B))
            }
        }
    )
}

@Composable
fun LoadingView(modifier: Modifier = Modifier, color: Color = Color(0xFF1976D2)) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = color)
    }
}
