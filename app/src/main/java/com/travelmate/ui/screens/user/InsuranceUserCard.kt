package com.travelmate.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelmate.data.models.Insurance
import com.travelmate.ui.components.ModernButton
import com.travelmate.ui.components.ModernCard
import com.travelmate.ui.theme.*

@Composable
fun InsuranceUserCard(
    insurance: Insurance,
    onCreateRequest: (String) -> Unit,
    onUnsubscribe: (String) -> Unit,
    onCreateClaim: ((String) -> Unit)? = null,
    isInMySubscriptionsTab: Boolean = false,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        elevation = 4.dp
    ) {
        // Header with name and agency
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    insurance.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )
                Text(
                    insurance.agencyName ?: "Agence",
                    fontSize = 13.sp,
                    color = ColorTextSecondary
                )
            }
            
            // Rating
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = ColorAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    insurance.rating.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorTextPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Price and Duration Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Price Badge
            Surface(
                color = ColorPrimary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${insurance.price.toInt()} TND",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorPrimary
                    )
                }
            }
            
            // Duration Badge
            Surface(
                color = ColorSecondary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = ColorSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        insurance.duration,
                        fontSize = 13.sp,
                        color = ColorSecondary
                    )
                }
            }
            
            // Destination Badge
            insurance.conditions?.destination?.firstOrNull()?.let { dest ->
                Surface(
                    color = ColorAccent.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Public,
                            contentDescription = null,
                            tint = ColorAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            dest,
                            fontSize = 13.sp,
                            color = ColorTextPrimary
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Coverage Section
        Text(
            "Ce qui est inclus :",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = ColorTextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        insurance.coverage.take(3).forEach { coverage ->
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ColorSuccess,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    coverage,
                    fontSize = 12.sp,
                    color = ColorTextSecondary
                )
            }
        }
        
        if (insurance.coverage.size > 3) {
            TextButton(onClick = { /* Show more */ }) {
                Text("Voir plus", fontSize = 12.sp, color = ColorPrimary)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Action Buttons
        when {
            // Dans l'onglet "Mes inscriptions", afficher les boutons de réclamation et désinscription
            isInMySubscriptionsTab -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Bouton Faire une réclamation
                    onCreateClaim?.let { callback ->
                        ModernButton(
                            text = "Faire une réclamation",
                            onClick = { callback(insurance._id) },
                            backgroundColor = ColorPrimary,
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Default.Report
                        )
                    }
                    
                    // Bouton Se désinscrire
                    OutlinedButton(
                        onClick = { onUnsubscribe(insurance._id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ColorTextSecondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Se désinscrire",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            // Dans l'onglet "Toutes les assurances", afficher selon le statut d'inscription
            insurance.isSubscribed -> {
                ModernButton(
                    text = "Déjà inscrit",
                    onClick = { /* Aucune action */ },
                    backgroundColor = ColorSuccess.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            else -> {
                ModernButton(
                    text = "Faire une demande",
                    onClick = { onCreateRequest(insurance._id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
