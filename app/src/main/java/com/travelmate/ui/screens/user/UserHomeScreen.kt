package com.travelmate.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.travelmate.ui.screens.groups.GroupsListScreen
import com.travelmate.ui.screens.groups.GroupDetailsScreen
import com.travelmate.ui.theme.*

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Accueil")
    object Groups : BottomNavItem("groups", Icons.Default.Group, "Groupes")
    object Offers : BottomNavItem("offers", Icons.Default.LocalOffer, "Offres")
    object Insurances : BottomNavItem("insurances", Icons.Default.Security, "Assurances")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profil")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(3) } // Default to Insurances tab

    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Groups,
        BottomNavItem.Offers,
        BottomNavItem.Insurances,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = ColorBackground,
                tonalElevation = 8.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            // ❌ SUPPRIMÉ - Ne pas naviguer ici!
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ColorPrimary,
                            selectedTextColor = ColorPrimary,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = ColorTextSecondary,
                            unselectedTextColor = ColorTextSecondary
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> PlaceholderScreen("Accueil")
                1 -> {
                    // ✅ SOLUTION - Créer un NavController à chaque fois
                    val groupsNavController = rememberNavController()

                    NavHost(
                        navController = groupsNavController,
                        startDestination = "groupsList"
                    ) {
                        composable("groupsList") {
                            GroupsListScreen(
                                onNavigateToGroupDetails = { groupId ->
                                    groupsNavController.navigate("groupDetails/$groupId")
                                },
                                onNavigateToNotifications = {
                                    groupsNavController.navigate("notifications")
                                }
                            )
                        }

                        composable(
                            route = "groupDetails/{groupId}",
                            arguments = listOf(
                                navArgument("groupId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
                            GroupDetailsScreen(
                                groupId = groupId,
                                onBack = { groupsNavController.popBackStack() },
                                onNavigateToUserProfile = { userId ->
                                    groupsNavController.navigate("userProfile/$userId")
                                }
                            )
                        }

                        composable("notifications") {
                            com.travelmate.ui.screens.notifications.NotificationsScreen(
                                onNavigateToGroup = { groupId ->
                                    groupsNavController.navigate("groupDetails/$groupId")
                                },
                                onNavigateToUserProfile = { userId ->
                                    groupsNavController.navigate("userProfile/$userId")
                                },
                                onBack = { groupsNavController.popBackStack() }
                            )
                        }

                        composable(
                            route = "userProfile/{userId}",
                            arguments = listOf(
                                navArgument("userId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                            com.travelmate.ui.screens.user.UserProfileScreen(
                                userId = userId,
                                onBack = { groupsNavController.popBackStack() }
                            )
                        }
                    }
                }
                2 -> PlaceholderScreen("Offres")
                3 -> InsurancesUserScreen()
                4 -> ProfileScreen(onLogout = onLogout)
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