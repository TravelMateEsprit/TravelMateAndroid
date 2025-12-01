package com.travelmate.ui.screens.agency

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.travelmate.ui.components.ModernButton
import com.travelmate.ui.components.ModernCard
import com.travelmate.ui.theme.*
import com.travelmate.utils.Constants
import com.travelmate.viewmodel.AgencyDashboardViewModel

enum class DashboardSection(
    val title: String,
    val icon: ImageVector,
    val color: Color
) {
    OVERVIEW("Vue d'ensemble", Icons.Default.Dashboard, ColorPrimary),
    INSURANCES("Assurances", Icons.Default.Shield, Color(0xFF2196F3)),
    CLAIMS("Réclamations", Icons.Default.Report, Color(0xFFFF9800)),
    BOOKINGS("Réservations", Icons.Default.CalendarMonth, Color(0xFF4CAF50)),
    DESTINATIONS("Destinations", Icons.Default.Flight, Color(0xFFFF9800)),
    CLIENTS("Clients", Icons.Default.People, Color(0xFF9C27B0)),
    ANALYTICS("Analytiques", Icons.Default.Analytics, Color(0xFFE91E63)),
    SETTINGS("Paramètres", Icons.Default.Settings, Color(0xFF607D8B))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyMainDashboard(
    navController: androidx.navigation.NavController,
    onNavigateToInsuranceForm: () -> Unit,
    onEditInsurance: (String) -> Unit,
    onViewSubscribers: (String, String) -> Unit,
    onLogout: () -> Unit,
    viewModel: AgencyDashboardViewModel = hiltViewModel()
) {
    var selectedSection by remember { mutableStateOf(DashboardSection.OVERVIEW) }
    var showMenu by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val insurances by viewModel.myInsurances.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadMyInsurances()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White
            ) {
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(ColorPrimary, ColorPrimary.copy(alpha = 0.8f))
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Icon(
                            Icons.Default.Dashboard,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "TravelMate",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Gestion d'agence",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Navigation Items
                DashboardSection.values().forEach { section ->
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                section.icon,
                                contentDescription = null,
                                tint = if (selectedSection == section) section.color else ColorTextSecondary
                            )
                        },
                        label = {
                            Text(
                                section.title,
                                fontWeight = if (selectedSection == section) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selected = selectedSection == section,
                        onClick = {
                            selectedSection = section
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = section.color.copy(alpha = 0.1f),
                            selectedTextColor = section.color,
                            selectedIconColor = section.color
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            Text("Tableau de bord", fontSize = 20.sp)
                            Text(
                                "Agence TravelMate",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ColorPrimary,
                        titleContentColor = Color.White
                    ),
                    actions = {
                        // Réclamations
                        IconButton(onClick = { 
                            navController.navigate(Constants.Routes.AGENCY_CLAIMS)
                        }) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = Color(0xFFFF9800),
                                        contentColor = Color.White
                                    ) {
                                        Text("", fontSize = 10.sp)
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Report,
                                    contentDescription = "Réclamations",
                                    tint = Color.White
                                )
                            }
                        }
                        
                        // Demandes d'assurance
                        IconButton(onClick = { 
                            navController.navigate(com.travelmate.utils.Constants.Routes.AGENCY_INSURANCE_REQUESTS)
                        }) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = ColorError,
                                        contentColor = Color.White
                                    ) {
                                        Text("!", fontSize = 10.sp)
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Assignment,
                                    contentDescription = "Demandes",
                                    tint = Color.White
                                )
                            }
                        }
                        
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Profil") },
                                onClick = { showMenu = false },
                                leadingIcon = { Icon(Icons.Default.Person, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Paramètres") },
                                onClick = { showMenu = false },
                                leadingIcon = { Icon(Icons.Default.Settings, null) }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Déconnexion", color = ColorError) },
                                onClick = {
                                    showMenu = false
                                    onLogout()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Logout, null, tint = ColorError)
                                }
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            // Main Content
            AnimatedContent(
                targetState = selectedSection,
                transitionSpec = {
                    (fadeIn() + slideInHorizontally()).togetherWith(
                        fadeOut() + slideOutHorizontally()
                    )
                },
                label = "section_transition",
                modifier = Modifier.padding(paddingValues)
            ) { section ->
                when (section) {
                    DashboardSection.OVERVIEW -> OverviewSection(
                        stats = stats,
                        insurances = insurances,
                        onNavigateToInsurances = { selectedSection = DashboardSection.INSURANCES },
                        onNavigateToInsuranceForm = onNavigateToInsuranceForm,
                        onSectionClick = { selectedSection = it },
                        navController = navController
                    )
                    DashboardSection.INSURANCES -> InsurancesSection(
                        insurances = insurances,
                        isLoading = isLoading,
                        onNavigateToInsuranceForm = onNavigateToInsuranceForm,
                        onEditInsurance = onEditInsurance,
                        onViewSubscribers = onViewSubscribers,
                        viewModel = viewModel
                    )
                    DashboardSection.CLAIMS -> {
                        // Navigate to claims screen
                        LaunchedEffect(Unit) {
                            navController.navigate(Constants.Routes.AGENCY_CLAIMS)
                            selectedSection = DashboardSection.OVERVIEW
                        }
                        Box(modifier = Modifier.fillMaxSize())
                    }
                    else -> ComingSoonSection(section)
                }
            }
        }
    }
}

