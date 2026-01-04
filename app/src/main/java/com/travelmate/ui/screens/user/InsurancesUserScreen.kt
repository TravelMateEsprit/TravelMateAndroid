package com.travelmate.ui.screens.user

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.AutoAwesome
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
    viewModel: InsurancesUserViewModel = hiltViewModel(),
    profileViewModel: com.travelmate.viewmodel.ProfileViewModel = hiltViewModel()
) {
    val insurances by viewModel.insurances.collectAsState()
    val mySubscriptions by viewModel.mySubscriptions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // AI Recommendations
    val recommendations by viewModel.recommendations.collectAsState()
    val isLoadingRecommendations by viewModel.isLoadingRecommendations.collectAsState()
    
    // Profil utilisateur pour le banner
    val userProfile by profileViewModel.userProfile.collectAsState()
    val profileCompletionPercentage = userProfile?.profileCompletionPercentage ?: 0
    
    // Récupérer les noms d'assurances recommandées depuis UserPreferences
    val context = androidx.compose.ui.platform.LocalContext.current
    val userPreferences = remember { com.travelmate.utils.UserPreferences(context) }
    val recommendedInsuranceNames = remember { userPreferences.getRecommendedInsuranceNames() }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Toutes les assurances", "Mes inscriptions")
    
    // États pour la recherche et les filtres
    var searchTerm by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedPriceRange by remember { mutableStateOf(0f..2000f) }
    var selectedDestination by remember { mutableStateOf<String?>(null) }
    var selectedDuration by remember { mutableStateOf<String?>(null) }
    
    // États pour la sélection et comparaison
    var selectedInsurances by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showComparisonDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadAllInsurances()
        viewModel.loadMySubscriptions()
        // Rafraîchir le profil au retour de l'écran pour mettre à jour le banner
        profileViewModel.loadProfile()
        android.util.Log.d("InsurancesUserScreen", "Profil rafraîchi, completion: $profileCompletionPercentage%")
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
    val filteredInsurances = remember(insurances, searchTerm, selectedPriceRange, selectedDestination, selectedDuration, recommendedInsuranceNames) {
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
        // NOUVEAU: Trier pour afficher en premier les assurances recommandées par IA
        .sortedByDescending { insurance ->
            if (recommendedInsuranceNames.contains(insurance.name)) 1 else 0
        }
    }
    
    // Filtrer les inscriptions
    val filteredSubscriptions = remember(mySubscriptions, searchTerm, recommendedInsuranceNames) {
        mySubscriptions.filter { insurance ->
            val destinations = insurance.conditions?.destination?.joinToString(" ") ?: ""
            
            searchTerm.isEmpty() || 
                insurance.name.contains(searchTerm, ignoreCase = true) ||
                insurance.description.contains(searchTerm, ignoreCase = true) ||
                destinations.contains(searchTerm, ignoreCase = true)
        }
        // NOUVEAU: Trier pour afficher en premier les assurances recommandées par IA
        .sortedByDescending { insurance ->
            if (recommendedInsuranceNames.contains(insurance.name)) 1 else 0
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
    
    Scaffold(
        floatingActionButton = {
            // Bouton de comparaison (seulement si des assurances sont sélectionnées)
            if (selectedInsurances.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showComparisonDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    icon = {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "Comparer avec IA",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    text = {
                        Text(
                            text = "Comparer avec IA (${selectedInsurances.size})",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { scaffoldPadding ->
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
            
            // Banner de profil incomplet (afficher seulement si profil non complet)
            if (profileCompletionPercentage < 100) {
                item {
                    AnimatedVisibility(
                        visible = contentVisible,
                        enter = fadeIn(animationSpec = tween(400, delayMillis = 50)) +
                                slideInVertically(
                                    animationSpec = tween(400, delayMillis = 50),
                                    initialOffsetY = { it / 4 }
                                )
                    ) {
                        ProfileCompletionBanner(
                            profileCompleteness = profileCompletionPercentage,
                            onCompleteClick = {
                                navController.navigate(Constants.Routes.COMPLETE_PROFILE)
                            }
                        )
                    }
                }
            } else {
                // Banner pour les recommandations IA (profil complété)
                item {
                    AnimatedVisibility(
                        visible = contentVisible,
                        enter = fadeIn(animationSpec = tween(400, delayMillis = 50)) +
                                slideInVertically(
                                    animationSpec = tween(400, delayMillis = 50),
                                    initialOffsetY = { it / 4 }
                                )
                    ) {
                        AIRecommendationBanner(
                            onViewRecommendationsClick = {
                                navController.navigate(Constants.Routes.AI_RECOMMENDATIONS)
                            }
                        )
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
                                // Nouveaux paramètres pour la sélection
                                isSelected = selectedInsurances.contains(insurance._id),
                                onSelectionToggle = {
                                    selectedInsurances = if (selectedInsurances.contains(insurance._id)) {
                                        selectedInsurances - insurance._id
                                    } else {
                                        if (selectedInsurances.size < 3) {
                                            selectedInsurances + insurance._id
                                        } else {
                                            selectedInsurances
                                        }
                                    }
                                },
                                // Badge "Recommandé par IA" si l'assurance est dans les recommandations
                                isRecommendedByAI = recommendedInsuranceNames.contains(insurance.name),
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
    
    // Dialog de comparaison IA
    if (showComparisonDialog) {
        InsuranceAIComparisonDialog(
            insurances = insurances.filter { selectedInsurances.contains(it._id) },
            onDismiss = { showComparisonDialog = false },
            onClearSelection = {
                selectedInsurances = emptySet()
                showComparisonDialog = false
            }
        )
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsuranceAIComparisonDialog(
    insurances: List<com.travelmate.data.models.Insurance>,
    onDismiss: () -> Unit,
    onClearSelection: () -> Unit
) {
    val compareViewModel: com.travelmate.viewmodel.CompareInsurancesViewModel = hiltViewModel()
    val comparisonResult by compareViewModel.comparisonResult.collectAsState()
    val isLoading by compareViewModel.isLoading.collectAsState()
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    LaunchedEffect(insurances) {
        compareViewModel.clearSelection()
        insurances.forEach { compareViewModel.toggleInsuranceSelection(it) }
        compareViewModel.compareInsurances()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.95f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, ColorSecondary)
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "Comparaison IA",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "${insurances.size} assurance${if (insurances.size > 1) "s" else ""}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            // Content
            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier.padding(32.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                shape = RoundedCornerShape(24.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(24.dp)
                                ) {
                                    // Icône IA animée
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        modifier = Modifier.size(80.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                    }
                                    
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            "Analyse en cours...",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            "L'IA compare vos ${insurances.size} assurances",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                    comparisonResult != null -> {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Résumé IA modernisé
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                ),
                                shape = RoundedCornerShape(20.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.tertiary,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            "Analyse IA",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        comparisonResult!!.summary,
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                            
                            // Meilleur choix modernisé
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                ),
                                shape = RoundedCornerShape(20.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                modifier = Modifier.border(
                                    width = 3.dp,
                                    color = Color(0xFF10B981),
                                    shape = RoundedCornerShape(20.dp)
                                )
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.EmojiEvents,
                                                    contentDescription = null,
                                                    tint = Color(0xFF059669),
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            "Meilleur Choix",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = Color(0xFF047857)
                                        )
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        comparisonResult!!.bestChoice.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        comparisonResult!!.bestChoice.reason,
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            
                            // Analyse détaillée titre
                            Text(
                                "Analyse Détaillée",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            // Détails modernisés
                            comparisonResult!!.insurances.forEach { comp ->
                                val insurance = insurances.find { it._id == comp.id }
                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        // Header avec score
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    comp.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 17.sp
                                                )
                                                if (insurance != null) {
                                                    Text(
                                                        "${insurance.price.toInt()} TND • ${insurance.duration}",
                                                        fontSize = 13.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.padding(top = 4.dp)
                                                    )
                                                }
                                            }
                                            // Score badge
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = when {
                                                    comp.overallScore >= 8.0 -> SuccessGreen.copy(alpha = 0.15f)
                                                    comp.overallScore >= 6.0 -> AccentOrange.copy(alpha = 0.15f)
                                                    else -> ErrorRed.copy(alpha = 0.15f)
                                                },
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.5.dp,
                                                    when {
                                                        comp.overallScore >= 8.0 -> SuccessGreen
                                                        comp.overallScore >= 6.0 -> AccentOrange
                                                        else -> ErrorRed
                                                    }
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = when {
                                                            comp.overallScore >= 8.0 -> SuccessGreen
                                                            comp.overallScore >= 6.0 -> AccentOrange
                                                            else -> ErrorRed
                                                        },
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Text(
                                                        String.format("%.1f/10", comp.overallScore),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        color = when {
                                                            comp.overallScore >= 8.0 -> SuccessGreen
                                                            comp.overallScore >= 6.0 -> AccentOrange
                                                            else -> ErrorRed
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                                        
                                        // Points forts
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.ThumbUp,
                                                contentDescription = null,
                                                tint = SuccessGreen,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Text(
                                                "Points Forts",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 16.sp,
                                                color = SuccessGreen
                                            )
                                        }
                                        Spacer(Modifier.height(12.dp))
                                        comp.strengths.forEach { strength ->
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier.padding(start = 8.dp)
                                            ) {
                                                Text(
                                                    "•",
                                                    color = SuccessGreen,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                                Text(
                                                    strength,
                                                    fontSize = 14.sp,
                                                    lineHeight = 20.sp,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            Spacer(Modifier.height(6.dp))
                                        }
                                        
                                        Spacer(Modifier.height(16.dp))
                                        
                                        // Points faibles
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.ThumbDown,
                                                contentDescription = null,
                                                tint = ErrorRed,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Text(
                                                "Points Faibles",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 16.sp,
                                                color = ErrorRed
                                            )
                                        }
                                        Spacer(Modifier.height(12.dp))
                                        comp.weaknesses.forEach { weakness ->
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier.padding(start = 8.dp)
                                            ) {
                                                Text(
                                                    "•",
                                                    color = ErrorRed,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                                Text(
                                                    weakness,
                                                    fontSize = 14.sp,
                                                    lineHeight = 20.sp,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            Spacer(Modifier.height(6.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Footer - Un seul bouton Fermer
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Fermer",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
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
fun ProfileCompletionBanner(
    profileCompleteness: Int,
    onCompleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (profileCompleteness < 100) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "✨ Profil de voyage",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Recommandations personnalisées",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        if (profileCompleteness > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = profileCompleteness / 100f,
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "$profileCompleteness% complété",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onCompleteClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        if (profileCompleteness == 0) "Compléter" else "Continuer",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
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

@Composable
fun AIRecommendationCard(
    recommendation: com.travelmate.data.models.InsuranceRecommendation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(280.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header avec score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recommendation.insuranceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        recommendation.matchScore >= 90 -> Color(0xFF4CAF50)
                        recommendation.matchScore >= 75 -> Color(0xFFFF9800)
                        else -> Color(0xFF9E9E9E)
                    }
                ) {
                    Text(
                        text = "${recommendation.matchScore}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Raison
            Text(
                text = recommendation.reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Facteurs de correspondance
            if (recommendation.matchFactors.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    recommendation.matchFactors.take(2).forEach { factor ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = factor,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AIRecommendationBanner(
    onViewRecommendationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "✨ Recommandations IA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Découvrez les assurances parfaites pour vous",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            
            Button(
                onClick = onViewRecommendationsClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Voir les recommandations",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

