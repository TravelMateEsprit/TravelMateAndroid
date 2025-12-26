package com.travelmate.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelmate.ui.theme.ColorPrimary

data class TravelPurposeOption(
    val label: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelPurposeSelector(
    selectedPurpose: String,
    onPurposeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val purposes = remember {
        listOf(
            TravelPurposeOption("Tourisme", Icons.Default.Landscape),
            TravelPurposeOption("Affaires", Icons.Default.Business),
            TravelPurposeOption("Études", Icons.Default.School),
            TravelPurposeOption("Visite familiale", Icons.Default.FamilyRestroom),
            TravelPurposeOption("Santé", Icons.Default.LocalHospital),
            TravelPurposeOption("Sport", Icons.Default.SportsBasketball),
            TravelPurposeOption("Culture", Icons.Default.Museum),
            TravelPurposeOption("Aventure", Icons.Default.Hiking)
        )
    }
    
    var showCustomInput by remember { mutableStateOf(false) }
    var customPurpose by remember { mutableStateOf("") }
    
    // Si la valeur actuelle n'est pas dans les suggestions, activer le mode personnalisé
    LaunchedEffect(selectedPurpose) {
        if (selectedPurpose.isNotBlank() && purposes.none { it.label == selectedPurpose }) {
            customPurpose = selectedPurpose
            showCustomInput = true
        }
    }
    
    Column(modifier = modifier) {
        Text(
            text = "Motif du voyage",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Suggestions de motifs
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(purposes) { purpose ->
                FilterChip(
                    selected = selectedPurpose == purpose.label,
                    onClick = {
                        onPurposeSelected(purpose.label)
                        showCustomInput = false
                    },
                    label = { Text(purpose.label) },
                    leadingIcon = {
                        Icon(
                            purpose.icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Option "Autre"
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCustomInput = !showCustomInput },
            border = BorderStroke(
                width = 1.dp,
                color = if (showCustomInput) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = if (showCustomInput) MaterialTheme.colorScheme.primary 
                              else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Autre motif (personnalisé)",
                        color = if (showCustomInput) MaterialTheme.colorScheme.primary 
                               else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                RadioButton(
                    selected = showCustomInput,
                    onClick = { showCustomInput = !showCustomInput }
                )
            }
        }
        
        // Champ de saisie personnalisé
        AnimatedVisibility(visible = showCustomInput) {
            OutlinedTextField(
                value = customPurpose,
                onValueChange = { 
                    customPurpose = it
                    onPurposeSelected(it)
                },
                label = { Text("Décrivez votre motif") },
                placeholder = { Text("Ex: Pèlerinage, Conférence...") },
                leadingIcon = { Icon(Icons.Default.Description, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                singleLine = true
            )
        }
    }
}
