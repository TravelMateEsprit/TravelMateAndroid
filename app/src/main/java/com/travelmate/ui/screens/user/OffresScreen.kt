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
import androidx.compose.ui.draw.drawWithContent
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import com.travelmate.data.models.Airport
import com.travelmate.data.models.FlightOffer
import com.travelmate.data.models.PriceAlert
import com.travelmate.data.models.AlertStatus
import com.travelmate.data.service.PriceAlertService
import com.travelmate.ui.components.PriceAlertsBottomSheet
import com.travelmate.ui.components.*
import com.travelmate.ui.theme.*
import com.travelmate.utils.Constants
import com.travelmate.utils.Constants.Routes
import com.travelmate.viewmodel.OffersViewModel
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.*

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
    val origin by viewModel.origin.collectAsState()
    val destination by viewModel.destination.collectAsState()
    val departureDate by viewModel.departureDate.collectAsState()
    val returnDate by viewModel.returnDate.collectAsState()
    val adults by viewModel.adults.collectAsState()

    val searchDone = origin != null && destination != null && departureDate != null

    var selectedFlights by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showComparisonDialog by remember { mutableStateOf(false) }
    var showRecommendationsDialog by remember { mutableStateOf(false) }
    var showAlertsDialog by remember { mutableStateOf(false) }
    
    val recommendations by viewModel.recommendations.collectAsState()
    val isLoadingRecommendations by viewModel.isLoadingRecommendations.collectAsState()
    
    // Price alerts from ViewModel
    val alerts by viewModel.priceAlerts.collectAsState()
    val alertsCount = alerts.size
    val triggeredCount = alerts.count { it.status == AlertStatus.TRIGGERED }
    
    // Check alerts when offers are loaded
    LaunchedEffect(offers) {
        if (offers.isNotEmpty() && !isLoading) {
            viewModel.checkAlerts(offers)
            // Show snackbar for triggered alerts
            // We'll add this later
        }
    }

    Scaffold(
        floatingActionButton = {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Price alerts FAB (visible only after a search with results)
                if (searchDone && offers.isNotEmpty()) {
                    FloatingActionButton(
                        onClick = { showAlertsDialog = true },
                        containerColor = ColorSecondary,
                        modifier = Modifier.size(56.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (alertsCount > 0) {
                                    Badge {
                                        Text(
                                            text = if (triggeredCount > 0) triggeredCount.toString() else "",
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Gérer les alertes de prix",
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
                
                // Comparison FAB (only if flights selected)
                if (selectedFlights.isNotEmpty()) {
                    ExtendedFloatingActionButton(
                        onClick = { showComparisonDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        icon = {
                            Icon(
                                Icons.Default.SwapHoriz,
                                contentDescription = "Comparer",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                        text = {
                            Text(
                                text = "Comparer (${selectedFlights.size})",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Recherche de vols",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 76.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // SECTION 1: AI RECOMMENDATIONS (TOP - Shows FIRST)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            "Recommandations personnalisées",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Show recommendations if available
                        if (recommendations.isEmpty() && !isLoadingRecommendations) {
                            Text(
                                "Aucune recommandation personnalisée trouvée",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Essayez d'ajuster vos critères de recherche",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showRecommendationsDialog = true },
                                modifier = Modifier.fillMaxWidth(),
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
                                                colors = listOf(MaterialTheme.colorScheme.primary, ColorSecondary),
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
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Obtenir des recommandations personnalisées",
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        } else if (isLoadingRecommendations) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Analyse en cours par l'IA Gemini...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Display recommendation cards if available
            if (recommendations.isNotEmpty() && !isLoadingRecommendations) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
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
                        modifier = Modifier.padding(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 6.dp)
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Recherche manuelle",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Divider(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else if (!isLoadingRecommendations) {
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Recherche manuelle",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Divider(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            // SECTION 2: MANUAL SEARCH (BELOW - Scroll to see)
            item {
                UnifiedFlightSearchForm(
                    viewModel = viewModel,
                    isLoading = isLoading,
                    error = error,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
                )
            }
            
            

            // Results Count
            if (offers.isNotEmpty() && !isLoading) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${offers.size} ${if (offers.size == 1) "vol trouvé" else "vols trouvés"}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                            .padding(start = 10.dp, top = 4.dp, end = 10.dp, bottom = 4.dp)
                                    ) {
                                        Text(
                                            text = getSortLabel(sortBy),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onPrimary,
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
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 4.dp
                            )
                            Text(
                                text = "Recherche de vols...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
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
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Erreur",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            }
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error)
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
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "Aucun vol trouvé",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Essayez de modifier vos critères de recherche",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
                        // Save selected offer to ViewModel before navigation
                        viewModel.setSelectedOffer(offer)
                        navController?.navigate("${Constants.Routes.FLIGHT_DETAILS}/${offerId}")
                    },
                    modifier = Modifier.padding(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 6.dp)
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
    
    // Price Alerts Bottom Sheet
    val alertCoroutineScope = rememberCoroutineScope()
    if (showAlertsDialog) {
        PriceAlertsBottomSheet(
            alerts = alerts,
            offers = offers,
            onDismiss = { showAlertsDialog = false },
            onDeleteAlert = { alertId ->
                alertCoroutineScope.launch {
                    viewModel.deleteAlert(alertId)
                }
            },
            onCreateAlert = { offer, priceThreshold ->
                alertCoroutineScope.launch {
                    viewModel.createAlertFromOffer(offer, priceThreshold)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmadeusSearchForm(
    viewModel: OffersViewModel,
    isLoading: Boolean,
    error: String?,
    modifier: Modifier = Modifier
) {
    var originText by remember { mutableStateOf("") }
    var destinationText by remember { mutableStateOf("") }
    var departureDateText by remember { mutableStateOf("") }
    var returnDateText by remember { mutableStateOf("") }
    var adultsText by remember { mutableStateOf("1") }
    var directOnly by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showRoundTrip by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            ColorSecondary.copy(alpha = 0.05f)
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(1000f, 0f)
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Flight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Recherche de vols en temps réel",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    "Recherchez des vols réels avec Amadeus",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                
                // Error Message (from validation or API)
                if (errorMessage != null || error != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
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
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                errorMessage ?: error ?: "",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { 
                                    errorMessage = null
                                    viewModel.clearError()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Fermer",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                
                // Origin and Destination
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Origin
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "D'où partez-vous?",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = originText,
                            onValueChange = { 
                                originText = it.uppercase().take(3)
                                errorMessage = null
                            },
                            placeholder = { Text("TUN", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.FlightTakeoff,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            ),
                            enabled = !isLoading
                        )
                        if (originText.isNotEmpty() && originText.length != 3) {
                            Text(
                                "Code IATA (3 lettres)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }
                    
                    // Swap Button
                    IconButton(
                        onClick = {
                            val temp = originText
                            originText = destinationText
                            destinationText = temp
                        },
                        modifier = Modifier
                            .align(Alignment.Bottom)
                            .padding(bottom = 8.dp)
                            .size(40.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Icon(
                                Icons.Default.SwapHoriz,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(8.dp)
                            )
                        }
                    }
                    
                    // Destination
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Où allez-vous?",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = destinationText,
                            onValueChange = { 
                                destinationText = it.uppercase().take(3)
                                errorMessage = null
                            },
                            placeholder = { Text("FCO", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.FlightLand,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            ),
                            enabled = !isLoading
                        )
                        if (destinationText.isNotEmpty() && destinationText.length != 3) {
                            Text(
                                "Code IATA (3 lettres)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }
                }
                
                // Dates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Departure Date
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Date de départ",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = departureDateText,
                            onValueChange = { 
                                departureDateText = it
                                errorMessage = null
                            },
                            placeholder = { Text("YYYY-MM-DD", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            ),
                            enabled = !isLoading
                        )
                    }
                    
                    // Return Date (optional)
                    if (showRoundTrip) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Date de retour",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            OutlinedTextField(
                                value = returnDateText,
                                onValueChange = { 
                                    returnDateText = it
                                    errorMessage = null
                                },
                                placeholder = { Text("YYYY-MM-DD", fontSize = 14.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                ),
                                enabled = !isLoading
                            )
                        }
                    }
                }
                
                // Round Trip Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Aller-retour",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = showRoundTrip,
                        onCheckedChange = { 
                            showRoundTrip = it
                            if (!it) returnDateText = ""
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                
                // Adults and Filters Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Adults
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Nombre de passagers",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = adultsText,
                            onValueChange = { 
                                val value = it.filter { char -> char.isDigit() }
                                if (value.isEmpty() || value.toIntOrNull()?.let { it > 0 && it <= 9 } == true) {
                                    adultsText = value.ifEmpty { "1" }
                                }
                            },
                            placeholder = { Text("1", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            ),
                            enabled = !isLoading
                        )
                    }
                    
                    // Sort
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Trier par",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = when (sortOption) {
                                    "price" -> "Prix"
                                    "duration" -> "Durée"
                                    "departure_time" -> "Heure de départ"
                                    else -> "Aucun"
                                },
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                ),
                                enabled = !isLoading
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Aucun") },
                                    onClick = {
                                        sortOption = null
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Prix") },
                                    onClick = {
                                        sortOption = "price"
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Durée") },
                                    onClick = {
                                        sortOption = "duration"
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Heure de départ") },
                                    onClick = {
                                        sortOption = "departure_time"
                                        expanded = false
                                    }
                                )
                            }
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
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Vols directs uniquement",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Switch(
                        checked = directOnly,
                        onCheckedChange = { directOnly = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                
                // Search Button
                Button(
                    onClick = {
                        // Validation
                        when {
                            originText.isEmpty() -> {
                                errorMessage = "Veuillez entrer l'aéroport de départ"
                            }
                            destinationText.isEmpty() -> {
                                errorMessage = "Veuillez entrer l'aéroport de destination"
                            }
                            originText.length != 3 -> {
                                errorMessage = "Code IATA invalide pour l'origine (3 lettres requis)"
                            }
                            destinationText.length != 3 -> {
                                errorMessage = "Code IATA invalide pour la destination (3 lettres requis)"
                            }
                            departureDateText.isEmpty() -> {
                                errorMessage = "Veuillez sélectionner une date de départ"
                            }
                            !isValidDate(departureDateText) -> {
                                errorMessage = "Format de date invalide (YYYY-MM-DD requis)"
                            }
                            isDateInPast(departureDateText) -> {
                                errorMessage = "La date de départ ne peut pas être dans le passé"
                            }
                            showRoundTrip && returnDateText.isNotEmpty() -> {
                                if (!isValidDate(returnDateText)) {
                                    errorMessage = "Format de date de retour invalide (YYYY-MM-DD requis)"
                                } else if (isDateBefore(returnDateText, departureDateText)) {
                                    errorMessage = "La date de retour doit être après la date de départ"
                                } else {
                                    // Valid - perform search
                                    viewModel.searchFlights(
                                        origin = originText,
                                        destination = destinationText,
                                        departureDate = departureDateText,
                                        returnDate = returnDateText.ifEmpty { null },
                                        adults = adultsText.toIntOrNull() ?: 1,
                                        direct = if (directOnly) true else null,
                                        sort = sortOption
                                    )
                                    errorMessage = null
                                }
                            }
                            else -> {
                                // Valid - perform search
                                viewModel.searchFlights(
                                    origin = originText,
                                    destination = destinationText,
                                    departureDate = departureDateText,
                                    returnDate = returnDateText.ifEmpty { null },
                                    adults = adultsText.toIntOrNull() ?: 1,
                                    direct = if (directOnly) true else null,
                                    sort = sortOption
                                )
                                errorMessage = null
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp),
                    enabled = !isLoading
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary, ColorSecondary),
                                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                    end = androidx.compose.ui.geometry.Offset(1000f, 0f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Recherche en cours...",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Rechercher des vols",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper functions for date validation
fun isValidDate(dateString: String): Boolean {
    val regex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
    if (!regex.matches(dateString)) return false
    
    return try {
        val parts = dateString.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt()
        
        month in 1..12 && day in 1..31 && year >= 2024
    } catch (e: Exception) {
        false
    }
}

fun isDateInPast(dateString: String): Boolean {
    if (!isValidDate(dateString)) return false
    
    return try {
        val parts = dateString.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt() - 1
        val day = parts[2].toInt()
        
        val date = java.util.Calendar.getInstance().apply {
            set(year, month, day)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        
        date.before(today)
    } catch (e: Exception) {
        false
    }
}

fun isDateBefore(date1: String, date2: String): Boolean {
    if (!isValidDate(date1) || !isValidDate(date2)) return false
    
    return try {
        val parts1 = date1.split("-")
        val year1 = parts1[0].toInt()
        val month1 = parts1[1].toInt() - 1
        val day1 = parts1[2].toInt()
        
        val parts2 = date2.split("-")
        val year2 = parts2[0].toInt()
        val month2 = parts2[1].toInt() - 1
        val day2 = parts2[2].toInt()
        
        val date1Cal = java.util.Calendar.getInstance().apply {
            set(year1, month1, day1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        
        val date2Cal = java.util.Calendar.getInstance().apply {
            set(year2, month2, day2)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        
        date1Cal.before(date2Cal)
    } catch (e: Exception) {
        false
    }
}

/**
 * Interface de recherche unifiée qui combine Amadeus et filtres
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedFlightSearchForm(
    viewModel: OffersViewModel,
    isLoading: Boolean,
    error: String?,
    modifier: Modifier = Modifier
) {
    // État pour le type de voyage
    var tripType by remember { mutableStateOf("aller-simple") } // "aller-simple" ou "aller-retour"
    
    // État pour les aéroports
    var originAirport by remember { mutableStateOf<Airport?>(null) }
    var destinationAirport by remember { mutableStateOf<Airport?>(null) }
    
    // État pour les dates
    var departureDate by remember { mutableStateOf<String?>(null) }
    var returnDate by remember { mutableStateOf<String?>(null) }
    
    // État pour les filtres
    var adults by remember { mutableStateOf(1) }
    var directOnly by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf<String?>(null) }
    var showAdvancedFilters by remember { mutableStateOf(false) }
    
    // État pour les erreurs
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Mettre à jour errorMessage quand error change
    LaunchedEffect(error) {
        if (error != null) {
            errorMessage = error
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            ColorSecondary.copy(alpha = 0.05f)
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(1000f, 0f)
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Flight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Recherche de vols en temps réel",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Message d'erreur
                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
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
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                errorMessage ?: "",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { 
                                    errorMessage = null
                                    viewModel.clearError()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Fermer",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                
                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                
                // Type de voyage
                Column {
                    Text(
                        text = "Type de voyage",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = tripType == "aller-simple",
                            onClick = { 
                                tripType = "aller-simple"
                                returnDate = null
                            },
                            label = { Text("Aller simple") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        FilterChip(
                            selected = tripType == "aller-retour",
                            onClick = { tripType = "aller-retour" },
                            label = { Text("Aller-retour") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
                
                // Aéroports avec autocomplete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Origin
                    Column(modifier = Modifier.weight(1f)) {
                        AirportAutocomplete(
                            label = "D'où partez-vous?",
                            selectedAirport = originAirport,
                            onAirportSelected = { 
                                originAirport = it
                                errorMessage = null
                            },
                            enabled = !isLoading
                        )
                    }
                    
                    // Swap Button
                    IconButton(
                        onClick = {
                            val temp = originAirport
                            originAirport = destinationAirport
                            destinationAirport = temp
                        },
                        modifier = Modifier
                            .align(Alignment.Bottom)
                            .padding(bottom = 8.dp)
                            .size(40.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Icon(
                                Icons.Default.SwapHoriz,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(8.dp)
                            )
                        }
                    }
                    
                    // Destination
                    Column(modifier = Modifier.weight(1f)) {
                        AirportAutocomplete(
                            label = "Où allez-vous?",
                            selectedAirport = destinationAirport,
                            onAirportSelected = { 
                                destinationAirport = it
                                errorMessage = null
                            },
                            enabled = !isLoading
                        )
                    }
                }
                
                // Dates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Departure Date
                    Column(modifier = Modifier.weight(1f)) {
                        DatePickerField(
                            label = "Date de départ",
                            selectedDate = departureDate,
                            onDateSelected = { 
                                departureDate = it
                                errorMessage = null
                            },
                            enabled = !isLoading
                        )
                    }
                    
                    // Return Date (si aller-retour)
                    if (tripType == "aller-retour") {
                        Column(modifier = Modifier.weight(1f)) {
                            DatePickerField(
                                label = "Date de retour",
                                selectedDate = returnDate,
                                onDateSelected = { 
                                    returnDate = it
                                    errorMessage = null
                                },
                                enabled = !isLoading,
                                minDate = departureDate?.let {
                                    try {
                                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)?.time
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                            )
                        }
                    }
                }
                
                // Nombre de passagers
                AdultsSelector(
                    adults = adults,
                    onAdultsChanged = { adults = it },
                    enabled = !isLoading
                )
                
                // Filtres avancés (collapsible)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAdvancedFilters = !showAdvancedFilters },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "Filtres avancés",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Icon(
                                if (showAdvancedFilters) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        AnimatedVisibility(visible = showAdvancedFilters) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
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
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Vols directs uniquement",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Switch(
                                        checked = directOnly,
                                        onCheckedChange = { directOnly = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                            checkedTrackColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                                
                                // Sort
                                Column {
                                    Text(
                                        text = "Trier par",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    var expanded by remember { mutableStateOf(false) }
                                    ExposedDropdownMenuBox(
                                        expanded = expanded,
                                        onExpandedChange = { expanded = !expanded }
                                    ) {
                                        OutlinedTextField(
                                            value = when (sortOption) {
                                                "price" -> "Prix"
                                                "duration" -> "Durée"
                                                "departure_time" -> "Heure de départ"
                                                else -> "Aucun"
                                            },
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                            ),
                                            enabled = !isLoading
                                        )
                                        ExposedDropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Aucun") },
                                                onClick = {
                                                    sortOption = null
                                                    expanded = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Prix") },
                                                onClick = {
                                                    sortOption = "price"
                                                    expanded = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Durée") },
                                                onClick = {
                                                    sortOption = "duration"
                                                    expanded = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Heure de départ") },
                                                onClick = {
                                                    sortOption = "departure_time"
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Search Button
                Button(
                    onClick = {
                        // Validation
                        when {
                            originAirport == null || originAirport?.code.isNullOrEmpty() -> {
                                errorMessage = "Veuillez sélectionner l'aéroport de départ"
                            }
                            destinationAirport == null || destinationAirport?.code.isNullOrEmpty() -> {
                                errorMessage = "Veuillez sélectionner l'aéroport d'arrivée"
                            }
                            departureDate.isNullOrEmpty() -> {
                                errorMessage = "Veuillez sélectionner une date de départ"
                            }
                            tripType == "aller-retour" && returnDate.isNullOrEmpty() -> {
                                errorMessage = "Veuillez sélectionner une date de retour"
                            }
                            tripType == "aller-retour" && returnDate != null && departureDate != null -> {
                                val returnDateValue = returnDate
                                val departureDateValue = departureDate
                                if (returnDateValue != null && departureDateValue != null && isDateBefore(returnDateValue, departureDateValue)) {
                                    errorMessage = "La date de retour doit être après la date de départ"
                                } else {
                                    // Valid - perform search
                                    viewModel.searchFlights(
                                        origin = originAirport!!.code,
                                        destination = destinationAirport!!.code,
                                        departureDate = departureDateValue!!,
                                        returnDate = returnDateValue,
                                        adults = adults,
                                        direct = if (directOnly) true else null,
                                        sort = sortOption
                                    )
                                    errorMessage = null
                                }
                            }
                            else -> {
                                // Valid - perform search
                                val departureDateValue = departureDate
                                if (departureDateValue != null) {
                                    viewModel.searchFlights(
                                        origin = originAirport!!.code,
                                        destination = destinationAirport!!.code,
                                        departureDate = departureDateValue,
                                        returnDate = returnDate,
                                        adults = adults,
                                        direct = if (directOnly) true else null,
                                        sort = sortOption
                                    )
                                    errorMessage = null
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp),
                    enabled = !isLoading
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary, ColorSecondary),
                                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                    end = androidx.compose.ui.geometry.Offset(1000f, 0f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Recherche en cours...",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Rechercher des vols",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
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
    origin: String?,
    destination: String?,
    departureDate: String?,
    returnDate: String?,
    adults: Int?,
    onFromAirportChange: (String?) -> Unit,
    onToAirportChange: (String?) -> Unit,
    onDateDepartChange: (String?) -> Unit,
    onDateReturnChange: (String?) -> Unit,
    onTypeChange: (String?) -> Unit,
    onDirectOnlyChange: (Boolean?) -> Unit,
    onOriginChange: (String?) -> Unit,
    onDestinationChange: (String?) -> Unit,
    onDepartureDateChange: (String?) -> Unit,
    onReturnDateChange: (String?) -> Unit,
    onAdultsChange: (Int?) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
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
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = onClear) {
                    Text("Réinitialiser", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

            // Flight Type Selection
            Column {
                Text(
                    text = "Type de vol",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
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
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                }

                // Return Date (if round trip)
                if (selectedType == "aller-retour") {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Date retour",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Vols directs uniquement",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
                Switch(
                    checked = directOnly == true,
                    onCheckedChange = { onDirectOnlyChange(if (it) true else null) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.surface,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
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
                                colors = listOf(MaterialTheme.colorScheme.primary, ColorSecondary),
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
                        color = MaterialTheme.colorScheme.onPrimary
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
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.surface,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            borderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                Text("Trier par", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
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
        color = if (selectedSort == value) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 14.sp, color = if (selectedSort == value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            if (selectedSort == value) {
                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
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
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 0.dp else 3.dp,
            pressedElevation = if (isSelected) 0.dp else 3.dp,
            focusedElevation = if (isSelected) 0.dp else 3.dp,
            hoveredElevation = if (isSelected) 0.dp else 3.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)
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
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                    Text(
                        text = if (isSelected) "Sélectionné pour comparaison" else "Sélectionner pour comparer",
                        fontSize = 12.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
                                        colors = listOf(MaterialTheme.colorScheme.primary, ColorSecondary),
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
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = offer.getAirlineName().ifEmpty { "Compagnie aérienne" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        offer.flightNumber?.let {
                            Text(
                                text = "Vol $it",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = destination,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
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
                                        colors = listOf(MaterialTheme.colorScheme.primary, ColorSecondary),
                                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                        end = androidx.compose.ui.geometry.Offset(100f, 0f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(start = 12.dp, top = 6.dp, end = 12.dp, bottom = 6.dp)
                        ) {
                            Text(
                                text = offer.getFormattedPrice(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Text(
                        text = "par personne",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Divider(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
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
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "De",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (!fromAirport.name.isBlank() && !fromAirport.code.isBlank()) {
                        Text(
                            text = fromAirport.name,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    } else if (!fromAirport.code.isBlank() && fromAirport.city != null) {
                        Text(
                            text = fromAirport.city,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    tint = MaterialTheme.colorScheme.primary
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1
                        )
                    } else if (!toAirport.code.isBlank() && toAirport.city != null) {
                        Text(
                            text = toAirport.city,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column {
                        Text(
                            text = "Durée",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = offer.duration ?: offer.getDepartureSegment()?.getDurationValue() ?: "N/A",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Départ",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = departureSegment.getDurationValue().ifEmpty { "N/A" },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                    modifier = Modifier.padding(start = 6.dp, top = 2.dp, end = 6.dp, bottom = 2.dp)
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
                                    modifier = Modifier.padding(start = 6.dp, top = 2.dp, end = 6.dp, bottom = 2.dp)
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
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End
                        )
                        Text(
                            text = "Arrivée",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${offer.availableSeats} places",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                .padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp)
                        ) {
                            Text(
                                text = when (offer.getTypeValue()) {
                                    "aller-retour" -> "Aller-retour"
                                    "multi-destin" -> "Multi-destinations"
                                    else -> "Aller simple"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
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
                                    colors = listOf(MaterialTheme.colorScheme.primary, ColorSecondary),
                                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                    end = androidx.compose.ui.geometry.Offset(100f, 0f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Voir détails",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Icon(
                                Icons.Default.ArrowForward,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
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
        containerColor = MaterialTheme.colorScheme.surface,
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
                            Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "Comparaison de vols",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "${flights.size} vol${if (flights.size > 1) "s" else ""} sélectionné${if (flights.size > 1) "s" else ""}",
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        contentColor = MaterialTheme.colorScheme.primary
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
                                    colors = listOf(MaterialTheme.colorScheme.primary, ColorSecondary),
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
                            color = MaterialTheme.colorScheme.onPrimary
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

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
                            MaterialTheme.colorScheme.surfaceVariant
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
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(start = 6.dp, top = 2.dp, end = 6.dp, bottom = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = value,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBestValue) ColorSuccess else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                maxLines = 3,
                                lineHeight = 16.sp
                            )
                            Text(
                                text = "Vol ${index + 1}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
    var destinationAirport by remember { mutableStateOf<Airport?>(null) }
    var tripType by remember { mutableStateOf("aller-retour") }
    var maxBudget by remember { mutableStateOf(500f) }
    var directOnly by remember { mutableStateOf(false) }
    

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
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
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Répondez à quelques questions",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Fermer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                    AirportAutocomplete(
                        label = "Destination (ville ou pays)",
                        selectedAirport = destinationAirport,
                        onAirportSelected = { airport ->
                            destinationAirport = airport
                            destination = airport.city ?: airport.name ?: ""
                        },
                        enabled = true
                )
                
                // Trip Type
                Column {
                    Text(
                        text = "Type de voyage",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.primary,
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
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "${maxBudget.toInt()} TND",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
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
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Ne montrer que les vols sans escale",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = directOnly,
                        onCheckedChange = { directOnly = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.surface,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
            
            // Error Message
            error?.let { errorMessage ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
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
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = errorMessage,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Loading Message
            if (isLoading) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Analyse en cours par l'IA Gemini... Cela peut prendre quelques instants",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
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
                                colors = listOf(MaterialTheme.colorScheme.primary, ColorSecondary),
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(1000f, 0f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            "Voir mes recommandations",
                            color = MaterialTheme.colorScheme.onPrimary,
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
            containerColor = MaterialTheme.colorScheme.surface
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
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Recommandé par IA",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            offer.getFormattedPrice(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 12.dp, top = 6.dp, end = 12.dp, bottom = 6.dp)
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
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp)
                        )
                    }
                }
                when (offer.type?.lowercase() ?: "") {
                    "aller-retour" -> {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Aller-retour",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp)
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
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp)
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
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp)
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
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = fromAirport.city ?: fromAirport.name ?: "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    Icons.Default.Flight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = toAirport.city ?: toAirport.name ?: "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "Pourquoi cette offre ?",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = recommendedOffer.getExplanation(),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
