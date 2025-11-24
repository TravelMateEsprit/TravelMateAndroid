package com.travelmate.ui.screens.groups

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.travelmate.data.models.MessageGroupe
import com.travelmate.ui.components.EditGroupDialog
import com.travelmate.ui.components.PendingRequestsSheet
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.GroupsViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
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

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("travelmate_prefs", android.content.Context.MODE_PRIVATE)
    val currentUserId = prefs.getString("user_id", "") ?: ""

    var messageContent by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var messageToDelete by remember { mutableStateOf<String?>(null) }
    var messageToEdit by remember { mutableStateOf<MessageGroupe?>(null) }
    var showEditMessageDialog by remember { mutableStateOf(false) }
    var showPendingRequestsSheet by remember { mutableStateOf(false) }
    var showGroupInfo by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    // Auto-scroll aux nouveaux messages
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
        viewModel.loadGroupById(groupId)
        viewModel.loadGroupMessages(groupId)
        viewModel.loadPendingRequests(groupId)
    }

    val isCreator = currentGroup?.createdBy == currentUserId

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // TopBar améliorée avec plus d'infos
            Surface(
                shadowElevation = 4.dp,
                color = ColorPrimary
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    "Retour",
                                    tint = Color.White
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 4.dp)
                            ) {
                                Text(
                                    currentGroup?.name ?: "Groupe",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )

                                // Affichage du nombre de membres
                                currentGroup?.let { group ->
                                    Text(
                                        "${group.memberCount} membres · ${group.destination ?: "Destination"}",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Row {
                            // Badge de demandes en attente
                            if (isCreator && pendingRequests.isNotEmpty()) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = ColorError,
                                            modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                                        ) {
                                            Text(
                                                pendingRequests.size.toString(),
                                                fontSize = 10.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                ) {
                                    IconButton(onClick = { showPendingRequestsSheet = true }) {
                                        Icon(
                                            Icons.Default.PersonAdd,
                                            "Demandes en attente",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }

                            // Info du groupe
                            IconButton(onClick = { showGroupInfo = !showGroupInfo }) {
                                Icon(
                                    Icons.Default.Info,
                                    "Informations",
                                    tint = Color.White
                                )
                            }

                            // Menu actions
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        "Options",
                                        tint = Color.White
                                    )
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.People,
                                                    null,
                                                    tint = ColorPrimary
                                                )
                                                Text("Voir les membres")
                                            }
                                        },
                                        onClick = {
                                            showMenu = false
                                            // TODO: Ouvrir la liste des membres
                                        }
                                    )

                                    if (isCreator) {
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Edit,
                                                        null,
                                                        tint = ColorPrimary
                                                    )
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

                    // Barre de progression lors du chargement
                    if (isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White,
                            trackColor = ColorPrimary
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Zone de saisie améliorée
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Column {
                    Divider(color = ColorTextSecondary.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        OutlinedTextField(
                            value = messageContent,
                            onValueChange = { messageContent = it },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text(
                                    "Écrivez votre message...",
                                    color = ColorTextSecondary.copy(alpha = 0.6f)
                                )
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ColorPrimary,
                                unfocusedBorderColor = ColorTextSecondary.copy(alpha = 0.2f),
                                focusedContainerColor = ColorPrimary.copy(alpha = 0.03f)
                            ),
                            maxLines = 4,
                            leadingIcon = {
                                IconButton(onClick = { /* TODO: Ajouter pièce jointe */ }) {
                                    Icon(
                                        Icons.Default.AttachFile,
                                        "Joindre un fichier",
                                        tint = ColorTextSecondary
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Bouton d'envoi animé
                        val isEnabled = messageContent.isNotBlank()
                        FloatingActionButton(
                            onClick = {
                                if (isEnabled) {
                                    viewModel.createMessage(groupId, messageContent)
                                    messageContent = ""
                                }
                            },
                            containerColor = if (isEnabled) ColorPrimary else ColorTextSecondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Envoyer",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
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
                // Info du groupe (dépliable)
                AnimatedVisibility(
                    visible = showGroupInfo,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    currentGroup?.let { group ->
                        EnhancedGroupHeader(group)
                    }
                }

                // Zone de messages
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
                            EnhancedMessageCard(
                                message = message,
                                currentUserId = currentUserId,
                                onDelete = { messageToDelete = it },
                                onEdit = {
                                    messageToEdit = message
                                    showEditMessageDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Bottom Sheets et Dialogs
    if (showPendingRequestsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPendingRequestsSheet = false },
            containerColor = Color.White
        ) {
            PendingRequestsSheet(
                requests = pendingRequests,
                onApprove = { userId ->
                    viewModel.approveMember(groupId, userId)
                },
                onReject = { userId ->
                    viewModel.rejectMember(groupId, userId)
                }
            )
        }
    }

    if (showEditDialog && currentGroup != null) {
        EditGroupDialog(
            group = currentGroup!!,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, destination, description, imageUrl ->
                viewModel.updateGroup(groupId, name, destination, description, imageUrl)
                showEditDialog = false
            },
            onUploadImage = { uri, onSuccess, onError ->
                viewModel.uploadGroupImage(uri, onSuccess, onError)
            }
        )
    }

    messageToDelete?.let { messageId ->
        AlertDialog(
            onDismissRequest = { messageToDelete = null },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    null,
                    tint = ColorError,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Supprimer le message") },
            text = { Text("Cette action est irréversible. Voulez-vous vraiment supprimer ce message?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMessage(groupId, messageId)
                        messageToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorError)
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToDelete = null }) {
                    Text("Annuler", color = ColorTextSecondary)
                }
            }
        )
    }

    if (showEditMessageDialog && messageToEdit != null) {
        EditMessageDialog(
            message = messageToEdit!!,
            onDismiss = {
                showEditMessageDialog = false
                messageToEdit = null
            },
            onConfirm = { newContent ->
                viewModel.updateMessage(groupId, messageToEdit!!.id, newContent)
                showEditMessageDialog = false
                messageToEdit = null
            }
        )
    }
}

// En-tête amélioré du groupe
@Composable
fun EnhancedGroupHeader(group: com.travelmate.data.models.Group) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Image avec gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val imageUrl = if (!group.image.isNullOrBlank()) {
                    group.image
                } else {
                    "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=800"
                }

                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient overlay pour améliorer la lisibilité
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                )
                            )
                        )
                )
            }

            // Contenu
            Column(modifier = Modifier.padding(16.dp)) {
                // Description
                if (!group.description.isNullOrBlank()) {
                    Text(
                        group.description,
                        fontSize = 14.sp,
                        color = ColorTextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Infos du groupe
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    if (!group.destination.isNullOrBlank()) {
                        InfoChip(
                            icon = Icons.Default.LocationOn,
                            text = group.destination,
                            tint = ColorPrimary
                        )
                    }

                    InfoChip(
                        icon = Icons.Default.People,
                        text = "${group.memberCount} membres",
                        tint = ColorTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: Color
) {
    Surface(
        color = tint.copy(alpha = 0.1f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text,
                fontSize = 13.sp,
                color = tint,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// État vide amélioré
@Composable
fun EmptyMessagesState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = ColorPrimary.copy(alpha = 0.1f)
            ) {
                Icon(
                    Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    tint = ColorPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Commencez la conversation",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Soyez le premier à poster un message dans ce groupe",
                fontSize = 14.sp,
                color = ColorTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// Carte de message améliorée
@Composable
fun EnhancedMessageCard(
    message: MessageGroupe,
    currentUserId: String,
    onDelete: (String) -> Unit,
    onEdit: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val authorName = message.authorId?.let { author ->
        "${author.prenom ?: ""} ${author.nom ?: ""}".trim().ifEmpty { "Utilisateur" }
    } ?: "Utilisateur"

    val authorInitial = message.authorId?.nom?.firstOrNull()?.uppercase()
        ?: message.authorId?.prenom?.firstOrNull()?.uppercase()
        ?: "U"

    val isMyMessage = message.authorId?.id == currentUserId

    // Alignement différent selon l'auteur
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(if (isMyMessage) 0.85f else 0.85f),
            horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
        ) {
            if (!isMyMessage) {
                // Avatar à gauche pour les autres
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ColorPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        authorInitial,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMyMessage) ColorPrimary else Color.White
                ),
                shape = RoundedCornerShape(
                    topStart = if (isMyMessage) 16.dp else 4.dp,
                    topEnd = if (isMyMessage) 4.dp else 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isMyMessage) {
                            Text(
                                authorName,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = ColorTextPrimary
                            )
                        }

                        if (isMyMessage) {
                            Spacer(modifier = Modifier.weight(1f))
                            Box {
                                IconButton(
                                    onClick = { showMenu = true },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "Options",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    null,
                                                    tint = ColorPrimary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text("Modifier")
                                            }
                                        },
                                        onClick = {
                                            onEdit()
                                            showMenu = false
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    null,
                                                    tint = ColorError,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text("Supprimer", color = ColorError)
                                            }
                                        },
                                        onClick = {
                                            onDelete(message.id)
                                            showMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        message.content,
                        fontSize = 14.sp,
                        color = if (isMyMessage) Color.White else ColorTextPrimary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        formatMessageDate(message.createdAt),
                        fontSize = 11.sp,
                        color = if (isMyMessage) Color.White.copy(alpha = 0.8f) else ColorTextSecondary
                    )
                }
            }

            if (isMyMessage) {
                Spacer(modifier = Modifier.width(8.dp))

                // Avatar à droite pour mes messages
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ColorPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        authorInitial,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// Dialog d'édition de message (inchangé mais avec style amélioré)
@Composable
fun EditMessageDialog(
    message: MessageGroupe,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var editedContent by remember { mutableStateOf(message.content) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Modifier le message",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Fermer", tint = ColorTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = editedContent,
                    onValueChange = { editedContent = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    placeholder = { Text("Votre message...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorPrimary,
                        unfocusedBorderColor = ColorTextSecondary.copy(alpha = 0.3f)
                    ),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Annuler")
                    }

                    Button(
                        onClick = {
                            if (editedContent.isNotBlank()) {
                                onConfirm(editedContent)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = editedContent.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Enregistrer", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

fun formatMessageDate(dateString: String?): String {
    if (dateString == null) return ""

    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString) ?: return ""

        val now = Calendar.getInstance()
        val messageTime = Calendar.getInstance().apply { time = date }

        when {
            now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR) -> {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                timeFormat.format(date)
            }
            now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) - messageTime.get(Calendar.DAY_OF_YEAR) == 1 -> {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                "Hier ${timeFormat.format(date)}"
            }
            else -> {
                val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                dateFormat.format(date)
            }
        }
    } catch (e: Exception) {
        ""
    }
}