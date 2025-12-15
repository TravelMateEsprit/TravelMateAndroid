package com.travelmate.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.travelmate.ui.theme.*
import kotlinx.coroutines.launch

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Packs : BottomNavItem("packs", Icons.Default.Inventory, "Packs")
    object Groups : BottomNavItem("groups", Icons.Default.Group, "Groupes")
    object Offers : BottomNavItem("offers", Icons.Default.LocalOffer, "Offres")
    object Insurances : BottomNavItem("insurances", Icons.Default.Security, "Assurances")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profil")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    navController: NavController? = null,
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) } // Default to Packs tab
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val navItems = listOf(
        BottomNavItem.Packs,
        BottomNavItem.Groups,
        BottomNavItem.Offers,
        BottomNavItem.Insurances,
        BottomNavItem.Profile
    )
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White
            ) {
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(ColorPrimary, ColorPrimary.copy(alpha = 0.8f))
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "TravelMate",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Utilisateur",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation Items
                if (navController != null) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                        label = { Text("Mes Favoris", fontWeight = FontWeight.Medium) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("user_favorites")
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = ColorPrimary.copy(alpha = 0.1f),
                            selectedTextColor = ColorPrimary,
                            selectedIconColor = ColorPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.EventNote, contentDescription = null) },
                        label = { Text("Mes Réservations", fontWeight = FontWeight.Medium) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("user_reservations")
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = ColorPrimary.copy(alpha = 0.1f),
                            selectedTextColor = ColorPrimary,
                            selectedIconColor = ColorPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Chat, contentDescription = null) },
                        label = { Text("Messages", fontWeight = FontWeight.Medium) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            // TODO: Navigate to conversations list
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = ColorPrimary.copy(alpha = 0.1f),
                            selectedTextColor = ColorPrimary,
                            selectedIconColor = ColorPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("TravelMate") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ColorPrimary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = ColorBackground,
                    tonalElevation = 8.dp
                ) {
                    navItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
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
                    0 -> {
                        // Packs browsing screen
                        if (navController != null) {
                            PacksBrowseScreen(
                                onNavigateToPackDetails = { packId ->
                                    navController.navigate("user_pack_details/$packId")
                                },
                                onNavigateToFavorites = {
                                    navController.navigate("user_favorites")
                                },
                                onNavigateToConversations = {
                                    navController.navigate("user_conversations")
                                }
                            )
                        } else {
                            PlaceholderScreen("Packs")
                        }
                    }
                    1 -> PlaceholderScreen("Groupes")
                    2 -> OffresScreen(navController = navController)
                    3 -> InsurancesUserScreen()
                    4 -> ProfileScreen(onLogout = onLogout)
                }
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
