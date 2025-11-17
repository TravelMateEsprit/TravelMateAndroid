package com.travelmate.ui.screens.groups

import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    val pendingRequests by viewModel.pendingRequests.collectAsState() // ✅ AJOUT
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
    var showPendingRequestsSheet by remember { mutableStateOf(false) } // ✅ AJOUT

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(groupId) {
        viewModel.loadGroupById(groupId)
        viewModel.loadGroupMessages(groupId)
        viewModel.loadPendingRequests(groupId) // ✅ AJOUT : Charger les demandes
    }

    val isCreator = currentGroup?.createdBy == currentUserId

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        currentGroup?.name ?: "Groupe",
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                },
                actions = {
                    // ✅ AJOUT : Bouton pour voir les demandes en attente (seulement si créateur)
                    if (isCreator && pendingRequests.isNotEmpty()) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = ColorError) {
                                    Text(
                                        pendingRequests.size.toString(),
                                        fontSize = 10.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        ) {
                            IconButton(onClick = { showPendingRequestsSheet = true }) {
                                Icon(Icons.Default.PersonAdd, "Demandes en attente")
                            }
                        }
                    }

                    IconButton(onClick = { /* TODO: Members */ }) {
                        Icon(Icons.Default.People, "Membres")
                    }

                    if (isCreator) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, "Modifier")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageContent,
                        onValueChange = { messageContent = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Écrivez un message...") },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorPrimary,
                            unfocusedBorderColor = ColorTextSecondary.copy(alpha = 0.3f)
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FloatingActionButton(
                        onClick = {
                            if (messageContent.isNotBlank()) {
                                viewModel.createMessage(groupId, messageContent)
                                messageContent = ""
                            }
                        },
                        containerColor = ColorPrimary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Envoyer",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && currentGroup == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = ColorPrimary
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    currentGroup?.let { group ->
                        GroupHeader(group)
                        Divider()
                    }

                    if (messages.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = ColorTextSecondary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Aucun message pour le moment",
                                    color = ColorTextSecondary,
                                    fontSize = 16.sp
                                )
                                Text(
                                    "Soyez le premier à poster!",
                                    color = ColorTextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            reverseLayout = false,
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(messages) { message ->
                                MessageCard(
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
    }

    // ✅ AJOUT : Bottom Sheet pour les demandes en attente
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
            title = { Text("Supprimer le message") },
            text = { Text("Êtes-vous sûr de vouloir supprimer ce message?") },
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
                    Text("Annuler")
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

// Reste du code identique (EditMessageDialog, GroupHeader, MessageCard, formatMessageDate)
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
            colors = CardDefaults.cardColors(containerColor = Color.White)
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

@Composable
fun GroupHeader(group: com.travelmate.data.models.Group) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        val imageUrl = if (!group.image.isNullOrBlank()) {
            group.image
        } else {
            "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=800"
        }

        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            group.description,
            fontSize = 14.sp,
            color = ColorTextSecondary,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (!group.destination.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = ColorPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        group.destination,
                        fontSize = 14.sp,
                        color = ColorTextSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.People,
                    contentDescription = null,
                    tint = ColorTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${group.memberCount} membres",
                    fontSize = 14.sp,
                    color = ColorTextSecondary
                )
            }
        }
    }
}

@Composable
fun MessageCard(
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            authorName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = ColorTextPrimary
                        )
                        Text(
                            formatMessageDate(message.createdAt),
                            fontSize = 12.sp,
                            color = ColorTextSecondary
                        )
                    }
                }

                if (isMyMessage) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = ColorTextSecondary
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
                                            contentDescription = null,
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
                                            contentDescription = null,
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                message.content,
                fontSize = 14.sp,
                color = ColorTextPrimary,
                lineHeight = 20.sp
            )
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
                "Aujourd'hui à ${timeFormat.format(date)}"
            }
            now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) - messageTime.get(Calendar.DAY_OF_YEAR) == 1 -> {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                "Hier à ${timeFormat.format(date)}"
            }
            else -> {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                dateFormat.format(date)
            }
        }
    } catch (e: Exception) {
        ""
    }
}