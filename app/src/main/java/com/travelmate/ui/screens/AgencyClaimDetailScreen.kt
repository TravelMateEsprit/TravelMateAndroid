package com.travelmate.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
            if (selectedClaim != null && selectedClaim?.status != "FERME") {
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
                        color = Color(0xFFFF9800)
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
                        Color(0xFFFF9800),
                        Color(0xFFF57C00)
                    )
                )
            )
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
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
                Text(
                    text = claim?.ticketNumber ?: "...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = claim?.insuranceRequestId?.travelerName ?: "Client",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            
            // Status badge
            if (claim != null) {
                TextButton(
                    onClick = onStatusClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = when (claim.status) {
                            "OUVERT" -> "Nouveau"
                            "EN_COURS" -> "En cours"
                            "RESOLU" -> "Résolu"
                            "FERME" -> "Fermé"
                            else -> claim.status
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subject
            InfoDetailRow(
                icon = Icons.Outlined.Assignment,
                label = "Objet",
                value = claim.subject,
                iconColor = Color(0xFFFF9800)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Description
            InfoDetailRow(
                icon = Icons.Outlined.Description,
                label = "Description",
                value = claim.description,
                iconColor = Color(0xFFFF9800)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Category
            InfoDetailRow(
                icon = Icons.Outlined.Category,
                label = "Catégorie",
                value = claim.category.toString(),
                iconColor = Color(0xFFFF9800)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Date
            InfoDetailRow(
                icon = Icons.Outlined.Schedule,
                label = "Date de création",
                value = formatDate(claim.createdAt),
                iconColor = Color(0xFFFF9800)
            )
            
            // Insurance details
            if (claim.insuranceRequestId != null) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                InfoDetailRow(
                    icon = Icons.Outlined.Shield,
                    label = "Destination",
                    value = claim.insuranceRequestId.destination,
                    iconColor = Color(0xFFFF9800)
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
                contentPadding = PaddingValues(vertical = 16.dp, horizontal = 12.dp)
            ) {
                // Initial message (claim description)
                item {
                    AgencyMessageBubble(
                        messageText = claim.description,
                        senderName = claim.insuranceRequestId?.travelerName ?: "Client",
                        timestamp = claim.createdAt,
                        isAgency = false
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // All subsequent messages
                items(claim.messages) { message ->
                    AgencyMessageBubble(
                        messageText = message.message,
                        senderName = message.sender.name,
                        timestamp = message.createdAt,
                        isAgency = message.senderRole == "agency"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isAgency) Arrangement.End else Arrangement.Start
    ) {
        if (!isAgency) {
            // Client avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1976D2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isAgency) Alignment.End else Alignment.Start
        ) {
            // Sender name
            if (senderName != null) {
                Text(
                    text = senderName,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
            
            Card(
                shape = RoundedCornerShape(
                    topStart = if (isAgency) 16.dp else 4.dp,
                    topEnd = if (isAgency) 4.dp else 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAgency) Color(0xFFFF9800) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = messageText,
                        fontSize = 15.sp,
                        color = if (isAgency) Color.White else Color(0xFF1E293B),
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = formatRelativeTime(timestamp),
                        fontSize = 11.sp,
                        color = if (isAgency) Color.White.copy(alpha = 0.7f) else Color(0xFF94A3B8)
                    )
                }
            }
        }
        
        if (isAgency) {
            Spacer(modifier = Modifier.width(8.dp))
            // Agency avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF9800)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Business,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
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
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Réponse au client...") },
                enabled = enabled,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF9800),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                maxLines = 4
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            FloatingActionButton(
                onClick = onSendClick,
                modifier = Modifier.size(48.dp),
                containerColor = if (messageText.isBlank()) Color(0xFFE2E8F0) else Color(0xFFFF9800),
                contentColor = if (messageText.isBlank()) Color(0xFF94A3B8) else Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = if (messageText.isBlank()) 0.dp else 4.dp
                )
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Envoyer",
                        modifier = Modifier.size(24.dp)
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
            Text(
                text = "Changer le statut",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                val statuses = listOf(
                    Pair("EN_COURS", "En cours"),
                    Pair("RESOLU", "Résolu"),
                    Pair("FERME", "Fermé")
                )
                
                statuses.forEach { (status, label) ->
                    Card(
                        onClick = {
                            onStatusChange(status)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentStatus == status) 
                                Color(0xFFFF9800).copy(alpha = 0.1f) 
                            else Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (currentStatus == status) 2.dp else 1.dp,
                            color = if (currentStatus == status) Color(0xFFFF9800) else Color(0xFFE2E8F0)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
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
                                tint = if (currentStatus == status) Color(0xFFFF9800) else Color(0xFF64748B),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                fontSize = 16.sp,
                                fontWeight = if (currentStatus == status) FontWeight.Bold else FontWeight.Normal,
                                color = if (currentStatus == status) Color(0xFFFF9800) else Color(0xFF1E293B)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
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
