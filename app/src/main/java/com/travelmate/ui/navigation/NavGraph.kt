package com.travelmate.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.travelmate.data.models.Pack
import com.travelmate.data.socket.SocketService
import com.travelmate.ui.screens.agency.AgencyMainDashboard
import com.travelmate.ui.screens.agency.AgencyPacksScreen
import com.travelmate.ui.screens.agency.AgencyReservationsScreen
import com.travelmate.ui.screens.agency.ChatScreen
import com.travelmate.ui.screens.agency.CreatePackScreen
import com.travelmate.ui.screens.agency.EditPackScreen
import com.travelmate.ui.screens.agency.InsuranceFormScreen
import com.travelmate.ui.screens.agency.InsuranceSubscribersScreen
import com.travelmate.ui.screens.agency.PackDetailsScreen
import com.travelmate.ui.screens.login.LoginScreen
import com.travelmate.ui.screens.registration.agency.AgencyRegistrationScreen
import com.travelmate.ui.screens.registration.user.UserRegistrationScreen
import com.travelmate.ui.screens.user.FlightDetailsScreen
import com.travelmate.ui.screens.user.UserChatScreen
import com.travelmate.ui.screens.user.UserHomeScreen
import com.travelmate.ui.screens.user.UserReservationsScreen
import com.travelmate.ui.screens.welcome.WelcomeScreen
import com.travelmate.utils.Constants
import com.travelmate.utils.UserPreferences
import com.travelmate.viewmodel.AgencyPacksViewModel
import com.travelmate.viewmodel.OffersViewModel
import com.travelmate.ui.theme.ColorPrimary

