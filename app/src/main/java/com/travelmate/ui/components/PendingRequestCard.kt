package com.travelmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.travelmate.data.models.PendingRequest
import com.travelmate.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PendingRequestCard(
    request: PendingRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            val userName = request.userId.name?.takeIf { it.isNotBlank() }
                ?: "${request.userId.prenom ?: ""} ${request.userId.nom ?: ""}".trim()
                ?: "Utilisateur"
            AsyncImage(
                model = request.userId.photo ?: request.userId.avatar ?: "https://ui-avatars.com/api/?name=${userName}",
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ColorPrimary.copy(alpha = 0.1f)),
                contentScale = ContentScale.Crop
            )

            // Infos utilisateur
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = userName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ColorTextPrimary
                )

                Text(
                    text = request.userId.email ?: "",
                    fontSize = 13.sp,
                    color = ColorTextSecondary
                )

                // Date de la demande
                Text(
                    text = "Il y a ${getTimeAgo(request.joinedAt)}",
                    fontSize = 12.sp,
                    color = ColorTextSecondary.copy(alpha = 0.7f)
                )
            }

            // Boutons Approuver/Refuser
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorSuccess
                    ),
                    modifier = Modifier.width(100.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Accepter", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ColorError
                    ),
                    modifier = Modifier.width(100.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Refuser", fontSize = 13.sp)
                }
            }
        }
    }
}

private fun getTimeAgo(dateString: String?): String {
    if (dateString == null) return "récemment"

    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString) ?: return "récemment"

        val now = Calendar.getInstance().timeInMillis
        val then = date.time
        val diff = now - then

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7

        when {
            minutes < 1 -> "quelques secondes"
            minutes < 60 -> "$minutes minutes"
            hours < 24 -> "$hours heures"
            days < 7 -> "$days jours"
            else -> "$weeks semaines"
        }
    } catch (e: Exception) {
        "récemment"
    }
}