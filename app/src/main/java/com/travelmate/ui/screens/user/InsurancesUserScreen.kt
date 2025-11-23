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
import androidx.navigation.NavController
import com.travelmate.ui.components.ModernCard
import com.travelmate.ui.theme.*
import com.travelmate.utils.Constants
import com.travelmate.viewmodel.InsurancesUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsurancesUserScreen(
    navController: NavController,
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
    var selectedPriceRange by remember { mutableStateOf(0f..2000f) }
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
            
            val matchesDestination = selectedDestination == null || 
                destinations.contains(selectedDestination!!, ignoreCase = true)
            
            val matchesDuration = selectedDuration == null || 
                insurance.duration.contains(selectedDuration!!, ignoreCase = true)
            
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
        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header artistique avec gradient diagonal
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    ColorPrimary,
                                    ColorPrimary.copy(alpha = 0.85f),
                                    ColorPrimary.copy(alpha = 0.7f)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(1000f, 1000f)
                            )
                        )
                ) {
                    // Pattern décoratif en arrière-plan
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .offset(x = (-50).dp, y = (-50).dp)
                            .background(
                                Color.White.copy(alpha = 0.05f),
                                shape = CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 50.dp, y = (-30).dp)
                            .background(
                                Color.White.copy(alpha = 0.08f),
                                shape = CircleShape
                            )
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Découvrez",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    "Nos Assurances",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(48.dp),
                                onClick = { navController.navigate(Constants.Routes.MY_INSURANCE_REQUESTS) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Assignment,
                                        "Mes demandes",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            InfoChip(
                                icon = Icons.Default.Security,
                                text = "${insurances.size} produits"
                            )
                            InfoChip(
                                icon = Icons.Default.CheckCircle,
                                text = "${mySubscriptions.size} inscriptions"
                            )
                        }
                    }
                }
            }
            
            // Barre de recherche moderne avec filtres
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .offset(y = (-30).dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchTerm,
                                onValueChange = { searchTerm = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { 
                                    Text(
                                        "Rechercher...",
                                        color = ColorTextSecondary.copy(alpha = 0.6f)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Rechercher",
                                        tint = ColorPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (searchTerm.isNotEmpty()) {
                                        IconButton(onClick = { searchTerm = "" }) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Effacer",
                                                tint = ColorTextSecondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    cursorColor = ColorPrimary,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                            
                            // Bouton filtres avec badge
                            val activeFiltersCount = listOf(
                                selectedPriceRange != 0f..2000f,
                                selectedDestination != null,
                                selectedDuration != null
                            ).count { it }
                            
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (showFilters) ColorPrimary else ColorPrimary.copy(alpha = 0.1f),
                                onClick = { showFilters = !showFilters },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        Icons.Default.FilterList,
                                        contentDescription = "Filtres",
                                        tint = if (showFilters) Color.White else ColorPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    if (activeFiltersCount > 0) {
                                        Surface(
                                            shape = CircleShape,
                                            color = ColorSuccess,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .align(Alignment.TopEnd)
                                                .offset(x = 4.dp, y = (-4).dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    "$activeFiltersCount",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Panneau de filtres moderne avec animation
            if (showFilters && selectedTabIndex == 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .animateContentSize(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Filtres",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorTextPrimary
                                )
                                TextButton(
                                    onClick = {
                                        selectedPriceRange = 0f..2000f
                                        selectedDestination = null
                                        selectedDuration = null
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = ColorPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Réinitialiser",
                                        fontSize = 13.sp,
                                        color = ColorPrimary
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Filtre de prix avec design moderne
                            Text(
                                "Budget",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ColorTextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${selectedPriceRange.start.toInt()}€",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ColorPrimary
                                )
                                Text(
                                    "${selectedPriceRange.endInclusive.toInt()}€",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ColorPrimary
                                )
                            }
                            
                            RangeSlider(
                                value = selectedPriceRange,
                                onValueChange = { selectedPriceRange = it },
                                valueRange = 0f..2000f,
                                steps = 39,
                                colors = SliderDefaults.colors(
                                    thumbColor = ColorPrimary,
                                    activeTrackColor = ColorPrimary,
                                    inactiveTrackColor = ColorPrimary.copy(alpha = 0.2f)
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Filtre de destination avec chips modernes
                            Text(
                                "Destination",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ColorTextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            val destinations = listOf("Europe", "Asie", "Amérique", "Afrique", "Océanie")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedDestination == null,
                                    onClick = { selectedDestination = null },
                                    label = { 
                                        Text(
                                            "Tous",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    leadingIcon = if (selectedDestination == null) {
                                        {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ColorPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                                destinations.take(3).forEach { dest ->
                                    FilterChip(
                                        selected = selectedDestination == dest,
                                        onClick = { selectedDestination = if (selectedDestination == dest) null else dest },
                                        label = { 
                                            Text(
                                                dest,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        },
                                        leadingIcon = if (selectedDestination == dest) {
                                            {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ColorPrimary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Filtre de durée
                            Text(
                                "Durée",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ColorTextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            val durations = listOf("1 semaine", "2 semaines", "1 mois", "3 mois", "1 an")
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = selectedDuration == null,
                                        onClick = { selectedDuration = null },
                                        label = { 
                                            Text(
                                                "Tous",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        },
                                        leadingIcon = if (selectedDuration == null) {
                                            {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ColorPrimary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                    durations.take(2).forEach { dur ->
                                        FilterChip(
                                            selected = selectedDuration == dur,
                                            onClick = { selectedDuration = if (selectedDuration == dur) null else dur },
                                            label = { 
                                                Text(
                                                    dur,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            leadingIcon = if (selectedDuration == dur) {
                                                {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            } else null,
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = ColorPrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    durations.drop(2).forEach { dur ->
                                        FilterChip(
                                            selected = selectedDuration == dur,
                                            onClick = { selectedDuration = if (selectedDuration == dur) null else dur },
                                            label = { 
                                                Text(
                                                    dur,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            leadingIcon = if (selectedDuration == dur) {
                                                {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            } else null,
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = ColorPrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
            
            // Tabs - Simple et épuré
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedTabIndex == index) ColorPrimary else MaterialTheme.colorScheme.surface,
                            onClick = { selectedTabIndex = index },
                            shadowElevation = if (selectedTabIndex == index) 4.dp else 1.dp
                        ) {
                            Text(
                                title,
                                fontSize = 14.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTabIndex == index) Color.White else ColorTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
            
            // Insurance List Header avec compteur
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (selectedTabIndex == 0) "Nos produits" else "Mes inscriptions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                    
                    // Compteur de résultats avec design moderne
                    val hasActiveFilters = selectedTabIndex == 0 && (
                        searchTerm.isNotEmpty() || 
                        selectedPriceRange != 0f..2000f || 
                        selectedDestination != null || 
                        selectedDuration != null
                    )
                    
                    if (hasActiveFilters || selectedTabIndex == 1) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = ColorPrimary.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    if (selectedTabIndex == 0) Icons.Default.FilterList else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = ColorPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    "${if (selectedTabIndex == 0) filteredInsurances.size else filteredSubscriptions.size} ${if ((if (selectedTabIndex == 0) filteredInsurances.size else filteredSubscriptions.size) > 1) "résultats" else "résultat"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ColorPrimary
                                )
                            }
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
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(vertical = 32.dp),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                            ) {
                                // Icône avec fond circulaire
                                Surface(
                                    shape = CircleShape,
                                    color = ColorPrimary.copy(alpha = 0.1f),
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            if (searchTerm.isNotEmpty() || selectedDestination != null || selectedDuration != null || selectedPriceRange != 0f..2000f) 
                                                Icons.Default.Search 
                                            else Icons.Default.Info,
                                            contentDescription = null,
                                            tint = ColorPrimary,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                Text(
                                    if (searchTerm.isNotEmpty() || selectedDestination != null || selectedDuration != null || selectedPriceRange != 0f..2000f) 
                                        "Aucun résultat trouvé"
                                    else if (selectedTabIndex == 0) "Aucune assurance disponible"
                                    else "Aucune inscription",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorTextPrimary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    if (searchTerm.isNotEmpty() || selectedDestination != null || selectedDuration != null || selectedPriceRange != 0f..2000f)
                                        "Essayez d'ajuster vos critères de recherche\nou vos filtres"
                                    else if (selectedTabIndex == 0) 
                                        "Les assurances créées par les agences\napparaîtront ici"
                                    else 
                                        "Vous n'êtes inscrit à aucune assurance\npour le moment",
                                    fontSize = 14.sp,
                                    color = ColorTextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                                
                                if (searchTerm.isNotEmpty() || selectedDestination != null || selectedDuration != null || selectedPriceRange != 0f..2000f) {
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    Button(
                                        onClick = {
                                            searchTerm = ""
                                            selectedPriceRange = 0f..2000f
                                            selectedDestination = null
                                            selectedDuration = null
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ColorPrimary,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Réinitialiser les filtres",
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(displayList) { insurance ->
                        InsuranceUserCard(
                            insurance = insurance,
                            onCreateRequest = { insuranceId ->
                                navController.navigate("${Constants.Routes.CREATE_INSURANCE_REQUEST}/$insuranceId")
                            },
                            onUnsubscribe = { viewModel.unsubscribeFromInsurance(it) },
                            isInMySubscriptionsTab = selectedTabIndex == 1,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
        
        // Snackbar for errors
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

@Composable
fun BenefitCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = ColorPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = ColorPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    title,
                    fontSize = 13.sp,
                    color = ColorTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}