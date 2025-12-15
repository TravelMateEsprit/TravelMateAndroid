package com.travelmate.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.travelmate.data.models.ChatMessage
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Beautiful Chat Screen - Messenger/WhatsApp Style
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserChatScreen(
    agencyId: String,
    packId: String? = null,
    packName: String? = null,
    agencyName: String = "Agence",
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(agencyId, packId, agencyName) {
        if (agencyId.isNotEmpty()) {
            viewModel.loadConversation(agencyId, packId, agencyName)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            kotlinx.coroutines.delay(100)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF0084FF),
                                            Color(0xFF0066CC)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = agencyName.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Column {
                            Text(
                                packName ?: agencyName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                packName?.let { agencyName } ?: "En ligne",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Retour", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Voice call */ }) {
                        Icon(Icons.Default.Phone, "Appeler", tint = Color.White)
                    }
                    IconButton(onClick = { /* Video call */ }) {
                        Icon(Icons.Default.Videocam, "Vidéo", tint = Color.White)
                    }
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Default.MoreVert, "Plus", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0084FF), // Messenger blue
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF0F2F5)) // Messenger chat background
        ) {
            // Messages list with pattern background
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFF0F2F5)) // Messenger background
            ) {
                when {
                    error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    "Erreur: $error",
                                    color = Color.Red,
                                    fontSize = 16.sp
                                )
                                Button(
                                    onClick = {
                                        viewModel.clearError()
                                        if (agencyId.isNotEmpty()) {
                                            viewModel.loadConversation(agencyId, packId, agencyName)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF0084FF)
                                    )
                                ) {
                                    Text("Réessayer")
                                }
                            }
                        }
                    }
                    isLoading && messages.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF0084FF))
                        }
                    }
                    messages.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Commencez une conversation !",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(messages, key = { it.id }) { message ->
                                MessageBubble(
                                    message = message,
                                    isSentByMe = viewModel.isMessageFromMe(message)
                                )
                            }
                        }
                    }
                }
            }

            // Message input bar - WhatsApp style
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp,
                color = Color(0xFFF0F2F5)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Emoji/Attachment button
                    IconButton(
                        onClick = { /* Show emoji picker or attachments */ },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.TagFaces,
                            contentDescription = "Emoji",
                            tint = Color(0xFF54656F),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Message input field
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp, max = 120.dp),
                        placeholder = { 
                            Text(
                                "Message",
                                color = Color.Gray.copy(alpha = 0.6f),
                                fontSize = 15.sp
                            ) 
                        },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color(0xFF0084FF)
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 15.sp,
                            color = Color.Black
                        )
                    )

                    // Send button
                    if (messageText.isNotEmpty()) {
                        FloatingActionButton(
                            onClick = {
                                viewModel.sendMessage(agencyId, messageText, packId)
                                messageText = ""
                            },
                            modifier = Modifier.size(40.dp),
                            containerColor = Color(0xFF0084FF),
                            contentColor = Color.White
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Envoyer",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        // Microphone button when no text
                        IconButton(
                            onClick = { /* Voice message */ },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Message vocal",
                                tint = Color(0xFF54656F),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isSentByMe: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = if (isSentByMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(horizontal = 4.dp),
            horizontalAlignment = if (isSentByMe) Alignment.End else Alignment.Start
        ) {
            // Message bubble
            Surface(
                color = if (isSentByMe) {
                    Color(0xFF0084FF) // Messenger sent message color (blue)
                } else {
                    Color.White // Received message color
                },
                shape = RoundedCornerShape(
                    topStart = if (isSentByMe) 8.dp else 0.dp,
                    topEnd = if (isSentByMe) 0.dp else 8.dp,
                    bottomStart = 8.dp,
                    bottomEnd = 8.dp
                ),
                shadowElevation = if (isSentByMe) 1.dp else 0.5.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = message.message,
                        fontSize = 15.sp,
                        color = if (isSentByMe) Color.White else Color.Black,
                        lineHeight = 20.sp
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatMessageTime(message.timestamp),
                            fontSize = 11.sp,
                            color = Color.Gray.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        
                        // Read receipt (double check for sent messages)
                        if (isSentByMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                if (message.read) Icons.Default.DoneAll else Icons.Default.DoneAll,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (message.read) Color(0xFF34B7F1) else Color.Gray.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatMessageTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    val messageHour = calendar.get(Calendar.HOUR_OF_DAY)
    val messageMinute = calendar.get(Calendar.MINUTE)

    return when {
        diff < 60000 -> "À l'instant"
        diff < 3600000 -> "${diff / 60000} min"
        diff < 86400000 -> String.format("%02d:%02d", messageHour, messageMinute)
        else -> {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
