package com.travelmate.ui.screens.agency

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.travelmate.ui.components.ModernButton
import com.travelmate.ui.components.ModernCard
import com.travelmate.ui.theme.*
import com.travelmate.data.models.Insurance
import com.travelmate.viewmodel.AgencyDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyDashboardScreen(
    onCreateInsurance: () -> Unit = {},
    onEditInsurance: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: AgencyDashboardViewModel = hiltViewModel()
) {
    val insurances by viewModel.myInsurances.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showMenu by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        android.util.Log.d("AgencyDashboardScreen", "Screen launched, loading insurances...")
        viewModel.loadMyInsurances()
    }
    
    // Log when data changes
    LaunchedEffect(insurances) {
        android.util.Log.d("AgencyDashboardScreen", "Insurances updated: ${insurances.size} items")
    }
    
    LaunchedEffect(error) {
        error?.let {
            android.util.Log.e("AgencyDashboardScreen", "Error: $it")
        }
    }
    
    // Create / Edit dialog state
    var showFormDialog by remember { mutableStateOf(false) }
    var editingInsurance by remember { mutableStateOf<Insurance?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tableau de bord") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrimary,
                    titleContentColor = androidx.compose.ui.graphics.Color.White
                ),
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Profil") },
                            onClick = {
                                showMenu = false
                                // TODO: Navigate to profile
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Paramètres") },
                            onClick = {
                                showMenu = false
                                // TODO: Navigate to settings
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Settings, contentDescription = null)
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Déconnexion", color = ColorError) },
                            onClick = {
                                showMenu = false
                                onLogout()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Logout,
                                    contentDescription = null,
                                    tint = ColorError
                                )
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showFormDialog = true; editingInsurance = null },
                containerColor = ColorPrimary
            ) {
                Icon(Icons.Default.Add, "Ajouter", tint = androidx.compose.ui.graphics.Color.White)
            }
        }
    ) { paddingValues ->


        if (showFormDialog) {
            // form state
            var name by remember { mutableStateOf(editingInsurance?.name ?: "") }
            var description by remember { mutableStateOf(editingInsurance?.description ?: "") }
            var priceText by remember { mutableStateOf(editingInsurance?.price?.toString() ?: "") }
            var duration by remember { mutableStateOf(editingInsurance?.duration ?: "") }
            var coverageText by remember { mutableStateOf(editingInsurance?.coverage?.joinToString(", ") ?: "") }

            AlertDialog(
                onDismissRequest = { showFormDialog = false; editingInsurance = null },
                title = { Text(if (editingInsurance == null) "Créer une assurance" else "Modifier l'assurance") },
                text = {
                    Column {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom") })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Prix") })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Durée (ex: 1 mois)") })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = coverageText, onValueChange = { coverageText = it }, label = { Text("Couvertures (séparées par ,)") })
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        // basic validation
                        val price = priceText.toDoubleOrNull() ?: 0.0
                        val coverage = coverageText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        if (editingInsurance == null) {
                            val req = com.travelmate.data.models.CreateInsuranceRequest(
                                name = name,
                                description = description,
                                price = price,
                                duration = duration,
                                coverage = coverage,
                                imageUrl = null,
                                conditions = null,
                                isActive = true
                            )
                            viewModel.createInsurance(req)
                        } else {
                            val req = com.travelmate.data.models.UpdateInsuranceRequest(
                                name = name,
                                description = description,
                                price = price,
                                duration = duration,
                                coverage = coverage,
                                imageUrl = null,
                                conditions = null,
                                isActive = editingInsurance?.isActive
                            )
                            viewModel.editInsurance(editingInsurance!!._id, req)
                        }
                        showFormDialog = false
                        editingInsurance = null
                    }) {
                        Text("Valider")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFormDialog = false; editingInsurance = null }) {
                        Text("Annuler")
                    }
                }
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Grid
            item {
                Text(
                    "Statistiques",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.Default.Inventory,
                        value = stats.totalInsurances.toString(),
                        label = "Total Assurances",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = Icons.Default.People,
                        value = stats.totalSubscribers.toString(),
                        label = "Total Inscrits",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.Default.CheckCircle,
                        value = stats.activeInsurances.toString(),
                        label = "Actives",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = Icons.Default.AttachMoney,
                        value = "${stats.estimatedRevenue.toInt()}€",
                        label = "Revenu Estimé",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Insurances List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Mes Assurances",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                    TextButton(onClick = { showFormDialog = true; editingInsurance = null }) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ajouter")
                    }
                }
            }
            
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ColorPrimary)
                    }
                }
            } else if (error != null) {
                item {
                    ModernCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = ColorError,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Erreur de chargement",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorTextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                error ?: "Une erreur est survenue",
                                fontSize = 14.sp,
                                color = ColorTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ModernButton(
                                text = "Réessayer",
                                onClick = { viewModel.loadMyInsurances() }
                            )
                        }
                    }
                }
            } else if (insurances.isEmpty()) {
                item {
                    ModernCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Inventory,
                                contentDescription = null,
                                tint = ColorTextSecondary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Aucune assurance",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorTextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Commencez par créer votre première assurance",
                                fontSize = 14.sp,
                                color = ColorTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ModernButton(
                                text = "Créer une assurance",
                                onClick = { showFormDialog = true; editingInsurance = null }
                            )
                        }
                    }
                }
            } else {
                items(insurances) { insurance ->
                    AgencyInsuranceCard(
                            insurance = insurance,
                            onEdit = {
                                editingInsurance = insurance
                                showFormDialog = true
                            },
                            onDelete = { viewModel.deleteInsurance(insurance._id) },
                            onToggleActive = { viewModel.toggleInsuranceActive(insurance._id, !insurance.isActive) },
                            onViewSubscribers = { /* TODO */ }
                        )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier,
        cornerRadius = 16.dp,
        elevation = 2.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = ColorPrimary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextPrimary
            )
            Text(
                label,
                fontSize = 12.sp,
                color = ColorTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
