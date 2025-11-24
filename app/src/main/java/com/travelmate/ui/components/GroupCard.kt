package com.travelmate.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
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
    onEdit: ((String) -> Unit)? = null,
    onClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {

    Log.d("GroupCard", "Rendering card for: ${group.name}")
    Log.d("GroupCard", "  - membershipStatus: ${group.membershipStatus}")
    Log.d("GroupCard", "  - isMyGroup: $isMyGroup")
    Log.d("GroupCard", "  - isCreatedByUser: $isCreatedByUser")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) {
                onClick?.invoke(group._id)
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // ========== IMAGE ==========
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val imageUrl = when {
                    !group.image.isNullOrBlank() &&
                            group.image.startsWith("http") &&
                            !group.image.contains("example.com") -> group.image

                    group.destination?.contains("Allemagne", ignoreCase = true) == true ->
                        "https://images.unsplash.com/photo-1467269204594-9661b134dd2b?w=800"

                    group.destination?.contains("Thailande", ignoreCase = true) == true ||
                            group.destination?.contains("Thaïlande", ignoreCase = true) == true ->
                        "https://images.unsplash.com/photo-1528181304800-259b08848526?w=800"

                    else -> "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=800"
                }

                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Image du groupe",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = android.R.drawable.ic_menu_gallery),
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            }

            // ========== CONTENU ==========
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

                    // ✅ MODIFICATION : Gérer les différents états
                    when {
                        isCreatedByUser -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (onEdit != null) {
                                    OutlinedButton(
                                        onClick = { onEdit(group._id) },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = ColorPrimary
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Modifier", fontSize = 13.sp)
                                    }
                                }

                                if (onDelete != null) {
                                    OutlinedButton(
                                        onClick = { onDelete(group._id) },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = ColorError
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
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
                            }
                        }

                        isMyGroup -> {
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

                        // ✅ AJOUT : Gérer le statut PENDING
                        group.membershipStatus == "pending" -> {
                            OutlinedButton(
                                onClick = { },
                                enabled = false,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = ColorTextSecondary
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("En attente", fontSize = 13.sp)
                            }
                        }

                        else -> {
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