@Composable
fun OverviewSection(
    stats: com.travelmate.viewmodel.AgencyStats,
    insurances: List<com.travelmate.data.models.Insurance>,
    onNavigateToInsurances: () -> Unit,
    onNavigateToInsuranceForm: () -> Unit,
    onSectionClick: (DashboardSection) -> Unit,
    navController: androidx.navigation.NavController
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(ColorPrimary, ColorPrimary.copy(alpha = 0.7f))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            "Bienvenue sur votre tableau de bord",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Gérez votre agence efficacement",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // Quick Stats
        item {
            Text(
                "Statistiques rapides",
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
                QuickStatCard(
                    icon = Icons.Default.Shield,
                    value = stats.totalInsurances.toString(),
                    label = "Assurances",
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f),
                    onClick = { onSectionClick(DashboardSection.INSURANCES) }
                )
                QuickStatCard(
                    icon = Icons.Default.People,
                    value = stats.totalSubscribers.toString(),
                    label = "Inscrits",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickStatCard(
                    icon = Icons.Default.CheckCircle,
                    value = stats.activeInsurances.toString(),
                    label = "Actives",
                    color = ColorSuccess,
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    icon = Icons.Default.Report,
                    value = "0",
                    label = "Réclamations",
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Constants.Routes.AGENCY_CLAIMS) }
                )
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickStatCard(
                    icon = Icons.Default.AttachMoney,
                    value = "${stats.estimatedRevenue.toInt()} TND",
                    label = "Revenu",
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    icon = Icons.Default.TrendingUp,
                    value = "+12%",
                    label = "Croissance",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Actions
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Actions rapides",
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
                QuickActionCard(
                    icon = Icons.Default.Add,
                    title = "Nouvelle assurance",
                    subtitle = "Créer une offre",
                    color = ColorPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToInsuranceForm
                )
                QuickActionCard(
                    icon = Icons.Default.CalendarMonth,
                    title = "Réservations",
                    subtitle = "Voir les réservations",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f),
                    onClick = { onSectionClick(DashboardSection.BOOKINGS) }
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Default.Report,
                    title = "Réclamations",
                    subtitle = "Gérer les demandes",
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Constants.Routes.AGENCY_CLAIMS) }
                )
                QuickActionCard(
                    icon = Icons.Default.People,
                    title = "Clients",
                    subtitle = "Voir les clients",
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f),
                    onClick = { onSectionClick(DashboardSection.CLIENTS) }
                )
            }
        }

        // Recent Insurances
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Assurances récentes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )
                TextButton(onClick = onNavigateToInsurances) {
                    Text("Voir tout")
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        items(insurances.take(3)) { insurance ->
            CompactInsuranceCard(insurance)
        }
        
        if (insurances.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = ColorTextSecondary.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = ColorTextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Aucune assurance",
                            fontWeight = FontWeight.Bold,
                            color = ColorTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Créez votre première assurance",
                            fontSize = 14.sp,
                            color = ColorTextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ModernButton(
                            text = "Créer une assurance",
                            onClick = onNavigateToInsuranceForm
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsurancesSection(
    insurances: List<com.travelmate.data.models.Insurance>,
    isLoading: Boolean,
    onNavigateToInsuranceForm: () -> Unit,
    onEditInsurance: (String) -> Unit,
    onViewSubscribers: (String, String) -> Unit,
    viewModel: AgencyDashboardViewModel
) {
    val stats by viewModel.stats.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // États pour la recherche et les filtres
    var searchTerm by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedPriceRange by remember { mutableStateOf(0f..1000f) }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    
    // Filtrer les assurances
    val filteredInsurances = remember(insurances, searchTerm, selectedPriceRange, selectedStatus) {
        insurances.filter { insurance ->
            val destinations = insurance.conditions?.destination?.joinToString(" ") ?: ""
            
            val matchesSearch = searchTerm.isEmpty() || 
                insurance.name.contains(searchTerm, ignoreCase = true) ||
                insurance.description.contains(searchTerm, ignoreCase = true) ||
                destinations.contains(searchTerm, ignoreCase = true)
            
            val matchesPrice = insurance.price in selectedPriceRange
            
            val matchesStatus = selectedStatus == null || 
                (selectedStatus == "active" && insurance.isActive) ||
                (selectedStatus == "inactive" && !insurance.isActive)
            
            matchesSearch && matchesPrice && matchesStatus
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Barre de recherche
        item {
            OutlinedTextField(
                value = searchTerm,
                onValueChange = { searchTerm = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Rechercher une assurance...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Rechercher")
                },
                trailingIcon = {
                    Row {
                        if (searchTerm.isNotEmpty()) {
                            IconButton(onClick = { searchTerm = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Effacer")
                            }
                        }
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filtres",
                                tint = if (showFilters) ColorPrimary else ColorTextSecondary
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ColorPrimary,
                    unfocusedBorderColor = ColorTextSecondary.copy(alpha = 0.3f)
                )
            )
        }
        
        // Panneau de filtres
        if (showFilters) {
            item {
                ModernCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp
                ) {
                    Text(
                        "Filtres",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Filtre de prix
                    Text(
                        "Fourchette de prix: ${selectedPriceRange.start.toInt()} TND - ${selectedPriceRange.endInclusive.toInt()} TND",
                        fontSize = 14.sp,
                        color = ColorTextSecondary
                    )
                    RangeSlider(
                        value = selectedPriceRange,
                        onValueChange = { selectedPriceRange = it },
                        valueRange = 0f..2000f,
                        steps = 19,
                        colors = SliderDefaults.colors(
                            thumbColor = ColorPrimary,
                            activeTrackColor = ColorPrimary
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Filtre de statut
                    Text(
                        "Statut",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedStatus == null,
                            onClick = { selectedStatus = null },
                            label = { Text("Tous") }
                        )
                        FilterChip(
                            selected = selectedStatus == "active",
                            onClick = { selectedStatus = "active" },
                            label = { Text("Actif") }
                        )
                        FilterChip(
                            selected = selectedStatus == "inactive",
                            onClick = { selectedStatus = "inactive" },
                            label = { Text("Inactif") }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Bouton réinitialiser les filtres
                    OutlinedButton(
                        onClick = {
                            selectedPriceRange = 0f..1000f
                            selectedStatus = null
                            searchTerm = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Réinitialiser les filtres")
                    }
                }
            }
        }
        
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
                QuickStatCard(
                    icon = Icons.Default.Shield,
                    value = stats.totalInsurances.toString(),
                    label = "Total Assurances",
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    icon = Icons.Default.People,
                    value = stats.totalSubscribers.toString(),
                    label = "Total Inscrits",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickStatCard(
                    icon = Icons.Default.CheckCircle,
                    value = stats.activeInsurances.toString(),
                    label = "Actives",
                    color = ColorSuccess,
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    icon = Icons.Default.AttachMoney,
                    value = "${stats.estimatedRevenue.toInt()} TND",
                    label = "Revenu",
                    color = Color(0xFFFF9800),
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
                TextButton(onClick = onNavigateToInsuranceForm) {
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
                            Icons.Default.Shield,
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
                            onClick = onNavigateToInsuranceForm
                        )
                    }
                }
            }
        } else if (filteredInsurances.isEmpty()) {
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
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = ColorTextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Aucun résultat",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Aucune assurance ne correspond à vos critères de recherche",
                            fontSize = 14.sp,
                            color = ColorTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                searchTerm = ""
                                selectedPriceRange = 0f..1000f
                                selectedStatus = null
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Réinitialiser la recherche")
                        }
                    }
                }
            }
        } else {
            items(filteredInsurances) { insurance ->
                AgencyInsuranceCard(
                    insurance = insurance,
                    onEdit = { onEditInsurance(insurance._id) },
                    onDelete = { viewModel.deleteInsurance(insurance._id) },
                    onToggleActive = { viewModel.toggleInsuranceActive(insurance._id, !insurance.isActive) },
                    onViewSubscribers = { onViewSubscribers(insurance._id, insurance.name) }
                )
            }
        }
    }
}

@Composable
fun ComingSoonSection(section: DashboardSection) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                section.icon,
                contentDescription = null,
                tint = section.color,
                modifier = Modifier.size(80.dp)
            )
            Text(
                section.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextPrimary
            )
            Text(
                "Cette section sera bientôt disponible",
                fontSize = 14.sp,
                color = ColorTextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = section.color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "🚧 En développement",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = section.color,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun QuickStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextPrimary
            )
            Text(
                label,
                fontSize = 12.sp,
                color = ColorTextSecondary
            )
        }
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = ColorBackground
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary,
                    fontSize = 14.sp
                )
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = ColorTextSecondary
                )
            }
        }
    }
}

@Composable
fun CompactInsuranceCard(insurance: com.travelmate.data.models.Insurance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (insurance.isActive) ColorPrimary.copy(alpha = 0.2f)
                        else ColorTextSecondary.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Shield,
                    contentDescription = null,
                    tint = if (insurance.isActive) ColorPrimary else ColorTextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    insurance.name,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary,
                    fontSize = 14.sp
                )
                Text(
                    "${insurance.price.toInt()} TND • ${insurance.subscribers.size} inscrits",
                    fontSize = 12.sp,
                    color = ColorTextSecondary
                )
            }
            Surface(
                color = if (insurance.isActive) ColorSuccess.copy(alpha = 0.2f)
                       else ColorTextSecondary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    if (insurance.isActive) "Active" else "Inactive",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (insurance.isActive) ColorSuccess else ColorTextSecondary
                )
            }
        }
    }
}
