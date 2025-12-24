package com.travelmate.ui.notifications

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.travelmate.data.model.NotificationModel
import com.travelmate.data.model.NotificationType
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.NotificationsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Notifications Screen moderne et efficient
 * Design compact, scannable et actionnable
 * Support des swipe gestures et filtres
 */
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

    var selectedFilter by remember { mutableStateOf<NotificationFilter>(NotificationFilter.ALL) }
    var showMarkAllDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Filtrer les notifications selon le filtre sélectionné
    val filteredNotifications = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            NotificationFilter.ALL -> notifications
            NotificationFilter.UNREAD -> notifications.filter { !it.isRead }
            NotificationFilter.REQUESTS -> notifications.filter { 
                it.type == NotificationType.NEW_INSURANCE_REQUEST || 
                it.type == NotificationType.REQUEST_STATUS_CHANGED 
            }
            NotificationFilter.SYSTEM -> notifications.filter { 
                it.type == NotificationType.NEW_INSURANCE_PRODUCT 
            }
        }
    }

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            EfficientNotificationsTopBar(
                unreadCount = unreadCount,
                selectedFilter = selectedFilter,
                onFilterChange = { selectedFilter = it },
                onMarkAllAsRead = { showMarkAllDialog = true },
                colorScheme = colorScheme
            )
        },
        containerColor = colorScheme.background,
        contentWindowInsets = WindowInsets.systemBars
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
                    NotificationsSkeletonLoader(colorScheme = colorScheme)
                }

                error != null && notifications.isEmpty() -> {
                    EfficientErrorState(
                        message = error ?: "Erreur inconnue",
                        onRetry = { viewModel.fetchNotifications() },
                        colorScheme = colorScheme
                    )
                }

                filteredNotifications.isEmpty() -> {
                    EfficientEmptyState(filter = selectedFilter, colorScheme = colorScheme)
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            end = 12.dp,
                            top = 8.dp,
                            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 12.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(
                            items = filteredNotifications,
                            key = { _, notification -> notification._id }
                        ) { index, notification ->
                            CompactNotificationItem(
                                notification = notification,
                                index = index,
                                onMarkAsRead = {
                                    if (!notification.isRead) {
                                        viewModel.markAsRead(notification._id)
                                    }
                                },
                                onDelete = {
                                    showDeleteDialog = notification._id
                                },
                                onClick = {
                                    if (!notification.isRead) {
                                        viewModel.markAsRead(notification._id)
                                    }

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
        EfficientAlertDialog(
            onDismiss = { showMarkAllDialog = false },
            onConfirm = {
                viewModel.markAllAsRead()
                showMarkAllDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("Toutes les notifications ont été marquées comme lues")
                }
            },
            title = "Tout marquer comme lu ?",
            message = "Toutes vos $unreadCount notifications seront marquées comme lues.",
            confirmText = "Confirmer",
            colorScheme = colorScheme
        )
    }

    // Dialog de suppression
    showDeleteDialog?.let { notificationId ->
        EfficientAlertDialog(
            onDismiss = { showDeleteDialog = null },
            onConfirm = {
                // TODO: Implémenter la suppression dans le ViewModel
                showDeleteDialog = null
                scope.launch {
                    snackbarHostState.showSnackbar("Notification supprimée")
                }
            },
            title = "Supprimer la notification ?",
            message = "Cette action est irréversible.",
            confirmText = "Supprimer",
            isDestructive = true,
            colorScheme = colorScheme
        )
    }
}

/**
 * Enum pour les filtres de notifications
 */
enum class NotificationFilter(val label: String) {
    ALL("Toutes"),
    UNREAD("Non lues"),
    REQUESTS("Demandes"),
    SYSTEM("Système")
}

/**
 * Top Bar efficient avec filtres
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EfficientNotificationsTopBar(
    unreadCount: Int,
    selectedFilter: NotificationFilter,
    onFilterChange: (NotificationFilter) -> Unit,
    onMarkAllAsRead: () -> Unit,
    colorScheme: ColorScheme
) {
    Column {
        // Top App Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            color = colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Notifications",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    if (unreadCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colorScheme.primary
                        ) {
                            Text(
                                text = "$unreadCount",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onPrimary
                            )
                        }
                    }
                }

                if (unreadCount > 0) {
                    IconButton(
                        onClick = onMarkAllAsRead,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Tout marquer comme lu"
                        )
                    }
                }
            }
        }

        // Filtres avec chips
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotificationFilter.values().forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { onFilterChange(filter) },
                        label = {
                            Text(
                                filter.label,
                                fontSize = 13.sp,
                                fontWeight = if (selectedFilter == filter) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorScheme.primary.copy(alpha = 0.15f),
                            selectedLabelColor = colorScheme.primary,
                            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            labelColor = colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Item de notification compact (72-80dp)
 */
@Composable
private fun CompactNotificationItem(
    notification: NotificationModel,
    index: Int,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var swipeOffset by remember { mutableStateOf(0f) }
    var isSwiping by remember { mutableStateOf(false) }

    // Animation d'entrée
    val enterAnimation = remember {
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = index * 30,
                easing = FastOutSlowInEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = index * 30
            )
        )
    }

    AnimatedVisibility(
        visible = true,
        enter = enterAnimation,
        exit = slideOutHorizontally() + fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Actions de swipe (marquer comme lu / supprimer)
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(end = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bouton marquer comme lu
                if (!notification.isRead && swipeOffset < -50) {
                    IconButton(
                        onClick = {
                            onMarkAsRead()
                            swipeOffset = 0f
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = ColorSuccess.copy(alpha = 0.9f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Marquer comme lu",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Bouton supprimer
                if (swipeOffset > 50) {
                    IconButton(
                        onClick = {
                            onDelete()
                            swipeOffset = 0f
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = ColorError.copy(alpha = 0.9f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Card de notification
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = swipeOffset.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                isSwiping = false
                                if (swipeOffset < -100) {
                                    onMarkAsRead()
                                } else if (swipeOffset > 100) {
                                    onDelete()
                                }
                                swipeOffset = 0f
                            }
                        ) { _, dragAmount ->
                            isSwiping = true
                            swipeOffset = (swipeOffset + dragAmount).coerceIn(-200f, 200f)
                        }
                    }
                    .clickable(onClick = onClick),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (notification.isRead) {
                        colorScheme.surface
                    } else {
                        colorScheme.primary.copy(alpha = 0.05f)
                    }
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (notification.isRead) 1.dp else 2.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .heightIn(min = 72.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Barre latérale colorée pour les non lues
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(3.dp)
                                .background(
                                    color = getNotificationColor(notification.type),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    } else {
                        Spacer(modifier = Modifier.width(3.dp))
                    }

                    // Icône compacte
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = getNotificationColor(notification.type).copy(alpha = 0.12f)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getNotificationIcon(notification.type),
                                contentDescription = null,
                                tint = getNotificationColor(notification.type),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Contenu compact
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Titre avec badge si non lu
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = notification.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                                color = colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (!notification.isRead) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = colorScheme.primary,
                                    modifier = Modifier.size(8.dp)
                                ) {}
                            }
                        }

                        // Message
                        Text(
                            text = notification.body,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )

                        // Timestamp et badge de statut
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = notification.getFormattedTime(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }

                            // Badge de statut compact
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = getNotificationColor(notification.type).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = getNotificationTypeLabel(notification.type),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = getNotificationColor(notification.type),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * État vide efficient
 */
@Composable
private fun EfficientEmptyState(
    filter: NotificationFilter,
    colorScheme: ColorScheme
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
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsNone,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Text(
                text = when (filter) {
                    NotificationFilter.ALL -> "Aucune notification"
                    NotificationFilter.UNREAD -> "Aucune notification non lue"
                    NotificationFilter.REQUESTS -> "Aucune demande"
                    NotificationFilter.SYSTEM -> "Aucune notification système"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )

            Text(
                text = when (filter) {
                    NotificationFilter.ALL -> "Vous n'avez reçu aucune notification pour le moment"
                    NotificationFilter.UNREAD -> "Toutes vos notifications ont été lues"
                    NotificationFilter.REQUESTS -> "Aucune demande d'assurance en cours"
                    NotificationFilter.SYSTEM -> "Aucune notification système"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * État d'erreur efficient
 */
@Composable
private fun EfficientErrorState(
    message: String,
    onRetry: () -> Unit,
    colorScheme: ColorScheme
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
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = colorScheme.error.copy(alpha = 0.1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Réessayer", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * Skeleton loader pour les notifications
 */
@Composable
private fun NotificationsSkeletonLoader(colorScheme: ColorScheme) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(6) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(72.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog efficient
 */
@Composable
private fun EfficientAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    confirmText: String,
    isDestructive: Boolean = false,
    colorScheme: ColorScheme
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = if (isDestructive) colorScheme.error.copy(alpha = 0.1f) else colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isDestructive) Icons.Default.Delete else Icons.Default.DoneAll,
                        contentDescription = null,
                        tint = if (isDestructive) colorScheme.error else colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        title = {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface
            )
        },
        text = {
            Text(
                message,
                color = colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) colorScheme.error else colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(confirmText, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Annuler")
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = colorScheme.surface
    )
}

/**
 * Helper functions
 */
@Composable
private fun getNotificationIcon(type: NotificationType): ImageVector {
    return when (type) {
        NotificationType.NEW_INSURANCE_REQUEST -> Icons.Default.Notifications
        NotificationType.REQUEST_STATUS_CHANGED -> Icons.Default.Info
        NotificationType.PAYMENT_CONFIRMED -> Icons.Default.CheckCircle
        NotificationType.PAYMENT_FAILED -> Icons.Default.Error
        NotificationType.NEW_INSURANCE_PRODUCT -> Icons.Default.AddCircle
        NotificationType.SUBSCRIPTION_CONFIRMED -> Icons.Default.CheckCircle
    }
}

@Composable
private fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.NEW_INSURANCE_REQUEST -> ColorPrimary
        NotificationType.REQUEST_STATUS_CHANGED -> ColorInfo
        NotificationType.PAYMENT_CONFIRMED -> ColorSuccess
        NotificationType.PAYMENT_FAILED -> ColorError
        NotificationType.NEW_INSURANCE_PRODUCT -> ColorPrimary
        NotificationType.SUBSCRIPTION_CONFIRMED -> ColorSuccess
    }
}

@Composable
private fun getNotificationTypeLabel(type: NotificationType): String {
    return when (type) {
        NotificationType.NEW_INSURANCE_REQUEST -> "Nouvelle demande"
        NotificationType.REQUEST_STATUS_CHANGED -> "Statut"
        NotificationType.PAYMENT_CONFIRMED -> "Paiement"
        NotificationType.PAYMENT_FAILED -> "Erreur"
        NotificationType.NEW_INSURANCE_PRODUCT -> "Nouveau produit"
        NotificationType.SUBSCRIPTION_CONFIRMED -> "Abonnement"
    }
}
