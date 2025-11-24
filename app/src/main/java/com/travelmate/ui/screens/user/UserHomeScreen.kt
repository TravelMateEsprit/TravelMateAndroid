package com.travelmate.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.NotificationsViewModel

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Accueil")
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
    var selectedTab by remember { mutableStateOf(3) } // Default to Insurances tab
    
    val unreadCount by notificationsViewModel.unreadCount.collectAsState()
    
    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Groups,
        BottomNavItem.Offers,
        BottomNavItem.Insurances,
        BottomNavItem.Notifications,
        BottomNavItem.Profile
    )
    
    Scaffold(
        bottomBar = {
            // Nouvelle NavigationBar moderne et épurée
            Surface(
                shadowElevation = 12.dp,
                tonalElevation = 0.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navItems.forEachIndexed { index, item ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedTab = index }
                                .then(
                                    if (selectedTab == index) {
                                        Modifier.background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    ColorPrimary.copy(alpha = 0.15f),
                                                    ColorPrimary.copy(alpha = 0.08f)
                                                )
                                            )
                                        )
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Badge pour les notifications non lues
                                BadgedBox(
                                    badge = {
                                        if (item is BottomNavItem.Notifications && unreadCount > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(ColorError),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (unreadCount > 9) "9+" else "$unreadCount",
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(if (selectedTab == index) 24.dp else 22.dp),
                                        tint = if (selectedTab == index) ColorPrimary else ColorTextSecondary.copy(alpha = 0.5f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.label,
                                    fontSize = if (selectedTab == index) 11.sp else 10.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) ColorPrimary else ColorTextSecondary.copy(alpha = 0.5f)
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
                0 -> PlaceholderScreen("Accueil")
                1 -> PlaceholderScreen("Groupes")
                2 -> PlaceholderScreen("Offres")
                3 -> InsurancesUserScreen(navController = navController)
                4 -> com.travelmate.ui.notifications.NotificationsScreen(
                    viewModel = notificationsViewModel,
                    onNavigateToRequestDetails = { /* TODO */ },
                    onNavigateToPaymentDetails = { /* TODO */ }
                )
                5 -> ProfileScreen(onLogout = onLogout)
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = "Écran $title",
            style = MaterialTheme.typography.headlineMedium,
            color = ColorTextSecondary
        )
    }
}
