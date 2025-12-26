package com.travelmate.ui.screens.welcome

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelmate.data.socket.SocketService
import com.travelmate.ui.components.*
import com.travelmate.ui.theme.*

@Composable
fun WelcomeScreen(
    onNavigateToUserRegistration: () -> Unit,
    onNavigateToAgencyRegistration: () -> Unit,
    onNavigateToLogin: () -> Unit,
    socketService: SocketService
) {
    val isConnected by socketService.connectionState.collectAsState()
    
    // Animations d'entrée
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    
    // Gérer le cycle de vie du socket
    DisposableEffect(Unit) {
        socketService.connect()
        onDispose {
            // Ne pas déconnecter ici car on veut garder la connexion active
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1500f)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section avec Logo et Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 60.dp)
            ) {
                                                                // Logo animé
                AnimatedVisibility(
                    visible = visible,
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn()
                ) {
                    Box(
                        modifier = Modifier.size(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                    Image(
                            painter = painterResource(id = com.travelmate.R.drawable.logo_travelmate),
                        contentDescription = "TravelMate logo",
                            modifier = Modifier.fillMaxSize()
                    )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Titre animé
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(
                        initialOffsetY = { -50 },
                        animationSpec = tween(600)
                    ) + fadeIn()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TravelMate",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Votre compagnon de voyage",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Light
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Indicateur de connexion seulement si non connecté
                if (!isConnected) {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(800)),
                        exit = fadeOut()
                    ) {
                        ModernConnectionStatus(
                            isConnected = isConnected,
                            modifier = Modifier.background(
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(24.dp)
                            ).padding(2.dp)
                        )
                    }
                }
            }
            
            // Bottom Section avec Boutons dans une Card
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { 100 },
                    animationSpec = tween(700, delayMillis = 200)
                ) + fadeIn()
            ) {
                ModernCard(
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    cornerRadius = 24.dp,
                    elevation = 8.dp,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Commencez votre aventure",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Bouton inscription utilisateur
                    ModernButton(
                        text = "S'inscrire en tant qu'Utilisateur",
                        onClick = onNavigateToUserRegistration,
                        enabled = isConnected,
                        icon = Icons.Default.Person,
                        backgroundColor = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Bouton inscription agence
                    ModernButton(
                        text = "S'inscrire en tant qu'Agence",
                        onClick = onNavigateToAgencyRegistration,
                        enabled = isConnected,
                        icon = Icons.Default.Business,
                        backgroundColor = ColorAccent
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Bouton connexion (outline)
                    ModernOutlineButton(
                        text = "Se connecter",
                        onClick = onNavigateToLogin,
                        enabled = isConnected,
                        icon = Icons.Default.Login
                    )
                    
                    // Message d'attente si non connecté
                    AnimatedVisibility(
                        visible = !isConnected,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = ColorAccent,
                                strokeWidth = 3.dp
                            )
                        }
                    }
                }
            }
        }
    }
}
