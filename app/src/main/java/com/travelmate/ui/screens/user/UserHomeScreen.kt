package com.travelmate.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.NotificationsViewModel

import com.travelmate.ui.profile.UserProfileScreen
import com.travelmate.ui.screens.groups.GroupsListScreen

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Groups : BottomNavItem("groups", Icons.Default.Group, "Groupes")
    object Offers : BottomNavItem("offers", Icons.Default.LocalOffer, "Offres")
    object Insurances : BottomNavItem("insurances", Icons.Default.Security, "Assurances")
    object Notifications : BottomNavItem("notifications", Icons.Default.Notifications, "Notifications")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profil")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    navController: NavController,
    onLogout: () -> Unit = {},
    notificationsViewModel: NotificationsViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(2) } // Default to Insurances tab (index 2)
    
    val unreadCount by notificationsViewModel.unreadCount.collectAsState()
    
    val navItems = listOf(
        BottomNavItem.Groups,
        BottomNavItem.Offers,
        BottomNavItem.Insurances,
        BottomNavItem.Notifications,
        BottomNavItem.Profile
    )
    
    Scaffold(
        bottomBar = {
            // Glassmorphism Navigation Bar - Compact et moderne
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                // Fond glassmorphism
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f),
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                    )
                                )
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            navItems.forEachIndexed { index, item ->
                                GlassmorphismNavItem(
                                    item = item,
                                    isSelected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    unreadCount = if (item is BottomNavItem.Notifications && unreadCount > 0) unreadCount else null
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> GroupsListScreen(
                    onNavigateToGroupDetails = { groupId ->
                        navController.navigate("groupDetails/$groupId")
                    }
                )
                1 -> OffresScreen(navController = navController)
                2 -> InsurancesUserScreen(navController = navController)
                3 -> com.travelmate.ui.notifications.NotificationsScreen(
                    viewModel = notificationsViewModel,
                    onNavigateToRequestDetails = { /* TODO */ },
                    onNavigateToPaymentDetails = { /* TODO */ }
                )
                4 -> UserProfileScreen(
                    navController = navController,
                    onLogout = onLogout
                )
            }
        }
    }
}

/**
 * Item de navigation avec effet glassmorphism - Style iOS moderne
 */
@Composable
private fun GlassmorphismNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    unreadCount: Int?
) {
    // Animations
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "backgroundAlpha"
    )
    
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        animationSpec = tween(300),
        label = "iconTint"
    )
    
    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        // Fond glassmorphism pour l'item sélectionné
        if (isSelected) {
            Surface(
                modifier = Modifier
                    .size(width = 46.dp, height = 46.dp)
                    .graphicsLayer { alpha = backgroundAlpha },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                )
                            )
                        )
                )
            }
        }
        
        // Contenu de l'item
        Box(
            modifier = Modifier
                .size(width = 46.dp, height = 46.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Badge pour notifications
                BadgedBox(
                    badge = {
                        if (unreadCount != null && unreadCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                modifier = Modifier.offset(x = 6.dp, y = (-5).dp)
                            ) {
                                Text(
                                    text = if (unreadCount > 9) "9+" else "$unreadCount",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onError
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(23.dp),
                        tint = iconTint
                    )
                }
                
                // Point indicateur minimaliste
                AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn(animationSpec = tween(200)) + scaleIn(
                        animationSpec = tween(200),
                        initialScale = 0.3f
                    ),
                    exit = fadeOut(animationSpec = tween(200)) + scaleOut(
                        animationSpec = tween(200),
                        targetScale = 0.3f
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

