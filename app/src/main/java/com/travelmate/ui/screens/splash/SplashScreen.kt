package com.travelmate.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelmate.R as TravelMateR
import com.travelmate.ui.theme.ColorPrimary
import com.travelmate.ui.theme.ColorSecondary
import kotlinx.coroutines.delay

/**
 * Splash Screen moderne avec animations
 * Utilise la palette de couleurs TravelMate : Vert Sauge (#8CA493) et Terre Cuite (#C88C78)
 */
@Composable
fun SplashScreen(
    onNavigateToNext: () -> Unit
) {
    // États d'animation
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.3f) }
    
    LaunchedEffect(key1 = true) {
        // Animation de fade-in pour le logo
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            )
        )
        // Animation de scale avec effet rebond
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        // Attendre avant de naviguer vers l'écran suivant
        delay(2000)
        onNavigateToNext()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                // Gradient vertical avec la palette TravelMate originale
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ColorPrimary,    // #2F80ED - Bleu primaire
                        ColorSecondary  // #56CCF2 - Cyan secondaire
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value)
        ) {
            // Logo circulaire
            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = TravelMateR.drawable.logo_travelmate),
                    contentDescription = "TravelMate Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Nom de l'application avec animation
            androidx.compose.material3.Text(
                text = "TravelMate",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tagline
            androidx.compose.material3.Text(
                text = "Votre compagnon de voyage",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Light
            )
        }
    }
}

