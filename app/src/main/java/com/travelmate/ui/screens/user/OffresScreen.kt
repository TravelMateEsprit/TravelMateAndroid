package com.travelmate.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.travelmate.data.models.Voyage
import com.travelmate.ui.theme.*
import com.travelmate.utils.Constants
import com.travelmate.viewmodel.VoyagesViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffresScreen(
    viewModel: VoyagesViewModel = hiltViewModel(),
    onVoyageClick: (String) -> Unit = {}
) {
    val voyages by viewModel.voyages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(Unit) {
        viewModel.loadAllVoyages()
    }
    
    // Show error if any
    error?.let { errorMsg ->
        LaunchedEffect(errorMsg) {
            android.util.Log.e("OffresScreen", "Error: $errorMsg")
            snackbarHostState.showSnackbar(
                message = errorMsg,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Custom TopBar
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
                        "Offres de voyage",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = { 
                        viewModel.loadAllVoyages()
                    }) {
                        Icon(Icons.Default.Refresh, "Actualiser", tint = Color.White)
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
                                Icons.Default.Flight,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Découvrez nos offres",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Des destinations exceptionnelles vous attendent",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
                
                // Voyages List
                item {
                    Text(
                        "Nos voyages",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary,
                        modifier = Modifier.padding(24.dp, 24.dp, 24.dp, 16.dp)
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
                    if (voyages.isEmpty()) {
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
                                        Icons.Default.FlightTakeoff,
                                        contentDescription = null,
                                        tint = ColorTextSecondary,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Aucune offre disponible",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ColorTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Les offres de voyage apparaîtront ici une fois créées",
                                        fontSize = 13.sp,
                                        color = ColorTextSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(voyages) { voyage ->
                            VoyageCard(
                                voyage = voyage,
                                onClick = { onVoyageClick(voyage.id_voyage) },
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
fun VoyageCard(
    voyage: Voyage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Format dates
    val dateFormat = SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault())
    val dateDisplayFormat = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    var dateDepartText: String = voyage.date_depart
    var dateRetourText: String = voyage.date_retour
    var timeDepartText: String = ""
    var timeRetourText: String = ""
    
    try {
        val depart = dateFormat.parse(voyage.date_depart)
        val retour = dateFormat.parse(voyage.date_retour)
        
        depart?.let {
            dateDepartText = dateDisplayFormat.format(it)
            timeDepartText = timeFormat.format(it)
        }
        retour?.let {
            dateRetourText = dateDisplayFormat.format(it)
            timeRetourText = timeFormat.format(it)
        }
    } catch (e: Exception) {
        // Keep original if parsing fails
    }
    
    // Calculate places remaining (default if not in model)
    val placesRestantes = (15..45).random() // Simulated, replace with actual field when available
    
    // Get type icon
    val typeIcon = when (voyage.type.lowercase()) {
        "vol", "avion", "flight" -> Icons.Default.Flight
        "hôtel", "hotel" -> Icons.Default.Home
        "voiture", "car", "auto" -> Icons.Default.DirectionsCar
        else -> Icons.Default.Flight
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Image - Full width at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (!voyage.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = voyage.imageUrl,
                        contentDescription = voyage.destination,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(ColorPrimary, ColorSecondary),
                                    start = Offset(0f, 0f),
                                    end = Offset(1000f, 1000f)
                                )
                            )
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            typeIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }
            
            // Content section
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Destination - Big and bold
                Text(
                    text = voyage.destination,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary,
                    letterSpacing = 0.3.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Details block - Type, Dates, Places
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .background(
                            color = ColorBackground,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Type
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            typeIcon,
                            contentDescription = null,
                            tint = ColorAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = voyage.type,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ColorTextPrimary
                        )
                    }
                    
                    // Divider
                    HorizontalDivider(
                        color = ColorTextSecondary.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                    
                    // Dates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Départ
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Flight,
                                    contentDescription = null,
                                    tint = ColorPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Départ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ColorTextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = dateDepartText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorTextPrimary
                            )
                            Text(
                                text = timeDepartText,
                                fontSize = 13.sp,
                                color = ColorTextSecondary
                            )
                        }
                        
                        // Arrow
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = ColorTextSecondary.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(24.dp)
                                .padding(top = 20.dp)
                        )
                        
                        // Retour
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Retour",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ColorTextSecondary
                                )
                                Icon(
                                    Icons.Default.Flight,
                                    contentDescription = null,
                                    tint = ColorPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = dateRetourText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorTextPrimary
                            )
                            Text(
                                text = timeRetourText,
                                fontSize = 13.sp,
                                color = ColorTextSecondary
                            )
                        }
                    }
                    
                    // Divider
                    HorizontalDivider(
                        color = ColorTextSecondary.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                    
                    // Places restantes
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = if (placesRestantes < 10) ColorError else ColorAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Places restantes",
                            fontSize = 13.sp,
                            color = ColorTextSecondary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Surface(
                            color = if (placesRestantes < 10) ColorError.copy(alpha = 0.15f) 
                                    else ColorAccent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$placesRestantes",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (placesRestantes < 10) ColorError else ColorAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                    
                    // Divider
                    HorizontalDivider(
                        color = ColorTextSecondary.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                    
                    // Prix
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
                                Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = ColorAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Prix",
                                fontSize = 13.sp,
                                color = ColorTextSecondary
                            )
                        }
                        Text(
                            text = "${(voyage.prix_estime ?: 0.0).toInt()} €",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorPrimary,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Réserver button
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorPrimary
                    )
                ) {
                    Text(
                        text = "Réserver",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

