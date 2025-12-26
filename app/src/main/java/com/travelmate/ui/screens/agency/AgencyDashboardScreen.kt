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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tableau de bord") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onPrimary
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
                onClick = onCreateInsurance,
                containerColor = ColorPrimary
            ) {
                Icon(Icons.Default.Add, "Ajouter", tint = androidx.compose.ui.graphics.Color.White)
            }
        }
    ) { paddingValues ->
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
                    color = MaterialTheme.colorScheme.onSurface
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
                        value = "${stats.estimatedRevenue.toInt()} TND",
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = onCreateInsurance) {
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
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                error ?: "Une erreur est survenue",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Aucune assurance",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Commencez par créer votre première assurance",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ModernButton(
                                text = "Créer une assurance",
                                onClick = onCreateInsurance
                            )
                        }
                    }
                }
            } else {
                items(insurances) { insurance ->
                    AgencyInsuranceCard(
                            insurance = insurance,
                            onEdit = { onEditInsurance(insurance._id) },
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
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
