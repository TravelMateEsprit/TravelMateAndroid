package com.travelmate.ui.screens.notifications

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.travelmate.data.models.Notification
import com.travelmate.data.models.NotificationType
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateToGroup: (String) -> Unit = {},
    onNavigateToUserProfile: (String) -> Unit = {},
    onBack: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val userInfo by viewModel.userInfo.collectAsState()
    val isLoadingUserInfo by viewModel.isLoadingUserInfo.collectAsState()

    var showMarkAllReadDialog by remember { mutableStateOf(false) }
    var showDeleteAllReadDialog by remember { mutableStateOf(false) }
    var notificationToHandle by remember { mutableStateOf<Notification?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                actions = {
                    // Bouton pour supprimer toutes les notifications lues
                    if (notifications.any { it.read }) {
                        IconButton(onClick = { showDeleteAllReadDialog = true }) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                "Supprimer les notifications lues",
                                tint = Color.White
                            )
                        }
                    }
                    // Bouton pour marquer toutes comme lues
                    if (notifications.any { !it.read }) {
                        IconButton(onClick = { showMarkAllReadDialog = true }) {
                            Icon(
                                Icons.Default.DoneAll,
                                "Tout marquer comme lu",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Erreur",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorError
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error ?: "Une erreur est survenue",
                            color = ColorTextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Réessayer")
                        }
                    }
                }
                notifications.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.NotificationsNone,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = ColorTextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Aucune notification",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Vous n'avez pas encore de notifications",
                            color = ColorTextSecondary
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications) { notification ->
                            NotificationCard(
                                notification = notification,
                                onMarkAsRead = { viewModel.markAsRead(notification.id) },
                                onNavigateToGroup = {
                                    if (notification.groupId != null) {
                                        onNavigateToGroup(notification.groupId)
                                    }
                                },
                                onNavigateToUserProfile = {
                                    if (notification.relatedUserId != null) {
                                        onNavigateToUserProfile(notification.relatedUserId)
                                    }
                                },
                                onHandleGroupRequest = { notif ->
                                    if (notif.type == NotificationType.GROUP_REQUEST && 
                                        notif.groupId != null && 
                                        notif.relatedUserId != null) {
                                        // Charger les informations de l'utilisateur
                                        viewModel.loadUserInfo(notif.relatedUserId!!)
                                        notificationToHandle = notif
                                    } else if (notif.groupId != null) {
                                        onNavigateToGroup(notif.groupId)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Mark all as read confirmation dialog
    if (showMarkAllReadDialog) {
        AlertDialog(
            onDismissRequest = { showMarkAllReadDialog = false },
            title = { Text("Tout marquer comme lu") },
            text = { Text("Voulez-vous marquer toutes les notifications comme lues ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.markAllAsRead()
                        showMarkAllReadDialog = false
                    }
                ) {
                    Text("Oui", color = ColorPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMarkAllReadDialog = false }) {
                    Text("Non")
                }
            }
        )
    }

    // Delete all read notifications confirmation dialog
    if (showDeleteAllReadDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllReadDialog = false },
            title = { Text("Supprimer les notifications lues") },
            text = { 
                Text("Voulez-vous supprimer toutes les notifications lues ? Cette action est irréversible.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllReadNotifications()
                        showDeleteAllReadDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ColorError)
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllReadDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Group request approval/rejection dialog
    notificationToHandle?.let { notification ->
        if (notification.type == NotificationType.GROUP_REQUEST && 
            notification.groupId != null && 
            notification.relatedUserId != null) {
            AlertDialog(
                onDismissRequest = { 
                    notificationToHandle = null
                    viewModel.clearUserInfo()
                },
                title = { Text("Demande de groupe") },
                text = { 
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Afficher les informations de l'utilisateur
                        if (isLoadingUserInfo) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Chargement des informations...", fontSize = 12.sp, color = ColorTextSecondary)
                        } else if (userInfo != null) {
                            // Avatar ou initiales
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(ColorPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userInfo!!.name?.take(2)?.uppercase() 
                                        ?: userInfo!!.firstName?.take(1)?.uppercase() 
                                        ?: userInfo!!.email?.substringBefore("@")?.take(2)?.uppercase() 
                                        ?: "U",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Nom de l'utilisateur
                            Text(
                                text = userInfo!!.name 
                                    ?: userInfo!!.email?.substringBefore("@") 
                                    ?: "Utilisateur",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorTextPrimary
                            )
                            
                            // Email
                            if (!userInfo!!.email.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = userInfo!!.email!!,
                                    fontSize = 14.sp,
                                    color = ColorTextSecondary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Message de la notification
                        Text(
                            text = notification.message,
                            fontSize = 14.sp,
                            color = ColorTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Voulez-vous accepter ou rejeter cette demande ?",
                            fontSize = 14.sp,
                            color = ColorTextSecondary
                        )
                    }
                },
                confirmButton = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.rejectGroupRequest(
                                    notification.groupId!!, 
                                    notification.relatedUserId!!,
                                    notification.id
                                )
                                viewModel.clearUserInfo()
                                notificationToHandle = null
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = ColorError)
                        ) {
                            Text("Rejeter")
                        }
                        Button(
                            onClick = {
                                viewModel.approveGroupRequest(
                                    notification.groupId!!, 
                                    notification.relatedUserId!!,
                                    notification.id
                                )
                                viewModel.clearUserInfo()
                                notificationToHandle = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorSuccess)
                        ) {
                            Text("Accepter", color = Color.White)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        notificationToHandle = null
                        viewModel.clearUserInfo()
                    }) {
                        Text("Annuler")
                    }
                }
            )
        }
    }
}

