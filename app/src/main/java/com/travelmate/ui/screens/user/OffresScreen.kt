package com.travelmate.ui.screens.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.travelmate.data.models.Airport
import com.travelmate.data.models.FlightOffer
import com.travelmate.ui.theme.*
import com.travelmate.utils.Constants
import com.travelmate.utils.Constants.Routes
import com.travelmate.viewmodel.OffersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffresScreen(navController: NavController? = null) {
    val viewModel: OffersViewModel = hiltViewModel()

    val offers by viewModel.offers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val fromAirport by viewModel.fromAirport.collectAsState()
    val toAirport by viewModel.toAirport.collectAsState()
    val dateDepart by viewModel.dateDepart.collectAsState()
    val dateReturn by viewModel.dateReturn.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val directOnly by viewModel.directOnly.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()

    var showFilters by remember { mutableStateOf(false) }
    var showSortOptions by remember { mutableStateOf(false) }
    var selectedFlights by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showComparisonDialog by remember { mutableStateOf(false) }
    var showRecommendationsDialog by remember { mutableStateOf(false) }
    
    val recommendations by viewModel.recommendations.collectAsState()
    val isLoadingRecommendations by viewModel.isLoadingRecommendations.collectAsState()

    Scaffold(
        floatingActionButton = {
            if (selectedFlights.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showComparisonDialog = true },
                    containerColor = ColorPrimary,
                    modifier = Modifier.padding(16.dp),
                    icon = {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = "Comparer",
                            tint = Color.White
                        )
                    },
                    text = {
                        Text(
                            text = "Comparer (${selectedFlights.size})",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Recherche de vols",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ColorBackground),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Recommendations Button
            item {
                Button(
                    onClick = { showRecommendationsDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(ColorPrimary, ColorSecondary),
                                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                    end = androidx.compose.ui.geometry.Offset(1000f, 0f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Obtenir des recommandations personnalisées",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
            
            // Loading State for Recommendations - Prominent display
            if (isLoadingRecommendations) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = ColorPrimary.copy(alpha = 0.05f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                ColorPrimary.copy(alpha = 0.2f)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = ColorPrimary,
                                        modifier = Modifier.size(56.dp),
                                        strokeWidth = 5.dp
                                    )
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = ColorPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "Analyse en cours par l'IA Gemini",
                                                fontSize = 18.sp,
                                                color = ColorPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = "Cela peut prendre quelques instants...",
                                            fontSize = 14.sp,
                                            color = ColorTextSecondary,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Recommendations Section Header
            if (recommendations.isNotEmpty() && !isLoadingRecommendations) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = ColorPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        "Recommandé pour vous",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorPrimary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
                
                items(
                    items = recommendations,
                    key = { it.offerId }
                ) { recommendedOffer ->
                    RecommendedFlightCard(
                        recommendedOffer = recommendedOffer,
                        onCardClick = {
                            val route = "${Routes.FLIGHT_DETAILS}/${recommendedOffer.getOffer().getIdValue()}"
                            navController?.navigate(route)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Divider(
                            color = ColorTextSecondary.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Autres offres",
                            fontSize = 14.sp,
                            color = ColorTextSecondary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Divider(
                            color = ColorTextSecondary.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            // Empty Recommendations State
            if (!isLoadingRecommendations && recommendations.isEmpty() && error == null) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = ColorBackground
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ColorTextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Aucune recommandation personnalisée trouvée",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorTextPrimary
                            )
                            Text(
                                text = "Essayez d'ajuster vos critères de recherche",
                                fontSize = 14.sp,
                                color = ColorTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { showRecommendationsDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = ColorPrimary
                                )
                            ) {
                                Text("Modifier mes préférences")
                            }
                        }
                    }
                    }
                }
            }
            
            // Search Bar Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        ColorPrimary.copy(alpha = 0.05f),
                                        ColorSecondary.copy(alpha = 0.02f)
                                    ),
                                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                    end = androidx.compose.ui.geometry.Offset(1000f, 0f)
                                )
                            )
                    ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Text Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "Rechercher une destination, ville...",
                                    color = ColorTextSecondary.copy(alpha = 0.6f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    null,
                                    tint = ColorPrimary
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        viewModel.setSearchQuery("")
                                        viewModel.searchOffers()
                                    }) {
                                        Icon(Icons.Default.Clear, null, tint = ColorTextSecondary)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ColorPrimary,
                                unfocusedBorderColor = ColorTextSecondary.copy(alpha = 0.3f),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge
                        )

                        // Filter Toggle Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showFilters = !showFilters },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = ColorPrimary
                                )
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Filtres", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }

                            OutlinedButton(
                                onClick = { showSortOptions = !showSortOptions },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = ColorPrimary
                                )
                            ) {
                                Icon(
                                    Icons.Default.Sort,
                                    null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Trier", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    }
                }
            }

            // Filters Panel
            item {
                AnimatedVisibility(
                    visible = showFilters,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    FiltersCard(
                        fromAirport = fromAirport,
                        toAirport = toAirport,
                        dateDepart = dateDepart,
                        dateReturn = dateReturn,
                        selectedType = selectedType,
                        directOnly = directOnly,
                        onFromAirportChange = { viewModel.setFromAirport(it) },
                        onToAirportChange = { viewModel.setToAirport(it) },
                        onDateDepartChange = { viewModel.setDateDepart(it) },
                        onDateReturnChange = { viewModel.setDateReturn(it) },
                        onTypeChange = { viewModel.setType(it) },
                        onDirectOnlyChange = { viewModel.setDirectOnly(it) },
                        onSearch = {
                            viewModel.searchOffers()
                            showFilters = false
                        },
                        onClear = {
                            viewModel.clearFilters()
                            showFilters = false
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Sort Options Panel
            item {
                AnimatedVisibility(
                    visible = showSortOptions,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    SortOptionsPanel(
                        selectedSort = sortBy,
                        onSortSelected = { sort ->
                            viewModel.setSortBy(sort)
                            viewModel.searchOffers()
                            showSortOptions = false
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Search Button
            item {
                Button(
                    onClick = { viewModel.searchOffers() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(ColorPrimary, ColorSecondary),
                                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                    end = androidx.compose.ui.geometry.Offset(1000f, 0f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Search,
                                null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Rechercher des vols",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Results Count
            if (offers.isNotEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${offers.size} ${if (offers.size == 1) "vol trouvé" else "vols trouvés"}",
                                fontSize = 14.sp,
                                color = ColorTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            if (sortBy != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Transparent
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(ColorAccent.copy(alpha = 0.8f), ColorAccent.copy(alpha = 0.6f)),
                                                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                                    end = androidx.compose.ui.geometry.Offset(100f, 0f)
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = getSortLabel(sortBy),
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Loading State
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = ColorPrimary,
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 4.dp
                            )
                            Text(
                                text = "Recherche de vols...",
                                color = ColorTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Error State
            error?.let { errorMessage ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ColorError.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                null,
                                tint = ColorError,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Erreur",
                                    fontWeight = FontWeight.Bold,
                                    color = ColorError,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = errorMessage,
                                    color = ColorError.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            }
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(Icons.Default.Close, null, tint = ColorError)
                            }
                        }
                    }
                }
            }

            // Empty State
            if (!isLoading && error == null && offers.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Flight,
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = ColorTextSecondary.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "Aucun vol trouvé",
                                color = ColorTextSecondary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Essayez de modifier vos critères de recherche",
                                color = ColorTextSecondary.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                }
            }

            // Offers List - Scrollable
            items(offers) { offer ->
                val offerId = offer.id ?: offer.getIdValue()
                val isSelected = selectedFlights.contains(offerId)
                ProfessionalFlightCard(
                    offer = offer,
                    isSelected = isSelected,
                    canSelect = selectedFlights.size < 3 || isSelected,
                    onSelectionChanged = { shouldSelect ->
                        selectedFlights = if (shouldSelect) {
                            if (selectedFlights.size < 3) {
                                selectedFlights + offerId
                            } else {
                                selectedFlights
                            }
                        } else {
                            selectedFlights - offerId
                        }
                    },
                    onClick = {
                        navController?.navigate("${Constants.Routes.FLIGHT_DETAILS}/${offerId}")
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }

    // Comparison Dialog
    if (showComparisonDialog && selectedFlights.isNotEmpty()) {
        val flightsToCompare = offers.filter {
            val offerId = it.id ?: it.getIdValue()
            selectedFlights.contains(offerId)
        }
        if (flightsToCompare.isNotEmpty()) {
            FlightComparisonDialog(
                flights = flightsToCompare,
                onDismiss = {
                    showComparisonDialog = false
                },
                onClearSelection = {
                    selectedFlights = emptySet()
                    showComparisonDialog = false
                }
            )
        } else {
            // Debug: Si pas de vols trouvés, fermer le dialog
            LaunchedEffect(flightsToCompare.isEmpty()) {
                if (flightsToCompare.isEmpty()) {
                    showComparisonDialog = false
                }
            }
        }
    }
    
    // Recommendations Dialog
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    
    if (showRecommendationsDialog) {
        PreferencesBottomSheet(
            offers = offers,
            onDismiss = { 
                focusManager.clearFocus() // Hide keyboard
                showRecommendationsDialog = false
                viewModel.clearError()
            },
            onConfirm = { preferences ->
                // Close dialog immediately and hide keyboard
                focusManager.clearFocus() // Hide keyboard
                showRecommendationsDialog = false
                viewModel.clearError()
                viewModel.getRecommendations(preferences)
            },
            isLoading = isLoadingRecommendations,
            error = error
        )
    }
}

@Composable
fun FiltersCard(
    fromAirport: String?,
    toAirport: String?,
    dateDepart: String?,
    dateReturn: String?,
    selectedType: String?,
    directOnly: Boolean?,
    onFromAirportChange: (String?) -> Unit,
    onToAirportChange: (String?) -> Unit,
    onDateDepartChange: (String?) -> Unit,
    onDateReturnChange: (String?) -> Unit,
    onTypeChange: (String?) -> Unit,
    onDirectOnlyChange: (Boolean?) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            ColorPrimary.copy(alpha = 0.05f),
                            ColorSecondary.copy(alpha = 0.03f)
                        )
                    )
                )
        ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Filtres de recherche",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ColorPrimary
                )
                TextButton(onClick = onClear) {
                    Text("Réinitialiser", fontSize = 12.sp, color = ColorPrimary)
                }
            }

            Divider(color = ColorTextSecondary.copy(alpha = 0.2f))

            // Flight Type Selection
            Column {
                Text(
                    text = "Type de vol",
                    fontSize = 12.sp,
                    color = ColorTextSecondary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FlightTypeChip(
                        label = "Aller-retour",
                        selected = selectedType == "aller-retour",
                        onClick = { onTypeChange(if (selectedType == "aller-retour") null else "aller-retour") },
                        modifier = Modifier.weight(1f)
                    )
                    FlightTypeChip(
                        label = "Aller simple",
                        selected = selectedType == "aller-simple",
                        onClick = { onTypeChange(if (selectedType == "aller-simple") null else "aller-simple") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Airport Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // From Airport
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Départ",
                        fontSize = 12.sp,
                        color = ColorTextSecondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = fromAirport ?: "",
                        onValueChange = { onFromAirportChange(it.ifBlank { null }) },
                        placeholder = { Text("Ex: TUN", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                Icons.Default.FlightTakeoff,
                                null,
                                tint = ColorPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorPrimary,
                            unfocusedBorderColor = ColorTextSecondary.copy(alpha = 0.3f)
                        )
                    )
                }

                // Swap Button
                IconButton(
                    onClick = {
                        val temp = fromAirport
                        onFromAirportChange(toAirport)
                        onToAirportChange(temp)
                    },
                    modifier = Modifier
                        .align(Alignment.Bottom)
                        .padding(bottom = 8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = ColorPrimary.copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            null,
                            tint = ColorPrimary,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(8.dp)
                        )
                    }
                }

                // To Airport
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Arrivée",
                        fontSize = 12.sp,
                        color = ColorTextSecondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = toAirport ?: "",
                        onValueChange = { onToAirportChange(it.ifBlank { null }) },
                        placeholder = { Text("Ex: ORY", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                Icons.Default.FlightLand,
                                null,
                                tint = ColorPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorPrimary,
                            unfocusedBorderColor = ColorTextSecondary.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            // Date Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Departure Date
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Date départ",
                        fontSize = 12.sp,
                        color = ColorTextSecondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = dateDepart ?: "",
                        onValueChange = { onDateDepartChange(it.ifBlank { null }) },
                        placeholder = { Text("YYYY-MM-DD", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                Icons.Default.CalendarToday,
                                null,
                                tint = ColorPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorPrimary,
                            unfocusedBorderColor = ColorTextSecondary.copy(alpha = 0.3f)
                        )
                    )
                }

                // Return Date (if round trip)
                if (selectedType == "aller-retour") {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Date retour",
                            fontSize = 12.sp,
                            color = ColorTextSecondary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = dateReturn ?: "",
                            onValueChange = { onDateReturnChange(it.ifBlank { null }) },
                            placeholder = { Text("YYYY-MM-DD", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    null,
                                    tint = ColorPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ColorPrimary,
                                unfocusedBorderColor = ColorTextSecondary.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }

            // Direct Flights Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Flight,
                        null,
                        tint = ColorPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Vols directs uniquement",
                        fontSize = 14.sp,
                        color = ColorTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Switch(
                    checked = directOnly == true,
                    onCheckedChange = { onDirectOnlyChange(if (it) true else null) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ColorPrimary
                    )
                )
            }

            // Apply Button
            Button(
                onClick = onSearch,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(ColorPrimary, ColorSecondary),
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(1000f, 0f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Appliquer les filtres",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        }
    }
}

@Composable
fun FlightTypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = true,
        label = {
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ColorPrimary,
            selectedLabelColor = Color.White,
            containerColor = Color.White,
            labelColor = ColorTextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            selectedBorderColor = ColorPrimary,
            borderColor = ColorTextSecondary.copy(alpha = 0.3f),
            selectedBorderWidth = 2.dp,
            borderWidth = 1.dp
        )
    )
}

@Composable
fun SortOptionsPanel(
    selectedSort: String?,
    onSortSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Trier par", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ColorPrimary)
                IconButton(onClick = { onSortSelected(selectedSort) }) {
                    Icon(Icons.Default.Close, null)
                }
            }


            SortOptionItem("Les moins chers", "price", selectedSort, onSortSelected)
            SortOptionItem("Les plus rapides", "duration", selectedSort, onSortSelected)
            SortOptionItem("Départ le plus tôt", "departure_time", selectedSort, onSortSelected)
        }
    }
}

@Composable
fun SortOptionItem(
    label: String,
    value: String,
    selectedSort: String?,
    onSortSelected: (String?) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSortSelected(if (selectedSort == value) null else value) },
        color = if (selectedSort == value) ColorPrimary.copy(alpha = 0.1f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 14.sp, color = if (selectedSort == value) ColorPrimary else ColorTextPrimary)
            if (selectedSort == value) {
                Icon(Icons.Default.Check, null, tint = ColorPrimary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun ProfessionalFlightCard(
    offer: FlightOffer,
    isSelected: Boolean = false,
    canSelect: Boolean = true,
    onSelectionChanged: (Boolean) -> Unit = {},
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 3.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ColorPrimary.copy(alpha = 0.05f) else Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            ColorPrimary.copy(alpha = 0.03f)
                        ),
                        startY = 0f,
                        endY = 1000f
                    )
                )
        ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selection Checkbox Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable { onSelectionChanged(!isSelected) }
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = {
                            if (!it || canSelect) {
                                onSelectionChanged(it)
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = ColorPrimary,
                            uncheckedColor = ColorTextSecondary.copy(alpha = 0.5f)
                        )
                    )
                    Text(
                        text = if (isSelected) "Sélectionné pour comparaison" else "Sélectionner pour comparer",
                        fontSize = 12.sp,
                        color = if (isSelected) ColorPrimary else ColorTextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
                if (!canSelect && !isSelected) {
                    Text(
                        text = "Maximum 3 vols",
                        fontSize = 10.sp,
                        color = ColorWarning,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Header with Airline and Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(40.dp),
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(ColorPrimary, ColorSecondary),
                                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                        end = androidx.compose.ui.geometry.Offset(40f, 40f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Flight,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = offer.getAirlineName().ifEmpty { "Compagnie aérienne" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextPrimary
                        )
                        offer.flightNumber?.let {
                            Text(
                                text = "Vol $it",
                                fontSize = 12.sp,
                                color = ColorTextSecondary
                            )
                        }
                        // Destination
                        val toAirport = offer.getToAirport()
                        val destination = when {
                            !toAirport.name.isBlank() -> toAirport.name
                            !toAirport.city.isNullOrBlank() -> toAirport.city
                            !toAirport.code.isBlank() -> toAirport.code
                            else -> "Destination"
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                null,
                                modifier = Modifier.size(14.dp),
                                tint = ColorPrimary
                            )
                            Text(
                                text = destination,
                                fontSize = 13.sp,
                                color = ColorPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(ColorPrimary, ColorSecondary),
                                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                        end = androidx.compose.ui.geometry.Offset(100f, 0f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = offer.getFormattedPrice(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Text(
                        text = "par personne",
                        fontSize = 11.sp,
                        color = ColorTextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Divider(
                color = ColorPrimary.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            // From - To Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val fromAirport = offer.getFromAirport()
                val toAirport = offer.getToAirport()

                // From
                Column(horizontalAlignment = Alignment.Start) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.FlightTakeoff,
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = ColorPrimary
                        )
                        Text(
                            text = "De",
                            fontSize = 12.sp,
                            color = ColorTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = fromAirport.code.ifEmpty {
                            fromAirport.name.ifEmpty {
                                fromAirport.city ?: "Aéroport"
                            }
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (!fromAirport.name.isBlank() && !fromAirport.code.isBlank()) {
                        Text(
                            text = fromAirport.name,
                            fontSize = 12.sp,
                            color = ColorTextSecondary,
                            maxLines = 1
                        )
                    } else if (!fromAirport.code.isBlank() && fromAirport.city != null) {
                        Text(
                            text = fromAirport.city,
                            fontSize = 12.sp,
                            color = ColorTextSecondary,
                            maxLines = 1
                        )
                    }
                }

                // Arrow
                Icon(
                    Icons.Default.ArrowForward,
                    null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(horizontal = 8.dp),
                    tint = ColorPrimary
                )

                // To
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Vers",
                            fontSize = 12.sp,
                            color = ColorTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            Icons.Default.FlightLand,
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = ColorSecondary
                        )
                    }
                    Text(
                        text = toAirport.code.ifEmpty {
                            toAirport.name.ifEmpty {
                                toAirport.city ?: "Aéroport"
                            }
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorSecondary,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                    if (!toAirport.name.isBlank() && !toAirport.code.isBlank()) {
                        Text(
                            text = toAirport.name,
                            fontSize = 12.sp,
                            color = ColorTextSecondary,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1
                        )
                    } else if (!toAirport.code.isBlank() && toAirport.city != null) {
                        Text(
                            text = toAirport.city,
                            fontSize = 12.sp,
                            color = ColorTextSecondary,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1
                        )
                    }
                }
            }

            // Duration Section - Simple and Professional
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        null,
                        modifier = Modifier.size(18.dp),
                        tint = ColorTextSecondary
                    )
                    Column {
                        Text(
                            text = "Durée",
                            fontSize = 12.sp,
                            color = ColorTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = offer.duration ?: offer.getDepartureSegment()?.getDurationValue() ?: "N/A",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextPrimary
                        )
                    }
                }
            }

            // Flight Times
            offer.getDepartureSegment()?.let { departureSegment ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Departure Time
                    Column {
                        val departureDetails = departureSegment.getDepartureDetails()
                        Text(
                            text = departureDetails.getTimeValue().ifEmpty { "--:--" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextPrimary
                        )
                        Text(
                            text = "Départ",
                            fontSize = 11.sp,
                            color = ColorTextSecondary
                        )
                    }

                    // Duration
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = ColorTextSecondary
                        )
                        Text(
                            text = departureSegment.getDurationValue().ifEmpty { "N/A" },
                            fontSize = 13.sp,
                            color = ColorTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        if (departureSegment.isDirect()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ColorSuccess.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "Direct",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorSuccess,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ColorWarning.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "${departureSegment.getStops()} escale(s)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorWarning,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Arrival Time
                    Column(horizontalAlignment = Alignment.End) {
                        val arrivalDetails = departureSegment.getArrivalDetails()
                        Text(
                            text = arrivalDetails.getTimeValue().ifEmpty { "--:--" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextPrimary,
                            textAlign = TextAlign.End
                        )
                        Text(
                            text = "Arrivée",
                            fontSize = 11.sp,
                            color = ColorTextSecondary,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            // Additional Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (offer.availableSeats != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.EventSeat,
                                null,
                                modifier = Modifier.size(14.dp),
                                tint = ColorTextSecondary
                            )
                            Text(
                                text = "${offer.availableSeats} places",
                                fontSize = 11.sp,
                                color = ColorTextSecondary
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(ColorAccent.copy(alpha = 0.8f), ColorAccent.copy(alpha = 0.6f)),
                                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                        end = androidx.compose.ui.geometry.Offset(100f, 0f)
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = when (offer.getTypeValue()) {
                                    "aller-retour" -> "Aller-retour"
                                    "multi-destin" -> "Multi-destinations"
                                    else -> "Aller simple"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent,
                    modifier = Modifier.clickable(onClick = onClick)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(ColorPrimary, ColorSecondary),
                                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                    end = androidx.compose.ui.geometry.Offset(100f, 0f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Voir détails",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Icon(
                                Icons.Default.ArrowForward,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
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
fun FlightComparisonDialog(
    flights: List<FlightOffer>,
    onDismiss: () -> Unit,
    onClearSelection: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.95f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(ColorPrimary, ColorSecondary)
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
                            Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "Comparaison de vols",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${flights.size} vol${if (flights.size > 1) "s" else ""} sélectionné${if (flights.size > 1) "s" else ""}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = Color.White
                        )
                    }
                }
            }

            // Content - Scrollable Column
            if (flights.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun vol à comparer",
                        color = ColorTextSecondary,
                        fontSize = 16.sp
                    )
                }
            } else {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                        ComparisonRow(
                            label = "Itinéraire",
                            icon = Icons.Default.LocationOn,
                            flights = flights,
                            getValue = {
                                val from = it.getFromAirport()
                                val to = it.getToAirport()
                                val fromCity = from.city ?: from.name.ifEmpty { from.code }
                                val toCity = to.city ?: to.name.ifEmpty { to.code }
                                "$fromCity → $toCity"
                            }
                        )

                        ComparisonRow(
                            label = "Date de départ",
                            icon = Icons.Default.CalendarToday,
                            flights = flights,
                            getValue = {
                                it.getDepartureDate() ?: it.getDepartureSegment()?.getDepartureDetails()?.date ?: "N/A"
                            }
                        )

                        ComparisonRow(
                            label = "Prix",
                            icon = Icons.Default.LocalOffer,
                            flights = flights,
                            getValue = { it.getFormattedPrice() },
                            highlightBest = true,
                            isBest = { flight, allFlights ->
                                flight.getPrice() == allFlights.minOfOrNull { it.getPrice() }
                            }
                        )

                        ComparisonRow(
                            label = "Durée totale",
                            icon = Icons.Default.Schedule,
                            flights = flights,
                            getValue = {
                                it.duration ?: it.getDepartureSegment()?.getDurationValue() ?: "N/A"
                            },
                            highlightBest = true,
                            isBest = { flight, allFlights ->
                                val flightDuration = flight.duration ?: ""
                                allFlights.all {
                                    val otherDuration = it.duration ?: ""
                                    flightDuration <= otherDuration || otherDuration.isEmpty()
                                }
                            }
                        )

                        ComparisonRow(
                            label = "Compagnie aérienne",
                            icon = Icons.Default.Flight,
                            flights = flights,
                            getValue = { it.getAirlineName().ifEmpty { "N/A" } }
                        )

                        ComparisonRow(
                            label = "Type de vol",
                            icon = Icons.Default.Flight,
                            flights = flights,
                            getValue = {
                                val isDirect = it.direct == true || it.getDepartureSegment()?.isDirect() == true
                                if (isDirect) "Direct" else "Avec escale(s)"
                            },
                            highlightBest = true,
                            isBest = { flight, allFlights ->
                                val isDirect = flight.direct == true || flight.getDepartureSegment()?.isDirect() == true
                                isDirect || allFlights.none {
                                    it.direct == true || it.getDepartureSegment()?.isDirect() == true
                                }
                            }
                        )

                        ComparisonRow(
                            label = "Heure de départ",
                            icon = Icons.Default.FlightTakeoff,
                            flights = flights,
                            getValue = {
                                it.getDepartureSegment()?.getDepartureDetails()?.getTimeValue()?.ifEmpty { "--:--" } ?: "--:--"
                            }
                        )

                        ComparisonRow(
                            label = "Heure d'arrivée",
                            icon = Icons.Default.FlightLand,
                            flights = flights,
                            getValue = {
                                it.getDepartureSegment()?.getArrivalDetails()?.getTimeValue()?.ifEmpty { "--:--" } ?: "--:--"
                            }
                        )

                        ComparisonRow(
                            label = "Nombre d'escales",
                            icon = Icons.Default.SwapHoriz,
                            flights = flights,
                            getValue = {
                                val stops = it.stops ?: it.getDepartureSegment()?.getStops() ?: 0
                                if (stops == 0) {
                                    "Aucune"
                                } else {
                                    "$stops escale${if (stops > 1) "s" else ""}"
                                }
                            },
                            highlightBest = true,
                            isBest = { flight, allFlights ->
                                val flightStops = flight.stops ?: flight.getDepartureSegment()?.getStops() ?: 0
                                allFlights.all {
                                    val otherStops = it.stops ?: it.getDepartureSegment()?.getStops() ?: 0
                                    flightStops <= otherStops
                                }
                            }
                        )

                        if (flights.any { it.getTypeValue() == "aller-retour" || it.getReturnDate() != null }) {
                            ComparisonRow(
                                label = "Date de retour",
                                icon = Icons.Default.CalendarToday,
                                flights = flights,
                                getValue = {
                                    it.getReturnDate() ?: it.getReturnSegment()?.getDepartureDetails()?.date ?: "N/A"
                                }
                            )

                            ComparisonRow(
                                label = "Aller - Durée",
                                icon = Icons.Default.FlightTakeoff,
                                flights = flights,
                                getValue = {
                                    it.getDepartureSegment()?.getDurationValue() ?: "N/A"
                                }
                            )

                            ComparisonRow(
                                label = "Retour - Durée",
                                icon = Icons.Default.FlightLand,
                                flights = flights,
                                getValue = {
                                    if (it.getTypeValue() == "aller-retour" || it.getReturnSegment() != null) {
                                        it.getReturnSegment()?.getDurationValue() ?: "N/A"
                                    } else {
                                        "N/A"
                                    }
                                }
                            )
                        }
                    }
            }

            // Footer Actions - CORRECTED VERSION
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onClearSelection,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ColorPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Effacer la sélection", fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(ColorPrimary, ColorSecondary),
                                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                    end = androidx.compose.ui.geometry.Offset(1000f, 0f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Fermer",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    flights: List<FlightOffer>,
    getValue: (FlightOffer) -> String,
    highlightBest: Boolean = false,
    isBest: ((FlightOffer, List<FlightOffer>) -> Boolean)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = ColorPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )
            }

            Divider(color = ColorTextSecondary.copy(alpha = 0.2f))

            // Flight Values
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                flights.forEachIndexed { index, flight ->
                    val value = getValue(flight)
                    val isBestValue = highlightBest && isBest?.invoke(flight, flights) == true

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (isBestValue) {
                                    Modifier.border(2.dp, ColorSuccess, RoundedCornerShape(8.dp))
                                } else {
                                    Modifier
                                }
                            ),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isBestValue) {
                            ColorSuccess.copy(alpha = 0.1f)
                        } else {
                            ColorBackground
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isBestValue) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = ColorSuccess
                                ) {
                                    Text(
                                        "Meilleur",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = value,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBestValue) ColorSuccess else ColorTextPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 3,
                                lineHeight = 16.sp
                            )
                            Text(
                                text = "Vol ${index + 1}",
                                fontSize = 10.sp,
                                color = ColorTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesBottomSheet(
    offers: List<FlightOffer>,
    onDismiss: () -> Unit,
    onConfirm: (com.travelmate.data.models.Preferences) -> Unit,
    isLoading: Boolean,
    error: String? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var destination by remember { mutableStateOf("") }
    var tripType by remember { mutableStateOf("aller-retour") }
    var maxBudget by remember { mutableStateOf(500f) }
    var directOnly by remember { mutableStateOf(false) }
    
    // Extract unique destinations from offers
    val availableDestinations = remember(offers) {
        offers.mapNotNull { it.getToAirport() }
            .mapNotNull { it.city ?: it.name ?: it.code }
            .distinct()
            .sorted()
    }
    
    // Filter destinations based on input
    val filteredDestinations = remember(destination, availableDestinations) {
        if (destination.isBlank()) {
            availableDestinations.take(5)
        } else {
            availableDestinations.filter { 
                it.contains(destination, ignoreCase = true) 
            }.take(5)
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Recommandations personnalisées",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorPrimary
                    )
                    Text(
                        text = "Répondez à quelques questions",
                        fontSize = 14.sp,
                        color = ColorTextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Fermer",
                        tint = ColorTextSecondary
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Destination
                Column {
                    Text(
                        text = "Destination (ville ou pays)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ex: Paris, Istanbul, Rome...") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ColorPrimary
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorPrimary,
                            unfocusedBorderColor = ColorTextSecondary.copy(alpha = 0.3f)
                        )
                    )
                    // Suggestions
                    if (filteredDestinations.isNotEmpty() && destination.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = ColorBackground
                            )
                        ) {
                            Column {
                                filteredDestinations.forEach { suggestion ->
                                    TextButton(
                                        onClick = { destination = suggestion },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            suggestion,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Start
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Trip Type
                Column {
                    Text(
                        text = "Type de voyage",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = tripType == "aller-retour",
                            onClick = { tripType = "aller-retour" },
                            label = { Text("Aller-retour") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = tripType == "aller-simple",
                            onClick = { tripType = "aller-simple" },
                            label = { Text("Aller-simple") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Budget
                Column {
                    Text(
                        text = "Indiquez votre budget maximum en dinar tunisien (TND)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Slider(
                            value = maxBudget,
                            onValueChange = { maxBudget = it },
                            valueRange = 100f..2000f,
                            steps = 18,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = ColorPrimary,
                                activeTrackColor = ColorPrimary
                            )
                        )
                        Text(
                            text = "${maxBudget.toInt()} TND",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorPrimary,
                            modifier = Modifier.width(70.dp)
                        )
                    }
                }
                
                // Direct Only
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Vol direct uniquement",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorPrimary
                        )
                        Text(
                            text = "Ne montrer que les vols sans escale",
                            fontSize = 12.sp,
                            color = ColorTextSecondary
                        )
                    }
                    Switch(
                        checked = directOnly,
                        onCheckedChange = { directOnly = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ColorPrimary
                        )
                    )
                }
            }
            
            // Error Message
            error?.let { errorMessage ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ColorError.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = ColorError,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = errorMessage,
                            fontSize = 13.sp,
                            color = ColorError,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Loading Message
            if (isLoading) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ColorPrimary.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = ColorPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Analyse en cours par l'IA Gemini... Cela peut prendre quelques instants",
                            fontSize = 13.sp,
                            color = ColorPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Confirm Button
            Button(
                onClick = {
                    onConfirm(
                        com.travelmate.data.models.Preferences(
                            tripType = tripType,
                            countryOrCity = destination,
                            maxBudget = maxBudget.toInt(),
                            directOnly = directOnly
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && destination.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(ColorPrimary, ColorSecondary),
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(1000f, 0f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            "Voir mes recommandations",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendedFlightCard(
    recommendedOffer: com.travelmate.data.models.RecommendedOffer,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val offer = recommendedOffer.getOffer()
    
    Card(
        onClick = onCardClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with AI Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ColorPrimary.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ColorPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Recommandé par IA",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorPrimary
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ColorPrimary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            offer.getFormattedPrice(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Flight Tags (Direct, Aller-retour, Score)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (offer.direct == true) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ColorSuccess.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Direct",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorSuccess,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                when (offer.type?.lowercase() ?: "") {
                    "aller-retour" -> {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ColorPrimary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Aller-retour",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = ColorPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    "aller-simple" -> {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ColorSecondary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Aller-simple",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = ColorSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                // Score badge if available
                recommendedOffer.getScore().takeIf { it > 0 }?.let { score ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ColorAccent.copy(alpha = 0.1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = ColorAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "$score%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorAccent
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Flight Route
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val fromAirport = offer.getFromAirport()
                val toAirport = offer.getToAirport()
                
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = fromAirport.code.ifEmpty {
                            fromAirport.name.ifEmpty {
                                fromAirport.city ?: "Aéroport"
                            }
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorPrimary
                    )
                    Text(
                        text = fromAirport.city ?: fromAirport.name ?: "",
                        fontSize = 12.sp,
                        color = ColorTextSecondary
                    )
                }
                
                Icon(
                    Icons.Default.Flight,
                    contentDescription = null,
                    tint = ColorPrimary,
                    modifier = Modifier.size(32.dp)
                )
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = toAirport.code.ifEmpty {
                            toAirport.name.ifEmpty {
                                toAirport.city ?: "Aéroport"
                            }
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorPrimary
                    )
                    Text(
                        text = toAirport.city ?: toAirport.name ?: "",
                        fontSize = 12.sp,
                        color = ColorTextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // AI Explanation
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ColorBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = ColorPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "Pourquoi cette offre ?",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = recommendedOffer.getExplanation(),
                        fontSize = 13.sp,
                        color = ColorTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

fun getSortLabel(sort: String?): String {
    return when (sort) {
        "best" -> "Le meilleur"
        "price" -> "Les moins chers"
        "duration" -> "Les plus rapides"
        "departure_time" -> "Départ le plus tôt"
        else -> ""
    }
}
