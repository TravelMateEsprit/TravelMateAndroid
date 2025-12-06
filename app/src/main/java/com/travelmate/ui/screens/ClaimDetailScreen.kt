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
fun ClaimDetailScreen(
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
            ModernChatTopBar(
                claim = selectedClaim,
                onBackClick = { navController.popBackStack() },
                onInfoClick = { showDetails = !showDetails }
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
                                .padding(horizontal = 20.dp, vertical = 16.dp),
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
                    MessageInputBar(
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
                            .padding(paddingValues)
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
                            ClaimInfoPanel(claim = selectedClaim!!)
                        }
                        
                        ChatMessagesView(
                            claim = selectedClaim!!,
                            listState = listState,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernChatTopBar(
    claim: Claim?,
    onBackClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    Box(
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
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
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
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (claim != null) {
                        ModernStatusBadge(status = claim.status)
                    }
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
fun ClaimInfoPanel(claim: Claim) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Détails du ticket",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Subject
            InfoDetailRow(
                icon = Icons.Outlined.Assignment,
                label = "Objet",
                value = claim.subject,
                iconColor = Color(0xFF1976D2)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Category
            InfoDetailRow(
                icon = Icons.Outlined.Category,
                label = "Catégorie",
                value = claim.category.toString(),
                iconColor = Color(0xFF1976D2)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.PriorityHigh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF1976D2)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Priorité",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.weight(1f)
                )
                ModernPriorityBadge(priority = claim.priority)
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Date
            InfoDetailRow(
                icon = Icons.Outlined.Schedule,
                label = "Créé le",
                value = formatDate(claim.createdAt),
                iconColor = Color(0xFF1976D2)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Agency
            InfoDetailRow(
                icon = Icons.Outlined.Business,
                label = "Agence",
                value = claim.agencyId.name,
                iconColor = Color(0xFF1976D2)
            )
            
            // Insurance details
            if (claim.insuranceRequestId != null) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                InfoDetailRow(
                    icon = Icons.Outlined.Shield,
                    label = "Destination",
                    value = claim.insuranceRequestId.destination,
                    iconColor = Color(0xFF1976D2)
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                InfoDetailRow(
                    icon = Icons.Outlined.Person,
                    label = "Voyageur",
                    value = claim.insuranceRequestId.travelerName,
                    iconColor = Color(0xFF1976D2)
                )
            }
        }
    }
}

@Composable
fun InfoDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = iconColor
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                color = Color(0xFF1E293B),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ChatMessagesView(
    claim: Claim,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (claim.messages.isEmpty()) {
            EmptyChatView()
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Initial message (claim description)
                item {
                    MessageBubble(
                        messageText = claim.description,
                        senderName = "Moi",
                        timestamp = claim.createdAt,
                        isUser = true
                    )
                }
                
                // All subsequent messages
                items(claim.messages) { message ->
                    MessageBubble(
                        messageText = message.message,
                        senderName = message.sender.name,
                        timestamp = message.createdAt,
                        isUser = message.senderRole == "user"
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyChatView() {
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
            text = "Aucun message pour l'instant",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "L'agence vous répondra bientôt",
            fontSize = 14.sp,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MessageBubble(
    messageText: String,
    senderName: String?,
    timestamp: String,
    isUser: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
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
            Spacer(modifier = Modifier.width(10.dp))
        }
        
        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Sender name for agency messages
            if (!isUser && senderName != null) {
                Text(
                    text = senderName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }
            
            Card(
                shape = RoundedCornerShape(
                    topStart = if (isUser) 20.dp else 4.dp,
                    topEnd = if (isUser) 4.dp else 20.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) Color(0xFF1976D2) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = messageText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isUser) Color.White else Color(0xFF1E293B),
                        lineHeight = 22.sp
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = formatRelativeTime(timestamp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isUser) Color.White.copy(alpha = 0.8f) else Color(0xFF94A3B8)
                    )
                }
            }
        }
        
        if (isUser) {
            Spacer(modifier = Modifier.width(10.dp))
            // User avatar
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
        }
    }
}

@Composable
fun MessageInputBar(
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { 
                    Text(
                        "Écrivez votre message...",
                        color = Color(0xFF94A3B8)
                    ) 
                },
                enabled = enabled,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1976D2),
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
                containerColor = if (messageText.isBlank()) Color(0xFFE2E8F0) else Color(0xFF1976D2),
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
                        contentDescription = "Envoyer",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFF1976D2))
    }
}

@Composable
fun ErrorView(error: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFEF4444)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error,
            fontSize = 16.sp,
            color = Color(0xFFEF4444),
            textAlign = TextAlign.Center
        )
    }
}
