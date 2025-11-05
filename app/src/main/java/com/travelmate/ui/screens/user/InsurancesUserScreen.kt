package com.travelmate.ui.screens.user

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
                Text(
                    "Nos produits",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary,
                    modifier = Modifier.padding(24.dp, 0.dp, 24.dp, 16.dp)
                )
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
                val displayList = if (selectedTabIndex == 0) insurances else mySubscriptions
                
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
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = ColorTextSecondary,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    if (selectedTabIndex == 0) "Aucune assurance disponible"
                                    else "Aucune inscription",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ColorTextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    if (selectedTabIndex == 0) "Les assurances apparaîtront ici une fois créées par les agences"
                                    else "Vous n'êtes inscrit à aucune assurance pour le moment",
                                    fontSize = 13.sp,
                                    color = ColorTextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(displayList) { insurance ->
                        InsuranceUserCard(
                            insurance = insurance,
                            onSubscribe = { viewModel.subscribeToInsurance(it) },
                            onUnsubscribe = { viewModel.unsubscribeFromInsurance(it) },
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