package com.travelmate.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape  // ✅ AJOUT DE L'IMPORT
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelmate.ui.theme.ColorTextSecondary

@Composable
fun TypingIndicator(
    typingUserCount: Int,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = typingUserCount > 0,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Animation des points
            TypingDots()

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = when {
                    typingUserCount == 1 -> "Quelqu'un est en train d'écrire..."
                    typingUserCount == 2 -> "2 personnes sont en train d'écrire..."
                    else -> "Plusieurs personnes sont en train d'écrire..."
                },
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                color = ColorTextSecondary
            )
        }
    }
}

@Composable
private fun TypingDots() {
    val infiniteTransition = rememberInfiniteTransition()

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 200)
                )
            )

            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        color = ColorTextSecondary.copy(alpha = alpha),
                        shape = CircleShape  // ✅ CORRECTION ICI
                    )
            )
        }
    }
}