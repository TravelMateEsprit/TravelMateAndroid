package com.travelmate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.travelmate.data.repository.NotificationRepository
import com.travelmate.data.socket.SocketService
import com.travelmate.ui.navigation.NavGraph
import com.travelmate.ui.theme.TravelMateTheme
import com.travelmate.utils.Constants
import com.travelmate.utils.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var socketService: SocketService
    
    @Inject
    lateinit var userPreferences: UserPreferences
    
    @Inject
    lateinit var notificationRepository: NotificationRepository
    
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    // Launcher pour la permission de notifications (Android 13+)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted")
            retrieveFcmToken()
        } else {
            Log.w(TAG, "Notification permission denied")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure EdgeToEdge properly to preserve system bars
        enableEdgeToEdge()
        
        // Ensure system bars are visible and properly configured
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
        
        // Demander permission notifications et récupérer FCM token
        requestNotificationPermissionAndToken()
        
        setContent {
            TravelMateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Gérer les deep links (reset password)
                    handleDeepLink(intent) { route ->
                        navController.navigate(route)
                    }
                    
                    NavGraph(
                        navController = navController,
                        socketService = socketService,
                        userPreferences = userPreferences
                    )
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Gérer les deep links quand l'app est déjà ouverte
        handleDeepLink(intent) { route ->
            Log.d(TAG, "Deep link navigation: $route")
            // Note: Pour naviguer ici, vous auriez besoin d'une référence au navController
        }
    }
    
    /**
     * Gère les deep links pour reset password
     */
    private fun handleDeepLink(intent: Intent?, onNavigate: (String) -> Unit) {
        intent?.data?.let { uri ->
            Log.d(TAG, "Deep link received: $uri")
            
            when {
                // URL format: http://10.0.2.2:3000/reset-password.html?token=xxx
                uri.path?.contains("reset-password") == true -> {
                    val token = uri.getQueryParameter("token")
                    if (!token.isNullOrEmpty()) {
                        val route = "reset_password/$token"
                        Log.d(TAG, "Navigating to: $route")
                        onNavigate(route)
                    }
                }
                // Custom scheme: travelmate://reset-password?token=xxx
                uri.scheme == "travelmate" && uri.host == "reset-password" -> {
                    val token = uri.getQueryParameter("token")
                    if (!token.isNullOrEmpty()) {
                        val route = "reset_password/$token"
                        Log.d(TAG, "Navigating to: $route")
                        onNavigate(route)
                    }
                }
            }
        }
    }
    
    /**
     * Demande la permission pour les notifications (Android 13+)
     */
    private fun requestNotificationPermissionAndToken() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission déjà accordée
                    retrieveFcmToken()
                }
                else -> {
                    // Demander la permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Pas besoin de permission pour Android < 13
            retrieveFcmToken()
        }
    }
    
    /**
     * Récupère le token FCM et l'enregistre dans le backend
     */
    private fun retrieveFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Failed to get FCM token", task.exception)
                return@addOnCompleteListener
            }
            
            val token = task.result
            Log.d(TAG, "FCM Token: $token")
            
            // Enregistrer le token dans le backend
            activityScope.launch {
                try {
                    val result = notificationRepository.registerFcmToken(token)
                    if (result.isSuccess) {
                        Log.d(TAG, "FCM token registered with backend")
                    } else {
                        Log.e(TAG, "Failed to register FCM token", result.exceptionOrNull())
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error registering FCM token", e)
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Déconnecter le socket quand l'activité est détruite
        socketService.disconnect()
    }
}
