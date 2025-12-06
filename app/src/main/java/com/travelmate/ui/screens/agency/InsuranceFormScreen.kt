package com.travelmate.ui.screens.agency

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.travelmate.data.models.*
import com.travelmate.ui.components.ModernButton
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.AgencyDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsuranceFormScreen(
    insuranceId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: AgencyDashboardViewModel = hiltViewModel()
) {
    val insurances by viewModel.myInsurances.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val editingInsurance = insurances.find { it._id == insuranceId }
    val isEditMode = editingInsurance != null
    
    // Form state
    var name by remember { mutableStateOf(editingInsurance?.name ?: "") }
    var description by remember { mutableStateOf(editingInsurance?.description ?: "") }
    var priceText by remember { mutableStateOf(editingInsurance?.price?.toString() ?: "") }
    var duration by remember { mutableStateOf(editingInsurance?.duration ?: "") }
    var coverageText by remember { mutableStateOf(editingInsurance?.coverage?.joinToString(", ") ?: "") }
    var imageUrl by remember { mutableStateOf(editingInsurance?.imageUrl ?: "") }
    var isActive by remember { mutableStateOf(editingInsurance?.isActive ?: true) }
    
    // Conditions state
    var showConditions by remember { mutableStateOf(editingInsurance?.conditions != null) }
    var ageMinText by remember { mutableStateOf(editingInsurance?.conditions?.ageMin?.toString() ?: "") }
    var ageMaxText by remember { mutableStateOf(editingInsurance?.conditions?.ageMax?.toString() ?: "") }
    var destinationsText by remember { mutableStateOf(editingInsurance?.conditions?.destination?.joinToString(", ") ?: "") }
    var otherConditions by remember { mutableStateOf(editingInsurance?.conditions?.other ?: "") }
    
    var formError by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                onNavigateBack()
            },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ColorSuccess,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Succès") },
            text = { 
                Text(if (isEditMode) "Assurance modifiée avec succès" else "Assurance créée avec succès")
            },
            confirmButton = {
                TextButton(onClick = { 
                    showSuccessDialog = false
                    onNavigateBack()
                }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Modifier l'assurance" else "Nouvelle assurance") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrimary,
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
                    navigationIconContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = ColorPrimary.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = ColorPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Les champs marqués d'un * sont obligatoires",
                        fontSize = 14.sp,
                        color = ColorTextSecondary
                    )
                }
            }

            // Basic Information Section
            Text(
                "Informations de base",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextPrimary
            )
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; formError = null },
                label = { Text("Nom de l'assurance *") },
                leadingIcon = { Icon(Icons.Default.Badge, null) },
                isError = name.isBlank(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it; formError = null },
                label = { Text("Description *") },
                leadingIcon = { Icon(Icons.Default.Description, null) },
                isError = description.isBlank(),
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it; formError = null },
                    label = { Text("Prix (TND) *") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                    isError = priceText.toDoubleOrNull() == null,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it; formError = null },
                    label = { Text("Durée *") },
                    leadingIcon = { Icon(Icons.Default.Schedule, null) },
                    isError = duration.isBlank(),
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("1 an") },
                    singleLine = true
                )
            }
            
            OutlinedTextField(
                value = coverageText,
                onValueChange = { coverageText = it; formError = null },
                label = { Text("Couvertures (séparées par des virgules) *") },
                leadingIcon = { Icon(Icons.Default.Shield, null) },
                isError = coverageText.isBlank(),
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Annulation, Bagages, Assistance médicale") },
                minLines = 2,
                maxLines = 4
            )
            
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it; formError = null },
                label = { Text("URL de l'image") },
                leadingIcon = { Icon(Icons.Default.Image, null) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("https://exemple.com/image.jpg") },
                singleLine = true
            )

            // Active status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) ColorSuccess.copy(alpha = 0.1f) else ColorTextSecondary.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (isActive) ColorSuccess else ColorTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Statut de l'assurance",
                                fontWeight = FontWeight.Bold,
                                color = ColorTextPrimary
                            )
                            Text(
                                if (isActive) "Active - Visible par les utilisateurs" else "Inactive - Non visible",
                                fontSize = 12.sp,
                                color = ColorTextSecondary
                            )
                        }
                    }
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ColorSuccess,
                            checkedTrackColor = ColorSuccess.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            // Conditions Section
            Divider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Conditions spécifiques (optionnel)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )
                Switch(
                    checked = showConditions,
                    onCheckedChange = { showConditions = it }
                )
            }
            
            if (showConditions) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = ColorBackground
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Restrictions d'âge",
                            fontWeight = FontWeight.SemiBold,
                            color = ColorTextPrimary
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = ageMinText,
                                onValueChange = { ageMinText = it },
                                label = { Text("Âge minimum") },
                                leadingIcon = { Icon(Icons.Default.Person, null) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                placeholder = { Text("18") },
                                singleLine = true
                            )
                            
                            OutlinedTextField(
                                value = ageMaxText,
                                onValueChange = { ageMaxText = it },
                                label = { Text("Âge maximum") },
                                leadingIcon = { Icon(Icons.Default.Person, null) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                placeholder = { Text("70") },
                                singleLine = true
                            )
                        }
                        
                        OutlinedTextField(
                            value = destinationsText,
                            onValueChange = { destinationsText = it },
                            label = { Text("Destinations couvertes") },
                            leadingIcon = { Icon(Icons.Default.Public, null) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Europe, Monde entier") },
                            minLines = 2
                        )
                        
                        OutlinedTextField(
                            value = otherConditions,
                            onValueChange = { otherConditions = it },
                            label = { Text("Autres conditions") },
                            leadingIcon = { Icon(Icons.Default.Notes, null) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4
                        )
                    }
                }
            }

            // Error message
            if (formError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = ColorError.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = ColorError,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            formError!!,
                            color = ColorError,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Annuler")
                }
                
                ModernButton(
                    text = if (isEditMode) "Enregistrer" else "Créer",
                    onClick = {
                        // Validation
                        when {
                            name.isBlank() -> formError = "Le nom est obligatoire"
                            description.isBlank() -> formError = "La description est obligatoire"
                            priceText.toDoubleOrNull() == null -> formError = "Prix invalide"
                            priceText.toDoubleOrNull()!! < 0 -> formError = "Le prix doit être positif"
                            duration.isBlank() -> formError = "La durée est obligatoire"
                            coverageText.isBlank() -> formError = "Au moins une couverture est requise"
                            imageUrl.isNotBlank() && !imageUrl.startsWith("http") -> formError = "L'URL de l'image doit commencer par http ou https"
                            else -> {
                                val price = priceText.toDouble()
                                val coverage = coverageText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                
                                val conditions = if (showConditions && (ageMinText.isNotBlank() || ageMaxText.isNotBlank() || destinationsText.isNotBlank() || otherConditions.isNotBlank())) {
                                    InsuranceConditions(
                                        ageMin = ageMinText.toIntOrNull(),
                                        ageMax = ageMaxText.toIntOrNull(),
                                        destination = if (destinationsText.isNotBlank()) 
                                            destinationsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                        else null,
                                        other = otherConditions.ifBlank { null }
                                    )
                                } else null
                                
                                if (isEditMode) {
                                    val req = UpdateInsuranceRequest(
                                        name = name.trim(),
                                        description = description.trim(),
                                        price = price,
                                        duration = duration.trim(),
                                        coverage = coverage,
                                        imageUrl = imageUrl.ifBlank { null },
                                        conditions = conditions,
                                        isActive = isActive
                                    )
                                    viewModel.editInsurance(editingInsurance!!._id, req)
                                } else {
                                    val req = CreateInsuranceRequest(
                                        name = name.trim(),
                                        description = description.trim(),
                                        price = price,
                                        duration = duration.trim(),
                                        coverage = coverage,
                                        imageUrl = imageUrl.ifBlank { null },
                                        conditions = conditions,
                                        isActive = isActive
                                    )
                                    viewModel.createInsurance(req)
                                }
                                showSuccessDialog = true
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                )
            }
            
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = ColorPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
