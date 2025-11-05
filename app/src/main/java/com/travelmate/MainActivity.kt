package com.travelmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.travelmate.data.socket.SocketService
import com.travelmate.ui.navigation.NavGraph
import com.travelmate.ui.theme.TravelMateTheme
import com.travelmate.utils.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var socketService: SocketService
    
    @Inject
    lateinit var userPreferences: UserPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            TravelMateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    NavGraph(
                        navController = navController,
                        socketService = socketService,
                        userPreferences = userPreferences
                    )
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
