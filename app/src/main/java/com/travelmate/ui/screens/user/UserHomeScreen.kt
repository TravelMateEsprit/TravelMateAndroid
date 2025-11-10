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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.travelmate.ui.theme.*
import com.travelmate.utils.Constants
import com.travelmate.utils.UserPreferences
import com.travelmate.viewmodel.VoyagesViewModel
import kotlinx.coroutines.launch

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
    onLogout: () -> Unit = {},
    navController: NavController? = null,
    userPreferences: UserPreferences,
    voyagesViewModel: VoyagesViewModel = hiltViewModel()
) {
    val userId = remember { userPreferences.getUserId() }
    var selectedTab by remember { mutableStateOf(2) } // Default to Offers tab to test
    
    // Debug logging
    LaunchedEffect(Unit) {
        android.util.Log.d("UserHomeScreen", "=== USER DEBUG INFO ===")
        android.util.Log.d("UserHomeScreen", "UserId: $userId")
        android.util.Log.d("UserHomeScreen", "UserType: ${userPreferences.getUserType()}")
        android.util.Log.d("UserHomeScreen", "UserEmail: ${userPreferences.getUserEmail()}")
        android.util.Log.d("UserHomeScreen", "IsLoggedIn: ${userPreferences.isLoggedIn()}")
    }
    
    // State for delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var voyageToDelete by remember { mutableStateOf<String?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Groups,
        BottomNavItem.Offers,
        BottomNavItem.Insurances,
        BottomNavItem.Profile
    )
    
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
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
                0 -> PlaceholderScreen("Accueil")
                1 -> PlaceholderScreen("Groupes")
                2 -> OffresScreen(
                    onVoyageClick = { voyageId ->
                        navController?.navigate("reservation/$voyageId")
                    },
                    onMyVoyageClick = { voyageId ->
                        navController?.navigate(Constants.Routes.VOYAGE_DETAIL.replace("{voyageId}", voyageId))
                    },
                    onCreateVoyage = {
                        navController?.navigate(Constants.Routes.VOYAGE_FORM)
                    },
                    onEditVoyage = { voyageId ->
                        navController?.navigate(Constants.Routes.VOYAGE_EDIT.replace("{voyageId}", voyageId))
                    },
                    onDeleteVoyage = { voyageId ->
                        voyageToDelete = voyageId
                        showDeleteDialog = true
                    },
                    onViewReservations = {
                        navController?.navigate(Constants.Routes.MY_RESERVATIONS)
                    },
                    userId = userId
                )
                3 -> InsurancesUserScreen()
                4 -> ProfileScreen(onLogout = onLogout)
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                voyageToDelete = null
            },
            title = {
                Text(
                    text = "Supprimer le voyage",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Êtes-vous sûr de vouloir supprimer ce voyage ? Cette action est irréversible.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        voyageToDelete?.let { voyageId ->
                            scope.launch {
                                voyagesViewModel.deleteVoyage(voyageId) { result ->
                                    scope.launch {
                                        if (result.isSuccess) {
                                            snackbarHostState.showSnackbar(
                                                message = "Voyage supprimé avec succès",
                                                duration = SnackbarDuration.Short
                                            )
                                        } else {
                                            snackbarHostState.showSnackbar(
                                                message = "Erreur lors de la suppression: ${result.exceptionOrNull()?.message}",
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        showDeleteDialog = false
                        voyageToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorError
                    )
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteDialog = false
                        voyageToDelete = null
                    }
                ) {
                    Text("Annuler")
                }
            }
        )
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
