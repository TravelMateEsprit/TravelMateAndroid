package com.travelmate.ui.screens.user

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.travelmate.ui.components.ModernCard
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.InsurancesUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsurancesUserScreen(
    viewModel: InsurancesUserViewModel = hiltViewModel()
) {
    val insurances by viewModel.insurances.collectAsState()
    val mySubscriptions by viewModel.mySubscriptions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Toutes les assurances", "Mes inscriptions")
    
    // États pour la recherche et les filtres
    var searchTerm by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedPriceRange by remember { mutableStateOf(0f..1000f) }
    var selectedDestination by remember { mutableStateOf<String?>(null) }
    var selectedDuration by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.loadAllInsurances()
        viewModel.loadMySubscriptions()
    }
    
    // Show error if any
    error?.let { errorMsg ->
        LaunchedEffect(errorMsg) {
            android.util.Log.e("InsurancesUserScreen", "Error: $errorMsg")
            snackbarHostState.showSnackbar(
                message = errorMsg,
                duration = SnackbarDuration.Long
            )
        }
    }
    
    // Log insurances state
    LaunchedEffect(insurances.size, mySubscriptions.size) {
        android.util.Log.d("InsurancesUserScreen", "Insurances: ${insurances.size}, Subscriptions: ${mySubscriptions.size}")
    }
    
    // Filtrer les assurances en fonction de la recherche et des filtres
    val filteredInsurances = remember(insurances, searchTerm, selectedPriceRange, selectedDestination, selectedDuration) {
        insurances.filter { insurance ->
            val destinations = insurance.conditions?.destination?.joinToString(" ") ?: ""
            
            val matchesSearch = searchTerm.isEmpty() || 
                insurance.name.contains(searchTerm, ignoreCase = true) ||
                insurance.description.contains(searchTerm, ignoreCase = true) ||
                destinations.contains(searchTerm, ignoreCase = true)
            
            val matchesPrice = insurance.price in selectedPriceRange
            
            val destinationValue = selectedDestination
            val matchesDestination = destinationValue == null || 
                destinations.contains(destinationValue, ignoreCase = true)
            
            val durationValue = selectedDuration
            val matchesDuration = durationValue == null || 
                insurance.duration.contains(durationValue, ignoreCase = true)
            
            matchesSearch && matchesPrice && matchesDestination && matchesDuration
        }
    }
    
    // Filtrer les inscriptions
    val filteredSubscriptions = remember(mySubscriptions, searchTerm) {
        mySubscriptions.filter { insurance ->
            val destinations = insurance.conditions?.destination?.joinToString(" ") ?: ""
            
            searchTerm.isEmpty() || 
                insurance.name.contains(searchTerm, ignoreCase = true) ||
                insurance.description.contains(searchTerm, ignoreCase = true) ||
                destinations.contains(searchTerm, ignoreCase = true)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Custom TopBar without system padding
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ColorPrimary,
                shadowElevation = 4.dp
            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Assurances",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = { 
                    viewModel.loadAllInsurances()
                    viewModel.loadMySubscriptions()
                }) {
                    Icon(Icons.Default.Refresh, "Actualiser", tint = Color.White)
                }
                IconButton(onClick = { /* Notifications */ }) {
                    Icon(Icons.Default.Notifications, "Notifications", tint = Color.White)
                }
            }
        }
        
        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(ColorPrimary, ColorSecondary),
                                start = Offset(0f, 0f),
                                end = Offset(1000f, 800f)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Assurances voyage",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Voyagez l'esprit tranquille",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
            
            // Barre de recherche
            item {
                // Calculer le nombre de filtres actifs
                val activeFiltersCount = remember(selectedPriceRange, selectedDestination, selectedDuration) {
                    var count = 0
                    if (selectedPriceRange != 0f..1000f) count++
                    if (selectedDestination != null) count++
                    if (selectedDuration != null) count++
                    count
                }
                
                OutlinedTextField(
                    value = searchTerm,
                    onValueChange = { searchTerm = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
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
                            Box {
                                IconButton(onClick = { showFilters = !showFilters }) {
                                    Icon(
                                        Icons.Default.FilterList,
                                        contentDescription = "Filtres",
                                        tint = if (showFilters || activeFiltersCount > 0) ColorPrimary else ColorTextSecondary
                                    )
                                }
                                // Badge pour le nombre de filtres actifs
                                if (activeFiltersCount > 0) {
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(16.dp)
                                            .offset(x = (-4).dp, y = 4.dp),
                                        shape = CircleShape,
                                        color = ColorError
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = activeFiltersCount.toString(),
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
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
            
            // Panneau de filtres (visible seulement pour "Toutes les assurances")
            if (showFilters && selectedTabIndex == 0) {
                item {
                    ModernCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
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
                            "Budget: ${selectedPriceRange.start.toInt()}€ - ${selectedPriceRange.endInclusive.toInt()}€",
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
                        
                        // Filtre de destination
                        Text(
                            "Destination",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val destinations = listOf("Europe", "Asie", "Amérique", "Afrique", "Océanie")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedDestination == null,
                                onClick = { selectedDestination = null },
                                label = { Text("Tous", fontSize = 12.sp) }
                            )
                            destinations.take(3).forEach { dest ->
                                FilterChip(
                                    selected = selectedDestination == dest,
                                    onClick = { selectedDestination = if (selectedDestination == dest) null else dest },
                                    label = { Text(dest, fontSize = 12.sp) }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Filtre de durée
                        Text(
                            "Durée",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val durations = listOf("1 semaine", "2 semaines", "1 mois", "3 mois", "1 an")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedDuration == null,
                                onClick = { selectedDuration = null },
                                label = { Text("Tous", fontSize = 12.sp) }
                            )
                            durations.take(2).forEach { dur ->
                                FilterChip(
                                    selected = selectedDuration == dur,
                                    onClick = { selectedDuration = if (selectedDuration == dur) null else dur },
                                    label = { Text(dur, fontSize = 12.sp) }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Bouton réinitialiser les filtres
                        OutlinedButton(
                            onClick = {
                                selectedPriceRange = 0f..1000f
                                selectedDestination = null
                                selectedDuration = null
                                searchTerm = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Réinitialiser les filtres")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // Benefits Grid
            item {
                Text(
                    "Pourquoi souscrire ?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary,
                    modifier = Modifier.padding(24.dp, 24.dp, 24.dp, 12.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BenefitCard(
                        icon = Icons.Default.Flight,
                        title = "Annulation\nde vol",
                        modifier = Modifier.weight(1f)
                    )
                    BenefitCard(
                        icon = Icons.Default.LocalHospital,
                        title = "Frais\nmédicaux",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BenefitCard(
                        icon = Icons.Default.Luggage,
                        title = "Perte de\nbagages",
                        modifier = Modifier.weight(1f)
                    )
                    BenefitCard(
                        icon = Icons.Default.Support,
                        title = "Assistance\n24/7",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.padding(vertical = 16.dp),
                    containerColor = Color.Transparent,
                    contentColor = ColorPrimary
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, fontSize = 14.sp) }
                        )
                    }
                }
            }
            
            // Insurance List
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp, 0.dp, 24.dp, 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (selectedTabIndex == 0) "Nos produits" else "Mes inscriptions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                    
                    // Compteur de résultats
                    if (selectedTabIndex == 0 && (searchTerm.isNotEmpty() || selectedDestination != null || selectedDuration != null || selectedPriceRange != 0f..1000f)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ColorPrimary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                "${filteredInsurances.size} résultat${if (filteredInsurances.size > 1) "s" else ""}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = ColorPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
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
            } else {
                val displayList = if (selectedTabIndex == 0) filteredInsurances else filteredSubscriptions
                
                if (displayList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    if (searchTerm.isNotEmpty() || selectedDestination != null || selectedDuration != null || selectedPriceRange != 0f..1000f)
                                        Icons.Default.Search
                                    else Icons.Default.Info,
                                    contentDescription = null,
                                    tint = ColorTextSecondary,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    if (searchTerm.isNotEmpty() || selectedDestination != null || selectedDuration != null || selectedPriceRange != 0f..1000f)
                                        "Aucun résultat"
                                    else if (selectedTabIndex == 0) "Aucune assurance disponible"
                                    else "Aucune inscription",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ColorTextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    if (searchTerm.isNotEmpty() || selectedDestination != null || selectedDuration != null || selectedPriceRange != 0f..1000f)
                                        "Aucune assurance ne correspond à vos critères"
                                    else if (selectedTabIndex == 0) "Les assurances apparaîtront ici une fois créées par les agences"
                                    else "Vous n'êtes inscrit à aucune assurance pour le moment",
                                    fontSize = 13.sp,
                                    color = ColorTextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                
                                if (searchTerm.isNotEmpty() || selectedDestination != null || selectedDuration != null || selectedPriceRange != 0f..1000f) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    OutlinedButton(
                                        onClick = {
                                            searchTerm = ""
                                            selectedPriceRange = 0f..1000f
                                            selectedDestination = null
                                            selectedDuration = null
                                        }
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Réinitialiser")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(displayList) { insurance ->
                        InsuranceUserCard(
                            insurance = insurance,
                            onSubscribe = { viewModel.subscribeToInsurance(it) },
                            onUnsubscribe = { viewModel.unsubscribeFromInsurance(it) },
                            isInMySubscriptionsTab = selectedTabIndex == 1,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
        }
        
        // Snackbar for errors
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun BenefitCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.height(100.dp),
        cornerRadius = 16.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = ColorPrimary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                fontSize = 12.sp,
                color = ColorTextPrimary,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}