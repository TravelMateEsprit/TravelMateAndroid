package com.travelmate.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.travelmate.data.socket.SocketService
import com.travelmate.ui.screens.agency.AgencyMainDashboard
import com.travelmate.ui.screens.agency.InsuranceFormScreen
import com.travelmate.ui.screens.agency.InsuranceSubscribersScreen
import com.travelmate.ui.screens.login.LoginScreen
import com.travelmate.ui.screens.registration.agency.AgencyRegistrationScreen
import com.travelmate.ui.screens.registration.user.UserRegistrationScreen
import com.travelmate.ui.screens.user.OffresScreen
import com.travelmate.ui.screens.user.ReservationScreen
import com.travelmate.ui.screens.user.UserHomeScreen
import com.travelmate.ui.screens.welcome.WelcomeScreen
import com.travelmate.utils.Constants
import com.travelmate.utils.UserPreferences

@Composable
fun NavGraph(
    navController: NavHostController,
    socketService: SocketService,
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier
) {
    // Determine start destination based on login status and user type
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
        
        // User Home screen with bottom navigation
        composable(Constants.Routes.USER_HOME) {
            UserHomeScreen(
                onLogout = {
                    navController.navigate(Constants.Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                navController = navController
            )
        }
        
        // Agency Dashboard
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
                onLogout = {
                    userPreferences.clearAll()
                    navController.navigate(Constants.Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
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
        
        // Reservation Screen
        composable(Constants.Routes.RESERVATION) { backStackEntry ->
            val voyageId = backStackEntry.arguments?.getString("voyageId") ?: ""
            ReservationScreen(
                voyageId = voyageId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onReservationSuccess = {
                    navController.popBackStack()
                },
                userPreferences = userPreferences
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
