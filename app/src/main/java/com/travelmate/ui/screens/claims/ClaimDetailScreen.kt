package com.travelmate.ui.screens.claims

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation. lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation. lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material. icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui. graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.travelmate.data.models.*
import com.travelmate. ui.components.*
import com.travelmate. viewmodel.ClaimViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimDetailScreen(
    navController: NavController,
    claimId: String,
    viewModel: ClaimViewModel = hiltViewModel()
) {
    val claim by viewModel.selectedClaim.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val addMessageSuccess by viewModel.addMessageSuccess. collectAsState()
    
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(claimId) {
        viewModel.loadClaimById(claimId)
    }

    LaunchedEffect(addMessageSuccess) {
        if (addMessageSuccess) {
            messageText = ""
            viewModel. resetAddMessageSuccess()
            viewModel.loadClaimById(claimId)
            coroutineScope.launch {
                claim?.messages?.size?.let { size ->
                    if (size > 0) {
                        listState.animateScrollToItem(size - 1)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(claim?.ticketNumber ?: "Chargement...")
                        claim?.let {
                            Text(
                                it.subject,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController. navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme. colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            if (claim != null && claim!! .status != ClaimStatus.CLOSED. value) {
                MessageInputBar(
                    messageText = messageText,
                    onMessageTextChange = { messageText = it },
                    onSendClick = {
                        if (messageText.isNotBlank()) {
                            viewModel. addMessage(claimId, messageText)
                        }
                    },
                    isLoading = isLoading
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading && claim == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier. align(Alignment.Center)
                    )
                }
                claim != null -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            ClaimHeader(claim!!)
                        }

                        item {
                            InitialMessageCard(
                                description = claim!! .description,
                                attachments = claim!!.initialAttachments,
                                createdAt = claim!!.createdAt
                            )
                        }

                        if (claim!!.messages.isNotEmpty()) {
                            item {
                                Divider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                Text(
                                    "Conversation",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            items(claim!!.messages) { message ->
                                MessageBubble(message)
                            }
                        }

                        if (claim!!.statusHistory.size > 1) {
                            item {
                                Divider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                Text(
                                    "Historique",
                                    style = MaterialTheme.typography. titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            items(claim!!.statusHistory) { history ->
                                StatusHistoryItem(history)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClaimHeader(claim: Claim) {
    Card(
        modifier = Modifier. fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme. colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier. padding(16.dp),
            verticalArrangement = Arrangement. spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier. fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusChip(status = claim.status)
                PriorityChip(priority = claim.priority)
                CategoryChip(category = claim.category)
            }

            Row(
                modifier = Modifier. fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Créé le",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme. onSecondaryContainer. copy(alpha = 0.7f)
                    )
                    Text(
                        formatDate(claim.createdAt),
                        style = MaterialTheme. typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                claim.firstResponseAt?.let {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Première réponse",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme. onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            formatDate(it),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons. Default.Business,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    claim.agencyId. name,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun InitialMessageCard(
    description: String,
    attachments: List<String>,
    createdAt: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement. SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Message initial",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = formatDate(createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium
            )

            if (attachments.isNotEmpty()) {
                Divider()
                Text(
                    "${attachments.size} pièce${if(attachments.size > 1) "s" else ""} jointe${if(attachments.size > 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme. primary
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: ClaimMessage) {
    val isFromUser = message.senderRole == "user"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4. dp),
        horizontalArrangement = if (isFromUser) Arrangement. End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (! isFromUser) {
            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                tonalElevation = 2.dp
            ) {
                Icon(
                    Icons.Default.SupportAgent,
                    contentDescription = null,
                    modifier = Modifier.padding(6.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isFromUser) Alignment. End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (! isFromUser) {
                Text(
                    text = message.sender. name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme. onSurfaceVariant,
                    modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (isFromUser) 20.dp else 4.dp,
                    bottomEnd = if (isFromUser) 4.dp else 20.dp
                ),
                color = if (isFromUser) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isFromUser) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (message.attachments.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = if (isFromUser) 
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) 
                            else 
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.AttachFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isFromUser) 
                                        MaterialTheme.colorScheme.onPrimary 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${message.attachments.size} fichier${if(message.attachments.size > 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isFromUser) 
                                        MaterialTheme.colorScheme.onPrimary 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4. dp))
                    
                    Text(
                        text = formatDate(message.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isFromUser) 
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier. align(Alignment.End)
                    )
                }
            }
        }

        if (isFromUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 2.dp
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(6.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun StatusHistoryItem(history: StatusHistoryItem) {
    Row(
        modifier = Modifier. fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Default.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement. spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(status = history.status)
                Text(
                    formatDate(history.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme. onSurfaceVariant
                )
            }
            if (history.comment != null) {
                Spacer(modifier = Modifier. height(4.dp))
                Text(
                    history.comment,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "Par ${history.changedBy.name}",
                style = MaterialTheme.typography. labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant. copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun MessageInputBar(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                . fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Votre message... ") },
                maxLines = 4,
                shape = RoundedCornerShape(24.dp)
            )

            FilledIconButton(
                onClick = onSendClick,
                enabled = messageText.isNotBlank() && !isLoading,
                modifier = Modifier.size(48.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Send, "Envoyer")
                }
            }
        }
    }
}