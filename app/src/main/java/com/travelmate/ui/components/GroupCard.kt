package com.travelmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.travelmate.data.models.Group
import com.travelmate.ui.theme.*

@Composable
fun GroupCard(
    group: Group,
    isMyGroup: Boolean,
    isCreatedByUser: Boolean = false,
    onJoin: (String) -> Unit,
    onLeave: (String) -> Unit,
    onDelete: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Image with gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                // ✅ Images de voyage selon la destination
                val imageUrl = when {
                    !group.image.isNullOrBlank() && !group.image.contains("example.com") -> group.image
                    group.destination?.contains("Tokyo", ignoreCase = true) == true ->
                        "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=800&h=400&fit=crop"
                    group.destination?.contains("Paris", ignoreCase = true) == true ->
                        "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800&h=400&fit=crop"
                    group.destination?.contains("London", ignoreCase = true) == true ->
                        "https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?w=800&h=400&fit=crop"
                    group.destination?.contains("New York", ignoreCase = true) == true ->
                        "https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?w=800&h=400&fit=crop"
                    group.destination?.contains("Rome", ignoreCase = true) == true ->
                        "https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=800&h=400&fit=crop"
                    group.destination?.contains("Dubai", ignoreCase = true) == true ->
                        "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=400&fit=crop"
                    else -> "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=800&h=400&fit=crop"
                }

                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 0f,
                                endY = 300f
                            )
                        )
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    group.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    group.description,
                    fontSize = 14.sp,
                    color = ColorTextSecondary,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Destination
                if (!group.destination.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = ColorPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            group.destination,
                            fontSize = 13.sp,
                            color = ColorTextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Members count
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            tint = ColorTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${group.memberCount} membres",
                            fontSize = 13.sp,
                            color = ColorTextSecondary
                        )
                    }

                    // Action Button
                    when {
                        isCreatedByUser && onDelete != null -> {
                            // Groupe créé par l'utilisateur - bouton supprimer
                            OutlinedButton(
                                onClick = { onDelete(group._id) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = ColorError
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Supprimer", fontSize = 13.sp)
                            }
                        }
                        isMyGroup -> {
                            // Groupe où l'utilisateur est membre - bouton quitter
                            OutlinedButton(
                                onClick = { onLeave(group._id) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = ColorError
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.ExitToApp,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Quitter", fontSize = 13.sp)
                            }
                        }
                        else -> {
                            // Groupe où l'utilisateur n'est pas membre - bouton rejoindre ou message
                            // Vérifier isUserMember pour afficher le bon état
                            if (group.isUserMember) {
                                // Déjà inscrit - afficher message (ne pas permettre de rejoindre à nouveau)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = ColorPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Vous êtes déjà inscrit",
                                        fontSize = 13.sp,
                                        color = ColorPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else {
                                // Peut rejoindre - afficher bouton
                                Button(
                                    onClick = { onJoin(group._id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ColorPrimary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Rejoindre", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}