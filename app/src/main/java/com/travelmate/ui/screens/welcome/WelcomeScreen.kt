package com.travelmate.ui.screens.welcome

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    var visible by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(Unit) {
        visible = true
        socketService.connect()
    }

    // Palette de couleurs dynamique
    val gradientColors = if (isDark) {
        listOf(Color(0xFF0D47A1), Color(0xFF000000))
    } else {
        listOf(Color(0xFF1E88E5), Color(0xFF64B5F6), Color(0xFFBBDEFB))
    }

    val cardBackgroundColor = if (isDark) Color(0xFF1E1E1E) else Color.White

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = gradientColors))
    ) {
        // Éléments décoratifs d'arrière-plan (Cercles subtils pour la profondeur)
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-150).dp, y = (-100).dp)
                .background(Color.White.copy(alpha = 0.07f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // --- SECTION TEXTE UNIQUEMENT ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 120.dp) // Plus d'espace en haut maintenant qu'il n'y a plus de logo
            ) {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(1000)) + slideInVertically(initialOffsetY = { -20 })
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TravelMate",
                            fontSize = 54.sp, // Taille augmentée pour compenser l'absence de logo
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-1.5).sp,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Explorez le monde avec un allié",
                            fontSize = 20.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Light,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }

            // --- SECTION ACTIONS (Carte du bas) ---
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { 150 }) + fadeIn(tween(1200))
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 50.dp),
                    shape = RoundedCornerShape(35.dp),
                    color = cardBackgroundColor.copy(alpha = if (isDark) 0.9f else 1f),
                    shadowElevation = if (isDark) 0.dp else 12.dp
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Prêt pour l'aventure ?",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Bouton Voyageur
                        ModernButton(
                            text = "Compte Voyageur",
                            onClick = onNavigateToUserRegistration,
                            enabled = isConnected,
                            icon = Icons.Outlined.PersonOutline,
                            modifier = Modifier.fillMaxWidth().height(58.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Bouton Agence
                        ModernButton(
                            text = "Espace Agence",
                            onClick = onNavigateToAgencyRegistration,
                            enabled = isConnected,
                            icon = Icons.Default.BusinessCenter,
                            backgroundColor = ColorAccent,
                            modifier = Modifier.fillMaxWidth().height(58.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Lien de connexion
                        TextButton(
                            onClick = onNavigateToLogin,
                            enabled = isConnected,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Déjà membre ? ",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 16.sp
                                )
                                Text(
                                    "Connexion",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        // Status de connexion discret
                        if (!isConnected) {
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(3.dp)
                                    .clip(CircleShape),
                                color = ColorAccent,
                                trackColor = ColorAccent.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
        }
    }
}