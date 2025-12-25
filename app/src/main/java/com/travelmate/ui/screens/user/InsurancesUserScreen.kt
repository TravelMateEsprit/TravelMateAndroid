package com.travelmate.ui.screens.user

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import kotlinx.coroutines.delay

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
    
    // Animation states
    var headerVisible by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        headerVisible = true
        delay(100)
        contentVisible = true
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 76.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Header moderne, simple et compact
            item {
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = fadeIn(animationSpec = tween(400)) + slideInVertically(
                        animationSpec = tween(400),
                        initialOffsetY = { -it / 2 }
                    )
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = ColorPrimary,
                        shadowElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            // Titre simple et moderne
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Assurances",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                
                                // Bouton d'action compact
                                FilledTonalIconButton(
                                    onClick = { navController.navigate(Constants.Routes.MY_INSURANCE_REQUESTS) },
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = Color.White.copy(alpha = 0.2f),
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Assignment,
                                        contentDescription = "Mes demandes",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Stats minimalistes inline
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CompactStatChip(
                                    icon = Icons.Default.Security,
                                    count = insurances.size,
                                    label = "disponibles"
                                )
                                CompactStatChip(
                                    icon = Icons.Default.CheckCircle,
                                    count = mySubscriptions.size,
                                    label = "inscrites"
                                )
                            }
                        }
                    }
                }
            }
            
            // Barre de recherche moderne et épurée avec animation
            item {
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(animationSpec = tween(400, delayMillis = 100)) + 
                            slideInVertically(
                                animationSpec = tween(400, delayMillis = 100),
                                initialOffsetY = { it / 4 }
                            )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        // Barre de recherche simple
                        OutlinedTextField(
                            value = searchTerm,
                            onValueChange = { searchTerm = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { 
                                Text(
                                    "Rechercher une assurance...",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Rechercher",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                Row {
                                    AnimatedVisibility(
                                        visible = searchTerm.isNotEmpty(),
                                        enter = fadeIn() + scaleIn(),
                                        exit = fadeOut() + scaleOut()
                                    ) {
                                        IconButton(onClick = { searchTerm = "" }) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Effacer",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    
                                    // Bouton filtres minimaliste
                                    val activeFiltersCount = listOf(
                                        selectedPriceRange != 0f..2000f,
                                        selectedDestination != null,
                                        selectedDuration != null
                                    ).count { it }
                                    
                                    IconButton(onClick = { showFilters = !showFilters }) {
                                        BadgedBox(
                                            badge = {
                                                if (activeFiltersCount > 0) {
                                                    Badge(
                                                        containerColor = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.offset(x = 4.dp, y = (-4).dp)
                                                    ) {
                                                        Text(
                                                            "$activeFiltersCount",
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.onPrimary
                                                        )
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Tune,
                                                contentDescription = "Filtres",
                                                tint = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            singleLine = true
                        )
                    }
                }
            }
            
            // Panneau de filtres moderne avec animation
            if (showFilters && selectedTabIndex == 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .animateContentSize(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                                    color = MaterialTheme.colorScheme.onSurface
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
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Réinitialiser",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Filtre de prix avec design moderne
                            Text(
                                "Budget",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${selectedPriceRange.start.toInt()} TND",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "${selectedPriceRange.endInclusive.toInt()} TND",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            RangeSlider(
                                value = selectedPriceRange,
                                onValueChange = { selectedPriceRange = it },
                                valueRange = 0f..2000f,
                                steps = 39,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Filtre de destination avec chips modernes
                            Text(
                                "Destination",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
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
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
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
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
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
                                color = MaterialTheme.colorScheme.onSurface
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
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
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
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
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
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
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
            
            // Tabs modernes et minimalistes
            item {
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(animationSpec = tween(400, delayMillis = 150)) + 
                            slideInVertically(
                                animationSpec = tween(400, delayMillis = 150),
                                initialOffsetY = { it / 4 }
                            )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val scale by animateFloatAsState(
                                targetValue = if (selectedTabIndex == index) 1.02f else 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "tab_scale"
                            )
                            
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .scale(scale),
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else Color.Transparent,
                                onClick = { selectedTabIndex = index },
                                border = if (selectedTabIndex == index) null else androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            ) {
                                Text(
                                    title,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
            
            // Section titre avec compteur moderne
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (selectedTabIndex == 0) "Tous les produits" else "Mes inscriptions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Compteur minimaliste
                    val count = if (selectedTabIndex == 0) filteredInsurances.size else filteredSubscriptions.size
                    if (count > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                "$count",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
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
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                val displayList = if (selectedTabIndex == 0) filteredInsurances else filteredSubscriptions
                
                if (displayList.isEmpty()) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 40.dp)
                        ) {
                            // Icône moderne
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                modifier = Modifier.size(100.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (searchTerm.isNotEmpty() || selectedDestination != null || selectedDuration != null || selectedPriceRange != 0f..2000f) 
                                            Icons.Default.SearchOff 
                                        else Icons.Default.AssignmentLate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                if (searchTerm.isNotEmpty() || selectedDestination != null || selectedDuration != null || selectedPriceRange != 0f..2000f) 
                                    "Aucun résultat"
                                else if (selectedTabIndex == 0) "Aucune assurance"
                                else "Aucune inscription",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                if (searchTerm.isNotEmpty() || selectedDestination != null || selectedDuration != null || selectedPriceRange != 0f..2000f)
                                    "Ajustez vos critères de recherche"
                                else if (selectedTabIndex == 0) 
                                    "Les assurances apparaîtront ici"
                                else 
                                    "Vous n'avez aucune inscription",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            
                            if (searchTerm.isNotEmpty() || selectedDestination != null || selectedDuration != null || selectedPriceRange != 0f..2000f) {
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                TextButton(
                                    onClick = {
                                        searchTerm = ""
                                        selectedPriceRange = 0f..2000f
                                        selectedDestination = null
                                        selectedDuration = null
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Réinitialiser",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    itemsIndexed(displayList) { index, insurance ->
                        // Animation avec délai progressif pour chaque item
                        var itemVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay((index * 50L).coerceAtMost(500L))
                            itemVisible = true
                        }
                        
                        AnimatedVisibility(
                            visible = itemVisible,
                            enter = fadeIn(animationSpec = tween(300)) + 
                                    slideInVertically(
                                        animationSpec = tween(300),
                                        initialOffsetY = { it / 3 }
                                    ) + 
                                    scaleIn(
                                        animationSpec = tween(300),
                                        initialScale = 0.9f
                                    )
                        ) {
                            InsuranceUserCard(
                                insurance = insurance,
                                onCreateRequest = { insuranceId ->
                                    navController.navigate("${Constants.Routes.CREATE_INSURANCE_REQUEST}/$insuranceId")
                                },
                                onUnsubscribe = { viewModel.unsubscribeFromInsurance(it) },
                                onCreateClaim = if (selectedTabIndex == 1) {
                                    { insuranceId ->
                                        navController.navigate("${Constants.Routes.CREATE_CLAIM}?insuranceId=$insuranceId")
                                    }
                                } else null,
                                isInMySubscriptionsTab = selectedTabIndex == 1,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Snackbar for errors
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}

@Composable
fun ModernStatCard(
    count: Int,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Icône avec fond
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            // Texte
            Column {
                Text(
                    count.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
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
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    title,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun CompactStatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(16.dp)
        )
        Text(
            "$count $label",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium
        )
    }
}