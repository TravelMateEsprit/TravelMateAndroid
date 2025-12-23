package com.travelmate.ui.notifications

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.travelmate.data.model.NotificationModel
import com.travelmate.data.model.NotificationType
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.NotificationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = hiltViewModel(),
    onNavigateToRequestDetails: (String) -> Unit = {},
    onNavigateToPaymentDetails: (String) -> Unit = {}
) {
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()

    var showMarkAllDialog by remember { mutableStateOf(false) }
    
    // Animation pour le badge
    val badgeScale by animateFloatAsState(
        targetValue = if (unreadCount > 0) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "badge_scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Notifications")
                            if (unreadCount > 0) {
                                Surface(
                                    modifier = Modifier.scale(badgeScale),
                                    shape = CircleShape,
                                    color = ColorPrimary,
                                    shadowElevation = 4.dp
                                ) {
                                    Text(
                                        text = "$unreadCount",
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        if (unreadCount > 0) {
                            Text(
                                "$unreadCount non lue${if (unreadCount > 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        FilledIconButton(
                            onClick = { showMarkAllDialog = true },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = ColorPrimary.copy(alpha = 0.1f),
                                contentColor = ColorPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Tout marquer comme lu"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { viewModel.refreshNotifications() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading && notifications.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ColorPrimary)
                    }
                }
                
                error != null && notifications.isEmpty() -> {
                    ErrorState(
                        message = error ?: "Erreur inconnue",
                        onRetry = { viewModel.fetchNotifications() }
                    )
                }
                
                notifications.isEmpty() -> {
                    EmptyNotificationsState()
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = notifications,
                            key = { it._id }
                        ) { notification ->
                            AnimatedNotificationItem(
                                notification = notification,
                                onClick = {
                                    if (!notification.isRead) {
                                        viewModel.markAsRead(notification._id)
                                    }
                                    
                                    // Navigation selon le type
                                    when (notification.type) {
                                        NotificationType.NEW_INSURANCE_REQUEST,
                                        NotificationType.REQUEST_STATUS_CHANGED -> {
                                            notification.data["requestId"]?.let { requestId ->
                                                onNavigateToRequestDetails(requestId)
                                            }
                                        }
                                        NotificationType.PAYMENT_CONFIRMED,
                                        NotificationType.PAYMENT_FAILED -> {
                                            notification.data["paymentId"]?.let { paymentId ->
                                                onNavigateToPaymentDetails(paymentId)
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog "Tout marquer comme lu"
    if (showMarkAllDialog) {
        AlertDialog(
            onDismissRequest = { showMarkAllDialog = false },
            icon = { Icon(Icons.Default.DoneAll, contentDescription = null, tint = ColorPrimary) },
            title = { Text("Tout marquer comme lu ?") },
            text = { Text("Toutes vos $unreadCount notifications seront marquées comme lues.") },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        viewModel.markAllAsRead()
                        showMarkAllDialog = false
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = ColorPrimary
                    )
                ) {
                    Text("Confirmer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMarkAllDialog = false }) {
                    Text("Annuler", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun AnimatedNotificationItem(
    notification: NotificationModel,
    onClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(300, easing = EaseOut)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(200)
        ) + fadeOut(animationSpec = tween(200))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (notification.isRead) 
                    MaterialTheme.colorScheme.surface 
                else 
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (notification.isRead) 2.dp else 6.dp
            )
        ) {
            Box {
                // Gradient de fond pour les notifications non lues
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        getNotificationColor(notification.type).copy(alpha = 0.05f),
                                        getNotificationColor(notification.type).copy(alpha = 0.01f)
                                    )
                                )
                            )
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Icône avec animation
                    val iconScale by animateFloatAsState(
                        targetValue = if (!notification.isRead) 1.1f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "icon_scale"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .scale(iconScale)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        getNotificationColor(notification.type).copy(alpha = 0.2f),
                                        getNotificationColor(notification.type).copy(alpha = 0.05f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getNotificationIcon(notification.type),
                            contentDescription = null,
                            tint = getNotificationColor(notification.type),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = notification.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                                color = if (notification.isRead) 
                                    MaterialTheme.colorScheme.onSurface 
                                else 
                                    MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            if (!notification.isRead) {
                                Surface(
                                    shape = CircleShape,
                                    color = ColorPrimary,
                                    modifier = Modifier.size(10.dp)
                                ) {}
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = notification.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = notification.getFormattedTime(),
                                style = MaterialTheme.typography.labelSmall,
                                color = getNotificationColor(notification.type).copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                            
                            // Badge du type de notification
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = getNotificationColor(notification.type).copy(alpha = 0.15f),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = getNotificationTypeLabel(notification.type),
                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = getNotificationColor(notification.type),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getNotificationTypeLabel(type: NotificationType): String {
    return when (type) {
        NotificationType.NEW_INSURANCE_REQUEST -> "Nouvelle demande"
        NotificationType.REQUEST_STATUS_CHANGED -> "Statut"
        NotificationType.PAYMENT_CONFIRMED -> "Paiement"
        NotificationType.PAYMENT_FAILED -> "Erreur paiement"
        NotificationType.NEW_INSURANCE_PRODUCT -> "Nouveau produit"
        NotificationType.SUBSCRIPTION_CONFIRMED -> "Abonnement"
    }
}

@Composable
fun EmptyNotificationsState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(ColorPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = ColorPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Text(
                    text = "Aucune notification",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Vous n'avez reçu aucune notification pour le moment",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = ColorError,
                modifier = Modifier.size(64.dp)
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary
                )
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Réessayer", color = Color.White)
            }
        }
    }
}

fun getNotificationIcon(type: NotificationType): ImageVector {
    return when (type) {
        NotificationType.NEW_INSURANCE_REQUEST -> Icons.Default.Notifications
        NotificationType.REQUEST_STATUS_CHANGED -> Icons.Default.Info
        NotificationType.PAYMENT_CONFIRMED -> Icons.Default.CheckCircle
        NotificationType.PAYMENT_FAILED -> Icons.Default.Error
        NotificationType.NEW_INSURANCE_PRODUCT -> Icons.Default.Add
        NotificationType.SUBSCRIPTION_CONFIRMED -> Icons.Default.Check
    }
}

fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.NEW_INSURANCE_REQUEST -> ColorPrimary
        NotificationType.REQUEST_STATUS_CHANGED -> ColorInfo
        NotificationType.PAYMENT_CONFIRMED -> ColorSuccess
        NotificationType.PAYMENT_FAILED -> ColorError
        NotificationType.NEW_INSURANCE_PRODUCT -> ColorPrimary
        NotificationType.SUBSCRIPTION_CONFIRMED -> ColorSuccess
    }
}
