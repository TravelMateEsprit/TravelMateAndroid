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
import com.travelmate.ui.screens.login.LoginScreen
import com.travelmate.ui.screens.registration.agency.AgencyRegistrationScreen
import com.travelmate.ui.screens.registration.user.UserRegistrationScreen
import com.travelmate.ui.screens.welcome.WelcomeScreen
import com.travelmate.utils.Constants

@Composable
fun NavGraph(
    navController: NavHostController,
    socketService: SocketService,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Constants.Routes.WELCOME,
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
                onLoginSuccess = {
                    // TODO: Navigate to home screen when implemented
                    navController.navigate(Constants.Routes.HOME) {
                        popUpTo(Constants.Routes.WELCOME) { inclusive = true }
                    }
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
