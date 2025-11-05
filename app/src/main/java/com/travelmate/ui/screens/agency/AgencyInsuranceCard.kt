package com.travelmate.ui.screens.agency

import androidx.compose.foundation.layout.*
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
import com.travelmate.ui.components.ModernCard
import com.travelmate.ui.theme.*

@Composable
fun AgencyInsuranceCard(
    insurance: Insurance,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit,
    onViewSubscribers: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer l'assurance") },
            text = { Text("Êtes-vous sûr de vouloir supprimer \"${insurance.name}\" ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Supprimer", color = ColorError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
    
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        elevation = 3.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    insurance.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${insurance.price.toInt()}€ • ${insurance.duration}",
                    fontSize = 13.sp,
                    color = ColorTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Subscribers Badge
                Surface(
                    color = ColorPrimary.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            tint = ColorPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${insurance.subscribers.size} inscrits",
                            fontSize = 12.sp,
                            color = ColorPrimary
                        )
                    }
                }
            }
            
            // Active Toggle
            Switch(
                checked = insurance.isActive,
                onCheckedChange = { onToggleActive() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ColorSuccess,
                    checkedTrackColor = ColorSuccess.copy(alpha = 0.5f)
                )
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Modifier", fontSize = 12.sp)
            }
            
            OutlinedButton(
                onClick = onViewSubscribers,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.People, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Inscrits", fontSize = 12.sp)
            }
            
            IconButton(
                onClick = { showDeleteDialog = true }
            ) {
                Icon(Icons.Default.Delete, "Supprimer", tint = ColorError)
            }
        }
    }
}
