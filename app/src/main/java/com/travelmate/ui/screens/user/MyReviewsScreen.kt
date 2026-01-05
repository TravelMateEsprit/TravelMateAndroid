package com.travelmate.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.travelmate.ui.components.*
import com.travelmate.viewmodel.ReviewViewModel

/**
 * Screen showing all reviews created by the current user
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReviewsScreen(
    navController: NavController,
    reviewViewModel: ReviewViewModel = hiltViewModel()
) {
    val myReviews by reviewViewModel.myReviews.collectAsState()
    val isLoading by reviewViewModel.isLoading.collectAsState()
    val error by reviewViewModel.error.collectAsState()
    
    var showEditBottomSheet by remember { mutableStateOf(false) }
    var selectedReviewId by remember { mutableStateOf<String?>(null) }
    var selectedInsuranceId by remember { mutableStateOf<String?>(null) }
    var selectedInsuranceName by remember { mutableStateOf<String?>(null) }
    var selectedRating by remember { mutableStateOf(0) }
    var selectedComment by remember { mutableStateOf("") }
    
    // Load user's reviews on screen open
    LaunchedEffect(Unit) {
        reviewViewModel.loadMyReviews()
    }
    
    // Show edit bottom sheet
    if (showEditBottomSheet) {
        CreateReviewBottomSheet(
            onDismiss = { showEditBottomSheet = false },
            onSubmit = { rating, comment ->
                selectedReviewId?.let { reviewId ->
                    selectedInsuranceId?.let { insuranceId ->
                        reviewViewModel.updateReview(reviewId, insuranceId, rating, comment)
                    }
                }
                showEditBottomSheet = false
            },
            isLoading = reviewViewModel.isSubmitting.collectAsState().value,
            existingReview = myReviews.find { it._id == selectedReviewId },
            insuranceName = selectedInsuranceName
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Mes avis",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${myReviews.size} avis publiés",
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
                        Button(onClick = { reviewViewModel.loadMyReviews() }) {
                            Text("Réessayer")
                        }
                    }
                }
                
                myReviews.isEmpty() -> {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
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
                            text = "Donnez votre avis sur les assurances auxquelles vous êtes inscrit",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                
                else -> {
                    // Reviews list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(myReviews) { review ->
                            ReviewCard(
                                review = review,
                                isUserReview = true,
                                onEditClick = {
                                    selectedReviewId = review._id
                                    selectedInsuranceId = review.insuranceId
                                    selectedInsuranceName = review.insuranceName
                                    selectedRating = review.rating
                                    selectedComment = review.comment
                                    showEditBottomSheet = true
                                },
                                onDeleteClick = {
                                    reviewViewModel.deleteReview(
                                        review._id,
                                        review.insuranceId
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
