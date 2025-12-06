package com.travelmate.ui.screens.groups

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.travelmate.data.models.Group
import com.travelmate.data.models.MessageGroupe
import com.travelmate.data.models.MessageReaction
import com.travelmate.ui.components.EditGroupDialog
import com.travelmate.ui.components.PendingRequestsSheet
import com.travelmate.ui.components.ReactionPicker
import com.travelmate.ui.components.TypingIndicator
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.GroupsViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GroupDetailsScreen(
    groupId: String,
    onBack: () -> Unit,
    viewModel: GroupsViewModel = hiltViewModel()
) {
    val currentGroup by viewModel.currentGroup.collectAsState()
    val messages by viewModel.groupMessages.collectAsState()
    val pendingRequests by viewModel.pendingRequests.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val socketConnected by viewModel.socketConnectionState.collectAsState()
    val typingUsers by viewModel.typingUsers.collectAsState()
    var typingJob: Job? by remember { mutableStateOf(null) }

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("travelmate_prefs", android.content.Context.MODE_PRIVATE)
    val currentUserId = prefs.getString("user_id", "") ?: ""

    // ✅ DEBUG - Afficher toutes les clés
    LaunchedEffect(Unit) {
        android.util.Log.d("DEBUG_TOKEN", "=== SHARED PREFERENCES ===")
        prefs.all.forEach { (key, value) ->
            if (value is String) {
                android.util.Log.d("DEBUG_TOKEN", "  $key: ${value.take(50)}...")
            } else {
                android.util.Log.d("DEBUG_TOKEN", "  $key: $value")
            }
        }
    }

    var messageContent by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var messageToDelete by remember { mutableStateOf<String?>(null) }
    var messageToEdit by remember { mutableStateOf<MessageGroupe?>(null) }
    var showEditMessageDialog by remember { mutableStateOf(false) }
    var showPendingRequestsSheet by remember { mutableStateOf(false) }
    var showGroupInfo by remember { mutableStateOf(false) }
    var showReactionPickerForMessage by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // États pour le dialog des réactions détaillées
    var showReactionDetailsDialog by remember { mutableStateOf(false) }
    var reactionDetailsMessage by remember { mutableStateOf<MessageGroupe?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && !listState.isScrollInProgress) {
            delay(100)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(groupId) {
        viewModel.setUserId(currentUserId)
        viewModel.loadGroupById(groupId)
        viewModel.loadGroupMessages(groupId)
        viewModel.loadPendingRequests(groupId)
    }

    DisposableEffect(groupId) {
        onDispose { viewModel.leaveGroupChat(groupId) }
    }

    val isCreator = currentGroup?.createdBy == currentUserId

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(shadowElevation = 4.dp, color = ColorPrimary) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, "Retour", tint = Color.White)
                            }

                            Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        currentGroup?.name ?: "Groupe",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Box(
                                        modifier = Modifier.size(8.dp)
                                            .background(if (socketConnected) Color.Green else Color.Red, CircleShape)
                                    )
                                }
                                currentGroup?.let {
                                    Text(
                                        "${it.memberCount} membres · ${it.destination ?: "Destination"}",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Row {
                            if (isCreator && pendingRequests.isNotEmpty()) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = ColorError, modifier = Modifier.offset(x = (-4).dp, y = 4.dp)) {
                                            Text(pendingRequests.size.toString(), fontSize = 10.sp, color = Color.White)
                                        }
                                    }
                                ) {
                                    IconButton(onClick = { showPendingRequestsSheet = true }) {
                                        Icon(Icons.Default.PersonAdd, "Demandes en attente", tint = Color.White)
                                    }
                                }
                            }

                            IconButton(onClick = { showGroupInfo = !showGroupInfo }) {
                                Icon(Icons.Default.Info, "Informations", tint = Color.White)
                            }

                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, "Options", tint = Color.White)
                                }
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Icon(Icons.Default.People, null, tint = ColorPrimary)
                                                Text("Voir les membres")
                                            }
                                        },
                                        onClick = { showMenu = false }
                                    )
                                    if (isCreator) {
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Icon(Icons.Default.Edit, null, tint = ColorPrimary)
                                                    Text("Modifier le groupe")
                                                }
                                            },
                                            onClick = {
                                                showMenu = false
                                                showEditDialog = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color.White, trackColor = ColorPrimary.copy(alpha = 0.5f))
                    }
                }
            }
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp, color = Color.White) {
                Column {
                    TypingIndicator(typingUserCount = typingUsers.size)
                    Divider(color = ColorTextSecondary.copy(alpha = 0.1f))

                    // Aperçu image sélectionnée
                    selectedImageUri?.let { uri ->
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp).padding(8.dp)) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { selectedImageUri = null },
                                modifier = Modifier.align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, "Retirer", tint = Color.White)
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.Bottom) {
                        OutlinedTextField(
                            value = messageContent,
                            onValueChange = { newValue ->
                                messageContent = newValue
                                typingJob?.cancel()
                                if (newValue.isNotBlank()) {
                                    viewModel.sendTypingIndicator(groupId, true)
                                    typingJob = coroutineScope.launch {
                                        delay(2000)
                                        viewModel.sendTypingIndicator(groupId, false)
                                    }
                                } else {
                                    viewModel.sendTypingIndicator(groupId, false)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Écrivez votre message...", color = ColorTextSecondary.copy(alpha = 0.6f)) },
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ColorPrimary,
                                unfocusedBorderColor = ColorTextSecondary.copy(alpha = 0.2f),
                                focusedContainerColor = ColorPrimary.copy(alpha = 0.03f)
                            ),
                            maxLines = 4,
                            leadingIcon = {
                                IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                                    Icon(Icons.Default.Image, "Ajouter une image", tint = ColorTextSecondary)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        val isEnabled = messageContent.isNotBlank() || selectedImageUri != null
                        FloatingActionButton(
                            onClick = {
                                if (isEnabled) {
                                    if (selectedImageUri != null) {
                                        viewModel.createMessageWithImage(groupId, messageContent, selectedImageUri!!)
                                    } else {
                                        viewModel.createMessage(groupId, messageContent)
                                    }
                                    messageContent = ""
                                    selectedImageUri = null
                                    typingJob?.cancel()
                                    viewModel.sendTypingIndicator(groupId, false)
                                }
                            },
                            containerColor = if (isEnabled) ColorPrimary else ColorTextSecondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Envoyer", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = showGroupInfo,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    currentGroup?.let { EnhancedGroupHeader(it) }
                }

                if (messages.isEmpty()) {
                    EmptyMessagesState()
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages) { message ->
                            Box {
                                EnhancedMessageCard(
                                    message = message,
                                    currentUserId = currentUserId,
                                    onDelete = { messageToDelete = it },
                                    onEdit = {
                                        messageToEdit = message
                                        showEditMessageDialog = true
                                    },
                                    onReactionClick = { emoji ->
                                        viewModel.toggleReaction(groupId, message.id, emoji)
                                    },
                                    onShowReactionPicker = { showReactionPickerForMessage = message.id },
                                    onShowReactionDetails = {
                                        reactionDetailsMessage = message
                                        showReactionDetailsDialog = true
                                    }
                                )

                                if (showReactionPickerForMessage == message.id) {
                                    Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)) {
                                        ReactionPicker(onEmojiSelected = { emoji ->
                                            viewModel.toggleReaction(groupId, message.id, emoji)
                                            showReactionPickerForMessage = null
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // === DIALOGS & SHEETS ===
    if (showReactionDetailsDialog && reactionDetailsMessage != null) {
        ReactionsDetailsDialog(
            reactions = reactionDetailsMessage!!.reactions,
            onDismiss = {
                showReactionDetailsDialog = false
                reactionDetailsMessage = null
            }
        )
    }

    if (showPendingRequestsSheet) {
        ModalBottomSheet(onDismissRequest = { showPendingRequestsSheet = false }, containerColor = Color.White) {
            PendingRequestsSheet(
                requests = pendingRequests,
                onApprove = { viewModel.approveMember(groupId, it) },
                onReject = { viewModel.rejectMember(groupId, it) }
            )
        }
    }

    if (showEditDialog && currentGroup != null) {
        EditGroupDialog(
            group = currentGroup!!,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, dest, desc, img -> viewModel.updateGroup(groupId, name, dest, desc, img); showEditDialog = false },
            onUploadImage = viewModel::uploadGroupImage
        )
    }

    messageToDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { messageToDelete = null },
            icon = { Icon(Icons.Default.Delete, null, tint = ColorError, modifier = Modifier.size(32.dp)) },
            title = { Text("Supprimer le message") },
            text = { Text("Cette action est irréversible. Voulez-vous vraiment supprimer ce message ?") },
            confirmButton = {
                Button(onClick = { viewModel.deleteMessage(groupId, id); messageToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = ColorError)) {
                    Text("Supprimer")
                }
            },
            dismissButton = { TextButton(onClick = { messageToDelete = null }) { Text("Annuler", color = ColorTextSecondary) } }
        )
    }

    if (showEditMessageDialog && messageToEdit != null) {
        EditMessageDialog(
            message = messageToEdit!!,
            onDismiss = { showEditMessageDialog = false; messageToEdit = null },
            onConfirm = { newContent ->
                viewModel.updateMessage(groupId, messageToEdit!!.id, newContent)
                showEditMessageDialog = false
                messageToEdit = null
            }
        )
    }
}

// ======================== COMPOSANTS ========================

@Composable
fun EnhancedGroupHeader(group: Group) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp)) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                val imageUrl = if (!group.image.isNullOrBlank()) group.image else "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=800"
                AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f)))))
            }
            Column(modifier = Modifier.padding(16.dp)) {
                if (!group.description.isNullOrBlank()) {
                    Text(group.description, fontSize = 14.sp, color = ColorTextSecondary, lineHeight = 20.sp)
                    Spacer(Modifier.height(16.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    if (!group.destination.isNullOrBlank()) InfoChip(Icons.Default.LocationOn, group.destination, ColorPrimary)
                    InfoChip(Icons.Default.People, "${group.memberCount} membres", ColorTextSecondary)
                }
            }
        }
    }
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, tint: Color) {
    Surface(color = tint.copy(alpha = 0.1f), shape = RoundedCornerShape(20.dp)) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
            Text(text, fontSize = 13.sp, color = tint, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun EmptyMessagesState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Surface(modifier = Modifier.size(100.dp), shape = CircleShape, color = ColorPrimary.copy(alpha = 0.1f)) {
                Icon(Icons.Default.ChatBubbleOutline, null, modifier = Modifier.fillMaxSize().padding(24.dp), tint = ColorPrimary)
            }
            Spacer(Modifier.height(24.dp))
            Text("Commencez la conversation", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ColorTextPrimary)
            Spacer(Modifier.height(8.dp))
            Text("Soyez le premier à poster un message dans ce groupe", fontSize = 14.sp, color = ColorTextSecondary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun EnhancedMessageCard(
    message: MessageGroupe,
    currentUserId: String,
    onDelete: (String) -> Unit,
    onEdit: () -> Unit,
    onReactionClick: (String) -> Unit,
    onShowReactionPicker: () -> Unit,
    onShowReactionDetails: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val authorName = message.authorId?.let { "${it.prenom ?: ""} ${it.nom ?: ""}".trim().ifEmpty { "Utilisateur" } } ?: "Utilisateur"
    val authorInitial = message.authorId?.nom?.firstOrNull()?.uppercase()
        ?: message.authorId?.prenom?.firstOrNull()?.uppercase() ?: "U"
    val isMyMessage = message.authorId?.id == currentUserId

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start) {
        Row(modifier = Modifier.fillMaxWidth(if (isMyMessage) 0.85f else 0.85f), horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start) {
            if (!isMyMessage) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(ColorPrimary), contentAlignment = Alignment.Center) {
                    Text(authorInitial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(Modifier.width(8.dp))
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = if (isMyMessage) ColorPrimary else Color.White),
                shape = RoundedCornerShape(
                    topStart = if (isMyMessage) 16.dp else 4.dp,
                    topEnd = if (isMyMessage) 4.dp else 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        if (!isMyMessage) {
                            Text(authorName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = ColorTextPrimary)
                        }
                        if (isMyMessage) {
                            Spacer(Modifier.weight(1f))
                            Box {
                                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.MoreVert, "Options", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { Icon(Icons.Default.Edit, null, tint = ColorPrimary, modifier = Modifier.size(18.dp)); Text("Modifier") } }, onClick = { onEdit(); showMenu = false })
                                    DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { Icon(Icons.Default.Delete, null, tint = ColorError, modifier = Modifier.size(18.dp)); Text("Supprimer", color = ColorError) } }, onClick = { onDelete(message.id); showMenu = false })
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(message.content, fontSize = 14.sp, color = if (isMyMessage) Color.White else ColorTextPrimary, lineHeight = 20.sp)

                    if (message.images.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        message.images.forEach { url ->
                            AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                            Spacer(Modifier.height(4.dp))
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(formatMessageDate(message.createdAt), fontSize = 11.sp, color = if (isMyMessage) Color.White.copy(alpha = 0.8f) else ColorTextSecondary)
                        IconButton(onClick = onShowReactionPicker, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.AddReaction, "Réagir", tint = if (isMyMessage) Color.White else ColorTextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            if (isMyMessage) {
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(ColorPrimary), contentAlignment = Alignment.Center) {
                    Text(authorInitial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        MessageReactionsBar(
            reactions = message.reactions,
            currentUserId = currentUserId,
            onReactionClick = onReactionClick,
            onShowDetails = onShowReactionDetails,
            modifier = Modifier.padding(horizontal = 48.dp)
        )
    }
}

@Composable
fun MessageReactionsBar(
    reactions: List<MessageReaction>,
    currentUserId: String,
    onReactionClick: (String) -> Unit,
    onShowDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (reactions.isEmpty()) return

    val grouped = reactions.groupBy { it.emoji }

    Row(
        modifier = modifier
            .padding(top = 4.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        grouped.forEach { (emoji, list) ->
            // ✅ FIX : Comparer avec userId.id (objet) au lieu de userId (String)
            val hasReacted = list.any { it.userId.id == currentUserId }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (hasReacted) ColorPrimary.copy(alpha = 0.2f) else Color(0xFFF0F0F0),
                modifier = Modifier.combinedClickable(
                    onClick = { onReactionClick(emoji) },
                    onLongClick = onShowDetails
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(emoji, fontSize = 14.sp)
                    Text(
                        list.size.toString(),
                        fontSize = 12.sp,
                        color = if (hasReacted) ColorPrimary else ColorTextSecondary,
                        fontWeight = if (hasReacted) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun ReactionsDetailsDialog(reactions: List<MessageReaction>, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Réactions (${reactions.size})", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Fermer", tint = ColorTextSecondary)
                    }
                }
                Spacer(Modifier.height(16.dp))

                val grouped = reactions.groupBy { it.emoji }
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    grouped.forEach { (emoji, list) ->
                        item {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(emoji, fontSize = 24.sp)
                                    Text(
                                        "${list.size} ${if (list.size > 1) "personnes" else "personne"}",
                                        color = ColorTextSecondary
                                    )
                                }
                                Spacer(Modifier.height(8.dp))

                                // ✅ FIX : reaction.userId est un UserReactionInfo
                                list.forEach { reaction ->
                                    val user = reaction.userId // ✅ C'est déjà un objet UserReactionInfo

                                    val userName = "${user.prenom ?: ""} ${user.nom ?: ""}".trim()
                                        .ifEmpty { "Utilisateur ${user.id.take(6)}" }

                                    val userInitial = user.nom?.firstOrNull()?.uppercase()
                                        ?: user.prenom?.firstOrNull()?.uppercase()
                                        ?: "U"

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(ColorPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                userInitial,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }

                                        Column {
                                            Text(userName, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                            Text(
                                                formatReactionTime(reaction.reactedAt),
                                                fontSize = 12.sp,
                                                color = ColorTextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ✅ Fonction helper pour formater le temps de réaction
fun formatReactionTime(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString) ?: return ""

        val now = Calendar.getInstance()
        val reactionTime = Calendar.getInstance().apply { time = date }

        val diffMillis = now.timeInMillis - reactionTime.timeInMillis
        val diffMinutes = diffMillis / (1000 * 60)
        val diffHours = diffMillis / (1000 * 60 * 60)
        val diffDays = diffMillis / (1000 * 60 * 60 * 24)

        when {
            diffMinutes < 1 -> "À l'instant"
            diffMinutes < 60 -> "Il y a ${diffMinutes}min"
            diffHours < 24 -> "Il y a ${diffHours}h"
            diffDays == 1L -> "Hier"
            diffDays < 7 -> "Il y a ${diffDays}j"
            else -> {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                dateFormat.format(date)
            }
        }
    } catch (e: Exception) {
        ""
    }
}

@Composable
fun EditMessageDialog(message: MessageGroupe, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(message.content) }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Modifier le message", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Fermer", tint = ColorTextSecondary) }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 5
                )
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Annuler") }
                    Button(onClick = { if (text.isNotBlank()) onConfirm(text) }, modifier = Modifier.weight(1f), enabled = text.isNotBlank()) {
                        Text("Enregistrer")
                    }
                }
            }
        }
    }
}

fun formatMessageDate(dateString: String?): String {
    if (dateString == null) return ""
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }
        val date = input.parse(dateString) ?: return ""
        val now = Calendar.getInstance()
        val msgTime = Calendar.getInstance().apply { time = date }

        when {
            now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == msgTime.get(Calendar.DAY_OF_YEAR) ->
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) - msgTime.get(Calendar.DAY_OF_YEAR) == 1 ->
                "Hier ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)}"
            else -> SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) { "" }
}