@Composable
fun NavGraph(
    navController: NavHostController,
    socketService: SocketService,
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier
) {
    val startDestination = when {
        !userPreferences.isLoggedIn() -> Constants.Routes.WELCOME
        userPreferences.isAgency() -> Constants.Routes.AGENCY_DASHBOARD
        else -> Constants.Routes.USER_HOME
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable(Constants.Routes.WELCOME) {
            WelcomeScreen(
                onNavigateToUserRegistration = {
                    navController.navigate(Constants.Routes.USER_REGISTRATION)
                },
                onNavigateToAgencyRegistration = {
                    navController.navigate(Constants.Routes.AGENCY_REGISTRATION)
                },
                onNavigateToLogin = {
                    navController.navigate(Constants.Routes.LOGIN)
                },
                socketService = socketService
            )
        }

        composable(Constants.Routes.USER_REGISTRATION) {
            UserRegistrationScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegistrationSuccess = {
                    navController.navigate(Constants.Routes.LOGIN) {
                        popUpTo(Constants.Routes.WELCOME) { inclusive = false }
                    }
                }
            )
        }

        composable(Constants.Routes.AGENCY_REGISTRATION) {
            AgencyRegistrationScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegistrationSuccess = {
                    navController.navigate(Constants.Routes.LOGIN) {
                        popUpTo(Constants.Routes.WELCOME) { inclusive = false }
                    }
                }
            )
        }

        composable(Constants.Routes.LOGIN) {
            LoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = { userType ->
                    val destination = when (userType.lowercase()) {
                        "agence" -> Constants.Routes.AGENCY_DASHBOARD
                        "admin" -> Constants.Routes.WELCOME
                        else -> Constants.Routes.USER_HOME
                    }

                    navController.navigate(destination) {
                        popUpTo(Constants.Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Constants.Routes.USER_HOME) {
            UserHomeScreen(
                navController = navController,
                onLogout = {
                    navController.navigate(Constants.Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("user_packs_browse") {
            com.travelmate.ui.screens.user.PacksBrowseScreen(
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
        }

        composable(
            route = "user_pack_details/{packId}",
            arguments = listOf(navArgument("packId") { type = NavType.StringType })
        ) { backStackEntry ->
            val packId = backStackEntry.arguments?.getString("packId") ?: ""
            com.travelmate.ui.screens.user.UserPackDetailScreen(
                packId = packId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { agencyId, packId, packName ->
                    android.util.Log.d("NavGraph", "Navigating to chat: agencyId=$agencyId, packId=$packId, packName=$packName")
                    try {
                        val encodedPackName = packName?.replace(" ", "%20") ?: ""
                        navController.navigate("user_chat/$agencyId/$packId?packName=$encodedPackName")
                    } catch (e: Exception) {
                        android.util.Log.e("NavGraph", "Error navigating to chat: ${e.message}", e)
                    }
                },
                onNavigateToConversations = {
                    navController.navigate("user_conversations")
                }
            )
        }

        composable("user_favorites") {
            com.travelmate.ui.screens.user.FavoritesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPackDetails = { packId ->
                    navController.navigate("user_pack_details/$packId")
                }
            )
        }

        composable("user_reservations") {
            UserReservationsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPackDetails = { packId ->
                    navController.navigate("user_pack_details/$packId")
                }
            )
        }

        composable("user_conversations") {
            com.travelmate.ui.screens.user.ConversationsListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { agencyId, packId ->
                    android.util.Log.d("NavGraph", "onNavigateToChat from conversations list: agencyId=$agencyId, packId=$packId")
                    if (agencyId.isEmpty()) {
                        android.util.Log.e("NavGraph", "Cannot navigate: agencyId is empty")
                    } else {
                        try {
                            val route = if (packId != null && packId.isNotEmpty()) {
                                "user_chat/$agencyId/$packId"
                            } else {
                                "user_chat/$agencyId"
                            }
                            android.util.Log.d("NavGraph", "Navigating to: $route")
                            navController.navigate(route)
                        } catch (e: Exception) {
                            android.util.Log.e("NavGraph", "Error navigating to chat: ${e.message}", e)
                        }
                    }
                }
            )
        }

        composable(
            route = "user_chat/{agencyId}",
            arguments = listOf(navArgument("agencyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val agencyId = backStackEntry.arguments?.getString("agencyId") ?: ""
            val viewModel: com.travelmate.viewmodel.ChatViewModel = hiltViewModel()

            if (agencyId.isEmpty()) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                LaunchedEffect(Unit) {
                    try {
                        viewModel.loadConversations()
                    } catch (e: Exception) {
                        android.util.Log.e("NavGraph", "Error loading conversations: ${e.message}", e)
                    }
                }

                val conversations by viewModel.conversations.collectAsState()
                val conversation = remember(conversations, agencyId) {
                    conversations.find { it.agencyId == agencyId && it.packId == null }
                }
                val agencyName = conversation?.agencyName ?: "Agence"

                com.travelmate.ui.screens.user.UserChatScreen(
                    agencyId = agencyId,
                    packId = null,
                    agencyName = agencyName,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = "user_chat/{agencyId}/{packId}?packName={packName}",
            arguments = listOf(
                navArgument("agencyId") { type = NavType.StringType },
                navArgument("packId") { type = NavType.StringType },
                navArgument("packName") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val agencyId = backStackEntry.arguments?.getString("agencyId") ?: ""
            val packId = backStackEntry.arguments?.getString("packId")
            val packName = backStackEntry.arguments?.getString("packName")
            val viewModel: com.travelmate.viewmodel.ChatViewModel = hiltViewModel()

            if (agencyId.isEmpty()) {
                LaunchedEffect(Unit) {
                    android.util.Log.e("NavGraph", "Cannot open chat: agencyId is empty")
                    navController.popBackStack()
                }
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Erreur: ID d'agence manquant", color = Color.Red)
                }
            } else {
                LaunchedEffect(Unit) {
                    try {
                        viewModel.loadConversations()
                    } catch (e: Exception) {
                        android.util.Log.e("NavGraph", "Error loading conversations: ${e.message}", e)
                    }
                }

                val conversations by viewModel.conversations.collectAsState()
                val conversation = remember(conversations, agencyId, packId) {
                    conversations.find { it.agencyId == agencyId && it.packId == packId }
                }
                val agencyName = conversation?.agencyName ?: "Agence"

                com.travelmate.ui.screens.user.UserChatScreen(
                    agencyId = agencyId,
                    packId = packId,
                    packName = packName,
                    agencyName = agencyName,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable("${Constants.Routes.FLIGHT_DETAILS}/{flightId}") { backStackEntry ->
            val viewModel: OffersViewModel = hiltViewModel()
            val flightId = backStackEntry.arguments?.getString("flightId") ?: ""
            val offers by viewModel.offers.collectAsState()

            val selectedOffer = offers.find { it.id == flightId }

            if (selectedOffer != null) {
                FlightDetailsScreen(
                    flightOffer = selectedOffer,
                    onNavigateBack = { navController.popBackStack() },
                    onBookFlight = { navController.popBackStack() }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Vol introuvable", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Retour")
                        }
                    }
                }
            }
        }

        composable(Constants.Routes.AGENCY_DASHBOARD) {
            AgencyMainDashboard(
                onNavigateToInsuranceForm = {
                    navController.navigate("insurance_form")
                },
                onEditInsurance = { insuranceId ->
                    navController.navigate("insurance_form/$insuranceId")
                },
                onViewSubscribers = { insuranceId, insuranceName ->
                    navController.navigate("insurance_subscribers/$insuranceId/$insuranceName")
                },
                onNavigateToPacksList = {
                    navController.navigate("agency_packs")
                },
                onNavigateToReservations = {
                    navController.navigate("agency_reservations")
                },
                onLogout = {
                    userPreferences.clearAll()
                    navController.navigate(Constants.Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("insurance_form") {
            InsuranceFormScreen(
                insuranceId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("insurance_form/{insuranceId}") { backStackEntry ->
            InsuranceFormScreen(
                insuranceId = backStackEntry.arguments?.getString("insuranceId"),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("insurance_subscribers/{insuranceId}/{insuranceName}") { backStackEntry ->
            InsuranceSubscribersScreen(
                insuranceId = backStackEntry.arguments?.getString("insuranceId") ?: "",
                insuranceName = backStackEntry.arguments?.getString("insuranceName") ?: "",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("agency_packs") { backStackEntry ->
            val viewModel: AgencyPacksViewModel = hiltViewModel()
            val packs by viewModel.filteredPacks.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()
            val error by viewModel.error.collectAsState()

            // Load packs whenever this screen is shown
            LaunchedEffect(backStackEntry) {
                android.util.Log.d("NavGraph", "Loading packs for agency_packs screen")
                viewModel.loadMyPacks()
            }

            AgencyPacksScreen(
                packs = packs,
                isLoading = isLoading,
                error = error,
                onRetry = { viewModel.loadMyPacks() },
                onNavigateToCreatePack = {
                    navController.navigate("create_pack")
                },
                onOpenPackDetails = { packId ->
                    navController.navigate("pack_details/$packId")
                },
                onNavigateToConversations = {
                    navController.navigate("chat")
                },
                onNavigateToReservations = {
                    navController.navigate("agency_reservations")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("create_pack") { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("agency_packs")
            }
            val viewModel: AgencyPacksViewModel = hiltViewModel(parentEntry)

            CreatePackScreen(
                onNavigateBack = { navController.popBackStack() },
                onCreatePack = { request ->
                    viewModel.createPack(request)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "pack_details/{packId}",
            arguments = listOf(navArgument("packId") { type = NavType.StringType })
        ) { backStackEntry ->
            val packId = backStackEntry.arguments?.getString("packId") ?: ""
            PackDetailsScreen(
                packId = packId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate("pack_edit/$id")
                }
            )
        }

        composable(
            route = "pack_edit/{packId}",
            arguments = listOf(navArgument("packId") { type = NavType.StringType })
        ) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("agency_packs")
            }
            val viewModel: AgencyPacksViewModel = hiltViewModel(parentEntry)
            val packId = backStackEntry.arguments?.getString("packId") ?: ""
            val packs by viewModel.packs.collectAsState()
            val pack = packs.find { it.id == packId }

            LaunchedEffect(packId, pack) {
                if (pack == null) {
                    viewModel.loadMyPacks()
                }
            }

            if (pack != null) {
                EditPackScreen(
                    pack = pack,
                    onBack = { navController.popBackStack() },
                    onUpdatePack = { updatedPack ->
                        viewModel.updatePack(packId, updatedPack)
                        navController.popBackStack()
                    }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ColorPrimary)
                }
            }
        }

        composable("chat") {
            ChatScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("agency_reservations") {
            AgencyReservationsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPacks = {
                    navController.popBackStack()
                    navController.navigate("agency_packs")
                },
                onOpenReservation = { reservation ->
                    val packId = reservation.packId
                    if (!packId.isNullOrBlank()) {
                        navController.navigate("pack_details/$packId")
                    }
                }
            )
        }

        composable(Constants.Routes.HOME) {
            HomeScreenPlaceholder(
                onLogout = {
                    navController.navigate(Constants.Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun HomeScreenPlaceholder(onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Bienvenue sur TravelMate !", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Vous êtes connecté avec succès.", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onLogout) {
            Text("Se déconnecter")
        }
    }
}
