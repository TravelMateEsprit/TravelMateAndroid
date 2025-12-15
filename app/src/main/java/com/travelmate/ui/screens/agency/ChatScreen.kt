package com.travelmate.ui.screens.agency

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.travelmate.data.models.ChatConversation
import com.travelmate.ui.components.ModernCard
import com.travelmate.ui.theme.ColorPrimary
import com.travelmate.ui.theme.ColorSecondary
import com.travelmate.ui.theme.ColorSuccess
import com.travelmate.ui.theme.ColorTextPrimary
import com.travelmate.ui.theme.ColorTextSecondary
import com.travelmate.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Messenger-style conversations hub for agencies.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val conversations by viewModel.conversations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadConversations()
    }

    val filteredConversations = remember(conversations, searchQuery) {
        conversations.filter {
            it.userName.contains(searchQuery, ignoreCase = true) ||
                (it.agencyName?.contains(searchQuery, ignoreCase = true) == true) ||
                (it.lastMessage?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Messagerie", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${conversations.count { it.unreadCount > 0 }} conversations non lues",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Filtrer", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorPrimary, titleContentColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* start new chat */ }, containerColor = ColorPrimary) {
                Icon(Icons.Default.Add, contentDescription = "Nouvelle conversation", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF4F6FA))
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Rechercher une conversation") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Effacer")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = ColorPrimary,
                    unfocusedIndicatorColor = ColorPrimary.copy(alpha = 0.4f),
                    cursorColor = ColorPrimary
                )
            )

            when {
                isLoading -> LoadingState()
                error != null -> ErrorState(error) {
                    viewModel.clearError()
                    viewModel.loadConversations()
                }
                filteredConversations.isEmpty() -> EmptyConversationsState()
                else -> ConversationsList(filteredConversations)
            }
        }
    }

    if (showFilters) {
        ConversationsFilterDialog(onDismiss = { showFilters = false })
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = ColorPrimary)
    }
}

@Composable
private fun ErrorState(message: String?, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Default.Chat, contentDescription = null, tint = ColorPrimary, modifier = Modifier.size(48.dp))
            Text(message ?: "Erreur inconnue", color = ColorTextPrimary)
            TextButton(onClick = onRetry) {
                Text("Réessayer")
            }
        }
    }
}

@Composable
private fun EmptyConversationsState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Default.Chat, contentDescription = null, tint = ColorPrimary, modifier = Modifier.size(64.dp))
            Text("Aucune conversation", fontWeight = FontWeight.Bold)
            Text("Vos échanges avec les clients apparaîtront ici", color = ColorTextSecondary)
        }
    }
}

@Composable
private fun ConversationsList(conversations: List<ChatConversation>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(conversations, key = { it.id }) { conversation ->
            ConversationCard(conversation)
        }
    }
}

@Composable
private fun ConversationCard(conversation: ChatConversation) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { /* navigate to chat detail */ },
        tonalElevation = 2.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(ColorPrimary, ColorSecondary))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.userName.take(2).uppercase(Locale.getDefault()),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(conversation.userName, fontWeight = FontWeight.Bold, color = ColorTextPrimary)
                    Text(
                        formatConversationTime(conversation.lastMessageTime),
                        color = ColorTextSecondary,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    conversation.lastMessage ?: "",
                    color = ColorTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (conversation.unreadCount > 0) {
                BadgedBox(badge = { Badge { Text(conversation.unreadCount.toString()) } }) {
                    Box(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ConversationsFilterDialog(onDismiss: () -> Unit) {
    var selectedFilter by remember { mutableStateOf("Tous les messages") }
    val filters = listOf("Tous les messages", "Non lus", "Réservations", "Clients premium")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtres") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filters.forEach { filter ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedFilter = filter
                            },
                        color = if (selectedFilter == filter) ColorPrimary.copy(alpha = 0.1f) else Color.Transparent
                    ) {
                        Text(
                            filter,
                            modifier = Modifier.padding(12.dp),
                            color = if (selectedFilter == filter) ColorPrimary else ColorTextPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Appliquer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

private fun formatConversationTime(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(date)
}
