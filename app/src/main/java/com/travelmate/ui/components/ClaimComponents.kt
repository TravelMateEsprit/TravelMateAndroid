package com.travelmate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ClaimStatusChip(status: String) {
    val (backgroundColor, textColor, label) = when (status) {
        "OUVERT" -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "Ouvert")
        "EN_COURS" -> Triple(Color(0xFFE3F2FD), Color(0xFF1976D2), "En cours")
        "EN_ATTENTE_CLIENT" -> Triple(Color(0xFFFFF9C4), Color(0xFFF57F17), "En attente")
        "RESOLU" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Résolu")
        "FERME" -> Triple(Color(0xFFECEFF1), Color(0xFF455A64), "Fermé")
        else -> Triple(Color.Gray, Color.White, status)
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ClaimPriorityChip(priority: String) {
    val (backgroundColor, textColor, label) = when (priority) {
        "BASSE" -> Triple(Color(0xFFE8F5E9), Color(0xFF388E3C), "Basse")
        "MOYENNE" -> Triple(Color(0xFFFFF9C4), Color(0xFFF57F17), "Moyenne")
        "HAUTE" -> Triple(Color(0xFFFFE0B2), Color(0xFFE65100), "Haute")
        "URGENTE" -> Triple(Color(0xFFFFCDD2), Color(0xFFC62828), "Urgente")
        else -> Triple(Color.Gray, Color.White, priority)
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ModernStatusBadge(status: String) {
    val (icon, backgroundColor, textColor, label) = when (status) {
        "OUVERT" -> Quadruple(
            Icons.Outlined.WatchLater,
            Color(0xFFFFF3E0),
            Color(0xFFE65100),
            "Ouvert"
        )
        "EN_COURS" -> Quadruple(
            Icons.Outlined.AutoMode,
            Color(0xFFE3F2FD),
            Color(0xFF1976D2),
            "En cours"
        )
        "EN_ATTENTE_CLIENT" -> Quadruple(
            Icons.Outlined.HourglassEmpty,
            Color(0xFFFFF9C4),
            Color(0xFFF57F17),
            "En attente"
        )
        "RESOLU" -> Quadruple(
            Icons.Outlined.CheckCircle,
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            "Résolu"
        )
        "FERME" -> Quadruple(
            Icons.Outlined.Lock,
            Color(0xFFECEFF1),
            Color(0xFF455A64),
            "Fermé"
        )
        else -> Quadruple(
            Icons.Outlined.Info,
            Color(0xFFF5F5F5),
            Color(0xFF616161),
            status
        )
    }
    
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = textColor
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

@Composable
fun ModernPriorityBadge(priority: String) {
    val (icon, backgroundColor, textColor, label) = when (priority) {
        "BASSE" -> Quadruple(
            Icons.Outlined.KeyboardArrowDown,
            Color(0xFFE8F5E9),
            Color(0xFF388E3C),
            "Basse"
        )
        "MOYENNE" -> Quadruple(
            Icons.Outlined.DragHandle,
            Color(0xFFFFF9C4),
            Color(0xFFF57F17),
            "Moyenne"
        )
        "HAUTE" -> Quadruple(
            Icons.Outlined.KeyboardArrowUp,
            Color(0xFFFFE0B2),
            Color(0xFFE65100),
            "Haute"
        )
        "URGENTE" -> Quadruple(
            Icons.Outlined.PriorityHigh,
            Color(0xFFFFCDD2),
            Color(0xFFC62828),
            "Urgent"
        )
        else -> Quadruple(
            Icons.Outlined.Info,
            Color(0xFFF5F5F5),
            Color(0xFF616161),
            priority
        )
    }
    
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = textColor
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

fun formatDate(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(dateString)
        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.FRENCH)
        formatter.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

fun formatRelativeTime(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(dateString) ?: return dateString
        
        val now = Date()
        val diffMillis = now.time - date.time
        val diffMinutes = diffMillis / (1000 * 60)
        val diffHours = diffMinutes / 60
        val diffDays = diffHours / 24
        
        when {
            diffMinutes < 1 -> "À l'instant"
            diffMinutes < 60 -> "Il y a ${diffMinutes}min"
            diffHours < 24 -> "Il y a ${diffHours}h"
            diffDays == 1L -> "Hier"
            diffDays < 7 -> "Il y a ${diffDays}j"
            else -> {
                val formatter = SimpleDateFormat("dd MMM", Locale.FRENCH)
                formatter.format(date)
            }
        }
    } catch (e: Exception) {
        dateString
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
