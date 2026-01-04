package com.travelmate.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.travelmate.viewmodel.SearchInsuranceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchInsuranceScreen(
    navController: androidx.navigation.NavController,
    viewModel: SearchInsuranceViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchTerm by viewModel.searchTerm.collectAsState()
    val minPrice by viewModel.minPrice.collectAsState()
    val maxPrice by viewModel.maxPrice.collectAsState()
    val selectedDuration by viewModel.selectedDuration.collectAsState()
    val selectedCoverage by viewModel.selectedCoverage.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val totalResults by viewModel.totalResults.collectAsState()
    
    // AI Recommendations state
    val recommendations by viewModel.recommendations.collectAsState()
    val isLoadingRecommendations by viewModel.isLoadingRecommendations.collectAsState()
    val recommendationsError by viewModel.recommendationsError.collectAsState()
    
    var showFilters by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    
    LaunchedEffect(Unit) {
        viewModel.searchInsurances()
        // Recharger les recommandations au cas où le profil a été complété
        viewModel.loadRecommendations()
        android.util.Log.d("SearchInsuranceScreen", "Recommandations rechargées")
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rechercher une assurance") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            imageVector = if (showFilters) Icons.Default.FilterList else Icons.Default.FilterList,
                            contentDescription = "Filtres",
                            tint = if (showFilters) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barre de recherche
            SearchBar(
                searchTerm = searchTerm,
                onSearchTermChange = { viewModel.updateSearchTerm(it) },
                onSearch = { viewModel.searchInsurances(0) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            
            // AI Recommendations Section
            if (recommendations.isNotEmpty() && !isLoadingRecommendations) {
                AIRecommendationsSection(
                    recommendations = recommendations,
                    onInsuranceClick = { insuranceId ->
                        navController.navigate("${com.travelmate.utils.Constants.Routes.CREATE_INSURANCE_REQUEST}/$insuranceId")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (isLoadingRecommendations) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Chargement des recommandations IA...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Panneau de filtres
            if (showFilters) {
                FiltersPanel(
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    selectedDuration = selectedDuration,
                    selectedCoverage = selectedCoverage,
                    selectedCity = selectedCity,
                    onPriceRangeChange = { min, max ->
                        viewModel.updatePriceRange(min, max)
                    },
                    onDurationChange = { viewModel.updateDuration(it) },
                    onCoverageChange = { viewModel.updateCoverage(it) },
                    onCityChange = { viewModel.updateCity(it) },
                    onApplyFilters = {
                        viewModel.searchInsurances(0)
                        showFilters = false
                    },
                    onClearFilters = {
                        viewModel.clearFilters()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
            
            // Nombre de résultats
            if (!isLoading && searchResults.isNotEmpty()) {
                Text(
                    text = "$totalResults résultat(s) trouvé(s)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
                )
            }
            
            // Liste des résultats
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading && searchResults.isEmpty() -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    error != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = error ?: "Une erreur est survenue",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.searchInsurances(0) }) {
                                Text("Réessayer")
                            }
                        }
                    }
                    searchResults.isEmpty() -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Aucune assurance trouvée",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                top = 16.dp,
                                end = 16.dp,
                                bottom = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(searchResults) { insurance ->
                                InsuranceUserCard(
                                    insurance = insurance,
                                    onCreateRequest = { 
                                        navController.navigate("${com.travelmate.utils.Constants.Routes.CREATE_INSURANCE_REQUEST}/${insurance._id}")
                                    },
                                    onUnsubscribe = { /* TODO: Implémenter la désinscription */ },
                                    isInMySubscriptionsTab = false
                                )
                            }
                            
                            // Bouton charger plus
                            if (searchResults.size < totalResults) {
                                item {
                                    Button(
                                        onClick = { viewModel.loadMore() },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !isLoading
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        } else {
                                            Text("Charger plus")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    searchTerm: String,
    onSearchTermChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchTerm,
        onValueChange = onSearchTermChange,
        modifier = modifier,
        placeholder = { Text("Rechercher une assurance...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Recherche")
        },
        trailingIcon = {
            if (searchTerm.isNotEmpty()) {
                IconButton(onClick = { onSearchTermChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Effacer")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Gray
        )
    )
}

@Composable
fun FiltersPanel(
    minPrice: Double?,
    maxPrice: Double?,
    selectedDuration: String?,
    selectedCoverage: String?,
    selectedCity: String?,
    onPriceRangeChange: (Double?, Double?) -> Unit,
    onDurationChange: (String?) -> Unit,
    onCoverageChange: (String?) -> Unit,
    onCityChange: (String?) -> Unit,
    onApplyFilters: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    var minPriceText by remember { mutableStateOf(minPrice?.toString() ?: "") }
    var maxPriceText by remember { mutableStateOf(maxPrice?.toString() ?: "") }
    var cityText by remember { mutableStateOf(selectedCity ?: "") }
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Filtres",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Prix
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = minPriceText,
                    onValueChange = { minPriceText = it },
                    label = { Text("Prix min") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = maxPriceText,
                    onValueChange = { maxPriceText = it },
                    label = { Text("Prix max") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            
            // Durée
            DurationFilter(
                selectedDuration = selectedDuration,
                onDurationChange = onDurationChange
            )
            
            // Couverture
            CoverageFilter(
                selectedCoverage = selectedCoverage,
                onCoverageChange = onCoverageChange
            )
            
            // Ville
            OutlinedTextField(
                value = cityText,
                onValueChange = { cityText = it },
                label = { Text("Ville de l'agence") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Boutons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClearFilters,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Réinitialiser")
                }
                Button(
                    onClick = {
                        onPriceRangeChange(
                            minPriceText.toDoubleOrNull(),
                            maxPriceText.toDoubleOrNull()
                        )
                        onCityChange(cityText.takeIf { it.isNotEmpty() })
                        onApplyFilters()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Appliquer")
                }
            }
        }
    }
}

@Composable
fun AIRecommendationsSection(
    recommendations: List<com.travelmate.data.models.InsuranceRecommendation>,
    onInsuranceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "✨ Recommandations IA pour vous",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                "Basées sur votre profil de voyage",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(recommendations) { recommendation ->
                    RecommendationCard(
                        recommendation = recommendation,
                        onClick = { onInsuranceClick(recommendation.insuranceId) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationCard(
    recommendation: com.travelmate.data.models.InsuranceRecommendation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = modifier.width(300.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Badge "Recommandé pour vous"
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Recommandé pour vous",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    // Match Score
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            "${recommendation.matchScore}/10",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Insurance Name
            Text(
                recommendation.insuranceName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // "Pourquoi cette assurance" Section
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "💡 Pourquoi cette assurance ?",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Masquer" else "Afficher",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                androidx.compose.animation.AnimatedVisibility(visible = expanded) {
                    Column(
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            recommendation.reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        
                        if (recommendation.matchFactors.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Points clés:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            recommendation.matchFactors.take(3).forEach { factor ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        "• ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        factor,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DurationFilter(
    selectedDuration: String?,
    onDurationChange: (String?) -> Unit
) {
    val durations = listOf("1 mois", "3 mois", "6 mois", "1 an", "voyage unique")
    
    Column {
        Text("Durée", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            durations.take(3).forEach { duration ->
                FilterChip(
                    selected = selectedDuration == duration,
                    onClick = {
                        onDurationChange(if (selectedDuration == duration) null else duration)
                    },
                    label = { Text(duration, style = MaterialTheme.typography.bodySmall) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverageFilter(
    selectedCoverage: String?,
    onCoverageChange: (String?) -> Unit
) {
    val coverages = listOf("Annulation", "Bagages", "Médicale", "Rapatriement")
    
    Column {
        Text("Type de couverture", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            coverages.take(3).forEach { coverage ->
                FilterChip(
                    selected = selectedCoverage == coverage,
                    onClick = {
                        onCoverageChange(if (selectedCoverage == coverage) null else coverage)
                    },
                    label = { Text(coverage, style = MaterialTheme.typography.bodySmall) }
                )
            }
        }
    }
}
