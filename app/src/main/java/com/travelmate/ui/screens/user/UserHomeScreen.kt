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

import com.travelmate.ui.profile.UserProfileScreen
import com.travelmate.ui.screens.groups.GroupsListScreen

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
                0 -> PlaceholderScreen("Accueil", navController)
                1 -> GroupsListScreen(
                    onNavigateToGroupDetails = { groupId ->
                        navController.navigate("groupDetails/$groupId")
                    }
                )
                2 -> OffresScreen(navController = navController)
                3 -> InsurancesUserScreen(navController = navController)
                4 -> com.travelmate.ui.notifications.NotificationsScreen(
                    viewModel = notificationsViewModel,
                    onNavigateToRequestDetails = { /* TODO */ },
                    onNavigateToPaymentDetails = { /* TODO */ }
                )
                5 -> UserProfileScreen(
                    navController = navController,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, navController: NavController? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // En-tête
        Text(
            text = "Bienvenue sur TravelMate",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ColorTextPrimary,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Carte: Mes demandes d'assurance
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController?.navigate(com.travelmate.utils.Constants.Routes.MY_INSURANCE_REQUESTS) },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(ColorPrimary, ColorPrimary.copy(alpha = 0.8f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mes demandes d'assurance",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Suivre vos demandes en cours",
                        fontSize = 14.sp,
                        color = ColorTextSecondary
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ColorTextSecondary
                )
            }
        }

        // Carte: Mes réclamations
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController?.navigate(com.travelmate.utils.Constants.Routes.MY_CLAIMS) },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(ColorSecondary, ColorSecondary.copy(alpha = 0.8f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Report,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mes réclamations",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Gérer vos réclamations",
                        fontSize = 14.sp,
                        color = ColorTextSecondary
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ColorTextSecondary
                )
            }
        }
    }
}
