package com.travelmate.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.travelmate.data.socket.SocketService
import com.travelmate.ui.screens.agency.AgencyMainDashboard
import com.travelmate.ui.screens.agency.InsuranceFormScreen
import com.travelmate.ui.screens.agency.InsuranceSubscribersScreen
import com.travelmate.ui.screens.groups.GroupDetailsScreen
import com.travelmate.ui.screens.groups.GroupsListScreen
import com.travelmate.ui.screens.groups.GroupMembersScreen
import com.travelmate.ui.screens.login.LoginScreen
import com.travelmate.ui.screens.login.ForgotPasswordScreen
import com.travelmate.ui.screens.login.ResetPasswordScreen
import com.travelmate.ui.screens.login.EnterResetCodeScreen
import com.travelmate.ui.screens.login.NewPasswordScreen
import com.travelmate.ui.screens.registration.agency.AgencyRegistrationScreen
import com.travelmate.ui.screens.registration.user.UserRegistrationScreen
import com.travelmate.ui.screens.user.FlightDetailsScreen
import com.travelmate.ui.screens.user.UserHomeScreen
import com.travelmate.ui.screens.welcome.WelcomeScreen
import com.travelmate.ui.screens.splash.SplashScreen
import com.travelmate.ui.user.requests.CreateInsuranceRequestScreen
import com.travelmate.ui.user.requests.MyInsuranceRequestsScreen
import com.travelmate.ui.requests.RequestDetailsScreen
import com.travelmate.ui.agency.requests.AgencyInsuranceRequestsScreen
import com.travelmate.ui.agency.requests.ReviewRequestScreen
import com.travelmate.ui.screens.payment.PaymentScreen
import com.travelmate.ui.screens.MyClaimsScreen
import com.travelmate.ui.screens.CreateClaimScreen
import com.travelmate.ui.screens.ClaimDetailScreen
import com.travelmate.ui.screens.AgencyClaimsScreen
import com.travelmate.ui.screens.AgencyClaimDetailScreen
import com.travelmate.ui.profile.UserProfileScreen
import com.travelmate.ui.profile.AgencyProfileScreen
import com.travelmate.utils.Constants
import com.travelmate.utils.UserPreferences
import com.travelmate.viewmodel.OffersViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    socketService: SocketService,
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier
) {
    // Determine start destination based on login status and user type
    val startDestination = Constants.Routes.SPLASH
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Splash Screen
        composable(Constants.Routes.SPLASH) {
            SplashScreen(
                onNavigateToNext = {
                    val destination = when {
                        !userPreferences.isLoggedIn() -> Constants.Routes.WELCOME
                        userPreferences.isAgency() -> Constants.Routes.AGENCY_DASHBOARD
                        else -> Constants.Routes.USER_HOME
                    }
                    navController.navigate(destination) {
                        popUpTo(Constants.Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        
        // Écran de bienvenue
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
        
        // Inscription utilisateur
        composable(Constants.Routes.USER_REGISTRATION) {
            UserRegistrationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegistrationSuccess = {
                    navController.navigate(Constants.Routes.LOGIN) {
                        popUpTo(Constants.Routes.WELCOME) { inclusive = false }
                    }
                }
            )
        }
        
        // Inscription agence
        composable(Constants.Routes.AGENCY_REGISTRATION) {
            AgencyRegistrationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegistrationSuccess = {
                    navController.navigate(Constants.Routes.LOGIN) {
                        popUpTo(Constants.Routes.WELCOME) { inclusive = false }
                    }
                }
            )
        }
        
        // Connexion
        composable(Constants.Routes.LOGIN) {
            LoginScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Constants.Routes.FORGOT_PASSWORD)
                },
                onLoginSuccess = { userType ->
                    // Navigate based on user type from login response
                    // Admin users cannot access mobile app
                    val destination = when (userType.lowercase()) {
                        "agence" -> Constants.Routes.AGENCY_DASHBOARD
                        "admin" -> {
                            // Admin should not access mobile app
                            android.util.Log.w("NavGraph", "Admin login attempt - access denied")
                            Constants.Routes.WELCOME
                        }
                        else -> Constants.Routes.USER_HOME
                    }
                    
                    android.util.Log.d("NavGraph", "Login success, userType: $userType, navigating to: $destination")
                    
                    navController.navigate(destination) {
                        popUpTo(Constants.Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }
        
        // Mot de passe oublié
        composable(Constants.Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEnterResetCode = {
                    navController.navigate("enter_reset_code")
                }
            )
        }
        

        // Saisie du code reçu par email
        composable("enter_reset_code") {
            EnterResetCodeScreen(
                navController = navController
            )
        }

        // Saisie du nouveau mot de passe
        composable("new_password") {
            NewPasswordScreen(
                navController = navController
            )
        }
        
        // User Home screen with bottom navigation
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
        
        // Flight Details Screen
        composable("${Constants.Routes.FLIGHT_DETAILS}/{flightId}") { backStackEntry ->
            // Get ViewModel from parent route (USER_HOME) to share the same instance
            // This ensures both OffresScreen and FlightDetailsScreen use the same ViewModel
            // OffresScreen is called from UserHomeScreen (USER_HOME route), so we use that ViewModelStoreOwner
            val parentEntry = remember(backStackEntry) {
                try {
                    navController.getBackStackEntry(Constants.Routes.USER_HOME)
                } catch (e: Exception) {
                    // Fallback to current entry if parent route not found (shouldn't happen in normal flow)
                    backStackEntry
                }
            }
            val viewModel: OffersViewModel = hiltViewModel(parentEntry)
            
            FlightDetailsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                    viewModel.clearSelectedOffer()
                },
                onBookFlight = { offer ->
                    // TODO: Navigate to booking screen
                    navController.popBackStack()
                    viewModel.clearSelectedOffer()
                }
            )
        }
        
        // Agency Dashboard
        composable(Constants.Routes.AGENCY_DASHBOARD) {
            AgencyMainDashboard(
                navController = navController,
                onNavigateToInsuranceForm = {
                    navController.navigate("insurance_form")
                },
                onEditInsurance = { insuranceId ->
                    navController.navigate("insurance_form/$insuranceId")
                },
                onViewSubscribers = { insuranceId, insuranceName ->
                    navController.navigate("insurance_subscribers/$insuranceId/$insuranceName")
                },
                onLogout = {
                    userPreferences.clearAll()
                    navController.navigate(Constants.Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // User Profile
        composable(Constants.Routes.USER_PROFILE) {
            UserProfileScreen(
                navController = navController,
                onLogout = {
                    navController.navigate(Constants.Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Agency Profile
        composable(Constants.Routes.AGENCY_PROFILE) {
            AgencyProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Insurance Form - Create
        composable("insurance_form") {
            InsuranceFormScreen(
                insuranceId = null,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Insurance Form - Edit
        composable("insurance_form/{insuranceId}") { backStackEntry ->
            InsuranceFormScreen(
                insuranceId = backStackEntry.arguments?.getString("insuranceId"),
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Insurance Subscribers
        composable("insurance_subscribers/{insuranceId}/{insuranceName}") { backStackEntry ->
            InsuranceSubscribersScreen(
                insuranceId = backStackEntry.arguments?.getString("insuranceId") ?: "",
                insuranceName = backStackEntry.arguments?.getString("insuranceName") ?: "",
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Home screen (placeholder for now)
        composable(Constants.Routes.HOME) {
            HomeScreenPlaceholder(
                onLogout = {
                    navController.navigate(Constants.Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // Insurance Request Routes - User
        composable("${Constants.Routes.CREATE_INSURANCE_REQUEST}/{insuranceId}") { backStackEntry ->
            val insuranceId = backStackEntry.arguments?.getString("insuranceId") ?: return@composable
            CreateInsuranceRequestScreen(
                navController = navController,
                insuranceId = insuranceId
            )
        }
        
        composable(Constants.Routes.MY_INSURANCE_REQUESTS) {
            MyInsuranceRequestsScreen(navController = navController)
        }
        
        composable("${Constants.Routes.REQUEST_DETAILS}/{requestId}") { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString("requestId") ?: return@composable
            RequestDetailsScreen(
                navController = navController,
                requestId = requestId,
                isAgencyView = false
            )
        }
        
        // Insurance Request Routes - Agency
        composable(Constants.Routes.AGENCY_INSURANCE_REQUESTS) {
            AgencyInsuranceRequestsScreen(navController = navController)
        }
        
        composable("${Constants.Routes.REVIEW_REQUEST}/{requestId}") { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString("requestId") ?: return@composable
            ReviewRequestScreen(
                navController = navController,
                requestId = requestId
            )
        }
        
        // Payment Route
        composable("${Constants.Routes.PAYMENT}/{requestId}") { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString("requestId") ?: return@composable
            PaymentScreen(
                requestId = requestId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPaymentSuccess = {
                    // Retour à la liste des demandes après paiement réussi
                    navController.navigate(Constants.Routes.MY_INSURANCE_REQUESTS) {
                        popUpTo(Constants.Routes.MY_INSURANCE_REQUESTS) { inclusive = true }
                    }
                }
            )
        }
        
        // Claims Routes - User
        composable(Constants.Routes.MY_CLAIMS) {
            MyClaimsScreen(navController = navController)
        }
        
        composable("${Constants.Routes.CREATE_CLAIM}?insuranceId={insuranceId}") { backStackEntry ->
            val insuranceId = backStackEntry.arguments?.getString("insuranceId")
            CreateClaimScreen(
                navController = navController,
                insuranceId = insuranceId
            )
        }
        
        composable("${Constants.Routes.CLAIM_DETAIL}/{claimId}") { backStackEntry ->
            val claimId = backStackEntry.arguments?.getString("claimId") ?: return@composable
            ClaimDetailScreen(
                navController = navController,
                claimId = claimId
            )
        }
        
        // Claims Routes - Agency
        composable(Constants.Routes.AGENCY_CLAIMS) {
            AgencyClaimsScreen(navController = navController)
        }
        
        composable("${Constants.Routes.AGENCY_CLAIM_DETAIL}/{claimId}") { backStackEntry ->
            val claimId = backStackEntry.arguments?.getString("claimId") ?: return@composable
            AgencyClaimDetailScreen(
                navController = navController,
                claimId = claimId
            )
        }
        
        //  ========== ROUTES GROUPES ==========
        
        // Liste des groupes
        composable("groups") {
            GroupsListScreen(
                onNavigateToGroupDetails = { groupId ->
                    navController.navigate("groupDetails/$groupId")
                }
            )
        }

        // Détails d'un groupe avec messages
        composable(
            route = "groupDetails/{groupId}",
            arguments = listOf(
                navArgument("groupId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            GroupDetailsScreen(
                groupId = groupId,
                onBack = {
                    navController.navigate("groups") {
                        popUpTo("groups") { inclusive = false }
                    }
                },
                onNavigateToMembers = { gid ->
                    navController.navigate("groupMembers/$gid")
                }
            )
        }

        // Membres d'un groupe
        composable(
            route = "groupMembers/{groupId}",
            arguments = listOf(
                navArgument("groupId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            GroupMembersScreen(
                groupId = groupId,
                onBack = { navController.popBackStack() }
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
        Text(
            text = "Bienvenue sur TravelMate !",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Vous êtes connecté avec succès.",
            fontSize = 16.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = onLogout) {
            Text("Se déconnecter")
        }
    }
}
