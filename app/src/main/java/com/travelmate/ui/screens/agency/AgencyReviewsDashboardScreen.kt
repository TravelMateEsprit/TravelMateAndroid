package com.travelmate.ui.screens.agency

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.travelmate.data.models.Review
import com.travelmate.ui.components.*
import com.travelmate.viewmodel.AgencyReviewViewModel

/**
 * Dashboard screen for agencies to view all reviews for their insurances
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyReviewsDashboardScreen(
    navController: NavController,
    viewModel: AgencyReviewViewModel = hiltViewModel()
) {
    val reviews by viewModel.agencyReviews.collectAsState()
    val stats by viewModel.agencyStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var selectedFilter by remember { mutableStateOf("all") }
    var showStatsBottomSheet by remember { mutableStateOf(false) }
    
    // Load agency reviews on screen open
    LaunchedEffect(Unit) {
        viewModel.loadAgencyReviews()
    }
    
    // Filter reviews by insurance if needed
    val filteredReviews = remember(reviews, selectedFilter) {
        if (selectedFilter == "all") {
            reviews
        } else {
            reviews.filter { it.insuranceId == selectedFilter }
        }
    }
    
    // Get unique insurances for filter
    val insurances = remember(reviews) {
        reviews.map { it.insuranceName to it.insuranceId }
            .distinctBy { it.second }
            .sortedBy { it.first }
    }
    
    // Show statistics bottom sheet
    if (showStatsBottomSheet && stats != null) {
        ModalBottomSheet(
            onDismissRequest = { showStatsBottomSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Statistiques globales",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                RatingStatsDisplay(
                    averageRating = stats!!.averageRating,
                    totalReviews = stats!!.totalReviews,
                    ratingDistribution = stats!!.ratingDistribution
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Avis clients",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${reviews.size} avis reçus",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {
                    // Statistics button
                    if (stats != null && stats!!.totalReviews > 0) {
                        IconButton(onClick = { showStatsBottomSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Statistiques"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                error != null -> {
                    // Error state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "❌",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error ?: "Une erreur s'est produite",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadAgencyReviews() }) {
                            Text("Réessayer")
                        }
                    }
                }
                
                reviews.isEmpty() -> {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Aucun avis pour le moment",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Les avis de vos clients apparaîtront ici",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Stats summary card
                        stats?.let { statsData ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Average rating
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = String.format("%.1f", statsData.averageRating),
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        StarRatingDisplay(
                                            rating = statsData.averageRating,
                                            starSize = 18,
                                            showRatingValue = false
                                        )
                                        Text(
                                            text = "Note moyenne",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                    
                                    VerticalDivider(
                                        modifier = Modifier.height(60.dp),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                                    )
                                    
                                    // Total reviews
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = statsData.totalReviews.toString(),
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "Avis reçus",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Filter chips
                        if (insurances.size > 1) {
                            LazyColumn(
                                modifier = Modifier.padding(vertical = 12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FilterChip(
                                            selected = selectedFilter == "all",
                                            onClick = { selectedFilter = "all" },
                                            label = { Text("Toutes (${reviews.size})") },
                                            leadingIcon = if (selectedFilter == "all") {
                                                { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                                            } else null
                                        )
                                        
                                        insurances.take(2).forEach { (name, id) ->
                                            val count = reviews.count { it.insuranceId == id }
                                            FilterChip(
                                                selected = selectedFilter == id,
                                                onClick = { selectedFilter = id },
                                                label = { Text("$name ($count)") },
                                                leadingIcon = if (selectedFilter == id) {
                                                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                                                } else null
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Reviews list
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredReviews) { review ->
                                ReviewCard(
                                    review = review,
                                    isUserReview = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