@Composable
fun NotificationCard(
    notification: Notification,
    onMarkAsRead: () -> Unit,
    onNavigateToGroup: () -> Unit,
    onNavigateToUserProfile: () -> Unit,
    onHandleGroupRequest: (Notification) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val icon = when (notification.type) {
        NotificationType.GROUP_REQUEST -> Icons.Default.PersonAdd
        NotificationType.GROUP_APPROVED -> Icons.Default.CheckCircle
        NotificationType.GROUP_REJECTED -> Icons.Default.Cancel
        NotificationType.MEMBER_REMOVED -> Icons.Default.PersonRemove
        NotificationType.MEMBER_BANNED -> Icons.Default.Block
    }

    val iconColor = when (notification.type) {
        NotificationType.GROUP_REQUEST -> ColorPrimary
        NotificationType.GROUP_APPROVED -> ColorSuccess
        NotificationType.GROUP_REJECTED -> ColorError
        NotificationType.MEMBER_REMOVED -> ColorTextSecondary
        NotificationType.MEMBER_BANNED -> ColorError
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read) Color.White else ColorPrimary.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.read) 1.dp else 2.dp),
        onClick = {
            if (!notification.read) {
                onMarkAsRead()
            }
            // Si c'est une demande de groupe, afficher le dialogue d'acceptation/rejet
            if (notification.type == NotificationType.GROUP_REQUEST && 
                notification.groupId != null && 
                notification.relatedUserId != null) {
                onHandleGroupRequest(notification)
            } else if (notification.groupId != null) {
                onNavigateToGroup()
            } else if (notification.relatedUserId != null) {
                onNavigateToUserProfile()
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (!notification.read) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ColorPrimary)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = notification.title,
                    fontSize = 16.sp,
                    fontWeight = if (notification.read) FontWeight.Normal else FontWeight.Bold,
                    color = ColorTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = ColorTextSecondary,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatNotificationDate(notification.createdAt),
                    fontSize = 12.sp,
                    color = ColorTextSecondary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

fun formatNotificationDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString) ?: return "récemment"

        val now = Calendar.getInstance().timeInMillis
        val then = date.time
        val diff = now - then

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        when {
            minutes < 1 -> "À l'instant"
            minutes < 60 -> "Il y a $minutes min"
            hours < 24 -> "Il y a $hours h"
            days < 7 -> "Il y a $days j"
            else -> {
                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                outputFormat.format(date)
            }
        }
    } catch (e: Exception) {
        "récemment"
    }
}

