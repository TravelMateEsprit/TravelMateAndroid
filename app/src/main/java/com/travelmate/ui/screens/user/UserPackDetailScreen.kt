package com.travelmate.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.travelmate.data.models.Pack
import com.travelmate.ui.components.ModernCard
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.UserPacksViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

/**
 * Pack detail screen for users with action buttons at bottom
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPackDetailScreen(
    packId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String, String, String?) -> Unit, // agencyId, packId, packName
    onNavigateToConversations: () -> Unit,
    viewModel: UserPacksViewModel = hiltViewModel()
) {
    val packs by viewModel.availablePacks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val pack = packs.find { it.id == packId }
    var isFavorite by remember { mutableStateOf(false) }
    var showReserveDialog by remember { mutableStateOf(false) }
    var showChatError by remember { mutableStateOf(false) }
    var adults by remember { mutableStateOf(1) }
    var children by remember { mutableStateOf(0) }
    
    // Calculate total price: base price * adults + (base price * 0.5 * children)
    val totalPrice = remember(adults, children, pack?.prix) {
        pack?.prix?.let { basePrice ->
            (basePrice * adults) + (basePrice * 0.5 * children)
        } ?: 0.0
    }

    LaunchedEffect(packId) {
        if (pack == null) {
            viewModel.loadAvailablePacks()
        }
        isFavorite = viewModel.isFavorite(packId)
    }
    
    // Reload pack if it's null or missing agenceId
    LaunchedEffect(pack) {
        if (pack != null) {
            Log.d("UserPackDetailScreen", "Pack loaded: id=${pack.id}, agenceId='${pack.agenceId}', titre='${pack.titre}'")
            if (pack.agenceId.isBlank()) {
                Log.w("UserPackDetailScreen", "Pack ${pack.id} has empty agenceId, reloading packs...")
                viewModel.loadAvailablePacks()
            }
        }
    }

    // Chat error dialog
    if (showChatError) {
        AlertDialog(
            onDismissRequest = { showChatError = false },
            title = { Text("Erreur", fontWeight = FontWeight.Bold, color = ColorError) },
            text = { 
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Impossible d'ouvrir la conversation.")
                    if (pack != null) {
                        Text("Pack ID: ${pack.id}", fontSize = 12.sp, color = Color.Gray)
                        Text("Agence ID: ${pack.agenceId ?: "Non disponible"}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showChatError = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Reserve dialog
    if (showReserveDialog && pack != null) {
        AlertDialog(
            onDismissRequest = { showReserveDialog = false },
            title = { Text("Confirmer la réservation", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Pack: ${pack.titre}", fontWeight = FontWeight.Bold)
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Prix par personne:")
                        Text("${pack.prix.toInt()} DT", fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Adultes: $adults")
                        Text("${(pack.prix * adults).toInt()} DT", fontWeight = FontWeight.Medium)
                    }
                    if (children > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Enfants: $children")
                            Text("${(pack.prix * 0.5 * children).toInt()} DT", fontWeight = FontWeight.Medium)
                        }
                    }
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("${totalPrice.toInt()} DT", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ColorPrimary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reservePack(packId)
                        showReserveDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                ) {
                    Text("Confirmer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReserveDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    var showReservationSheet by remember { mutableStateOf(false) }
    val reservationFormState = remember { ReservationFormState() }

    Scaffold(
        topBar = {
            PackDetailTopBar(
                onNavigateBack = onNavigateBack,
                pack = pack,
                isFavorite = isFavorite,
                onToggleFavorite = {
                    if (isFavorite) {
                        viewModel.removeFromFavorites(packId)
                        isFavorite = false
                    } else {
                        viewModel.addToFavorites(packId)
                        isFavorite = true
                    }
                }
            )
        },
        bottomBar = {
            if (pack != null) {
                Surface(
                    shadowElevation = 16.dp,
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Discuss button
                        OutlinedButton(
                            onClick = {
                                if (pack.agenceId.isNotBlank()) {
                                    onNavigateToChat(pack.agenceId, pack.id, pack.titre)
                                } else {
                                    showChatError = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ColorPrimary
                            ),
                            border = BorderStroke(1.dp, ColorPrimary)
                        ) {
                            Icon(
                                Icons.Default.ChatBubble,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Discuter")
                        }

                        // Reserve button
                        Button(
                            onClick = {
                                reservationFormState.hydrateFromPack(pack)
                                showReservationSheet = true
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Réserver")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ColorPrimary)
                    }
                }
                pack == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Pack introuvable")
                    }
                }
                else -> {
                    PackDetailContent(
                        pack = pack,
                        adults = adults,
                        children = children,
                        onAdultsChange = { adults = it },
                        onChildrenChange = { children = it },
                        totalPrice = totalPrice,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }

    ReservationDialog(
        visible = showReservationSheet,
        pack = pack,
        formState = reservationFormState,
        onDismiss = { showReservationSheet = false },
        onConfirm = { enriched ->
            // Calculate final price based on selections
            val pensionPrices = mapOf(
                "Tout compris" to 150.0,
                "Demi-pension" to 100.0,
                "Petit-déjeuner" to 50.0
            )
            val transportPrices = mapOf(
                "Transfert inclus" to 80.0,
                "Location voiture" to 200.0,
                "Pas de transport" to 0.0
            )
            val hotelPrices = mapOf(
                "3*" to 0.0,
                "4*" to 50.0,
                "5*" to 150.0
            )

            val basePrice = pack?.prix ?: 0.0
            val pensionPrice = pensionPrices[enriched.pensionType] ?: 0.0
            val transportPrice = transportPrices[enriched.transportType] ?: 0.0
            val hotelPrice = hotelPrices[enriched.hotelCategory] ?: 0.0
            val totalPrice = basePrice + pensionPrice + transportPrice + hotelPrice

            val notes = buildString {
                if (enriched.country.isNotBlank()) appendLine("Pays: ${enriched.country}")
                if (enriched.region.isNotBlank()) appendLine("Région: ${enriched.region}")
                appendLine("Type pension: ${enriched.pensionType} (+${pensionPrice.toInt()} DT)")
                appendLine("Transport: ${enriched.transportType} (+${transportPrice.toInt()} DT)")
                appendLine("Catégorie hôtel: ${enriched.hotelCategory} (+${hotelPrice.toInt()} DT)")
                if (enriched.activities.isNotEmpty()) appendLine("Activités: ${enriched.activities.joinToString()}")
                if (enriched.places.isNotEmpty()) appendLine("Lieux: ${enriched.places.joinToString()}")
            }
            viewModel.reservePack(packId, notes = notes.ifBlank { null }, totalPrice = totalPrice)
        }
    )
}

@Composable
fun PackDetailContent(
    pack: Pack,
    adults: Int,
    children: Int,
    onAdultsChange: (Int) -> Unit,
    onChildrenChange: (Int) -> Unit,
    totalPrice: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Image card
        if (pack.images.isNotEmpty()) {
            ModernCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                cornerRadius = 16.dp
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(pack.images.first())
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Title and price
        ModernCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    pack.titre,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = Color.White)
                    Text(
                        pack.destination.orEmpty(),
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Prix par personne",
                    fontSize = 12.sp,
                    color = ColorTextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = ColorPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "${pack.prix.toInt()} DT / personne",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorPrimary
                    )
                }
            }
        }

        // Number of travelers selection
        ModernCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        tint = ColorPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Nombre de voyageurs",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TravelerCounter(
                        label = "Adultes",
                        value = adults,
                        onIncrement = { if (adults < 20) onAdultsChange(adults + 1) },
                        onDecrement = { if (adults > 1) onAdultsChange(adults - 1) },
                        modifier = Modifier.weight(1f)
                    )
                    TravelerCounter(
                        label = "Enfants",
                        value = children,
                        onIncrement = { if (children < 20) onChildrenChange(children + 1) },
                        onDecrement = { if (children > 0) onChildrenChange(children - 1) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Prix total",
                            fontSize = 14.sp,
                            color = ColorTextSecondary
                        )
                        Text(
                            "${totalPrice.toInt()} DT",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorPrimary
                        )
                    }
                    if (children > 0) {
                        Surface(
                            color = ColorSuccess.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Enfants: 50%",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                color = ColorSuccess,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Dates
        ModernCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = ColorPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Dates du voyage",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Début", fontSize = 12.sp, color = ColorTextSecondary)
                        Text(
                            formatDate(pack.dateDebut),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(Icons.Default.ArrowForward, null, tint = ColorTextSecondary)
                    Column {
                        Text("Fin", fontSize = 12.sp, color = ColorTextSecondary)
                        Text(
                            formatDate(pack.dateFin),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Description
        if (pack.description.isNotEmpty()) {
            ModernCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = ColorPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Description",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        pack.description,
                        fontSize = 14.sp,
                        color = ColorTextSecondary,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Places disponibles
        pack.placesDisponibles?.let { places ->
            ModernCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        tint = ColorPrimary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Places disponibles",
                            fontSize = 12.sp,
                            color = ColorTextSecondary
                        )
                        Text(
                            "$places personnes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorTextPrimary
                        )
                    }
                }
            }
        }

        // Bottom spacing for fixed buttons
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun TravelerCounter(
    label: String,
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            label,
            fontSize = 14.sp,
            color = ColorTextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    ColorPrimary.copy(alpha = 0.1f),
                    RoundedCornerShape(16.dp)
                )
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDecrement,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        ColorPrimary.copy(alpha = 0.2f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Remove,
                    null,
                    tint = ColorPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                value.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            IconButton(
                onClick = onIncrement,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        ColorPrimary.copy(alpha = 0.2f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Add,
                    null,
                    tint = ColorPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH)
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PackDetailTopBar(
    onNavigateBack: () -> Unit,
    pack: Pack?,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    TopAppBar(
        title = { Text("Détails du pack", fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
            }
        },
        actions = {
            if (pack != null) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
                        tint = if (isFavorite) ColorError else ColorTextPrimary
                    )
                }
            }
        }
    )
}

@Composable
private fun ReservationDialog(
    visible: Boolean,
    pack: Pack?,
    formState: ReservationFormState,
    onDismiss: () -> Unit,
    onConfirm: (ReservationFormState) -> Unit
) {
    if (!visible || pack == null) return

    // Calculate additional prices based on selections
    val pensionPrices = mapOf(
        "Tout compris" to 150.0,
        "Demi-pension" to 100.0,
        "Petit-déjeuner" to 50.0
    )

    val transportPrices = mapOf(
        "Transfert inclus" to 80.0,
        "Location voiture" to 200.0,
        "Pas de transport" to 0.0
    )

    val hotelPrices = mapOf(
        "3*" to 0.0,
        "4*" to 50.0,
        "5*" to 150.0
    )

    val basePrice = pack.prix
    val pensionPrice = pensionPrices[formState.pensionType] ?: 0.0
    val transportPrice = transportPrices[formState.transportType] ?: 0.0
    val hotelPrice = hotelPrices[formState.hotelCategory] ?: 0.0
    val totalAdditionalPrice = pensionPrice + transportPrice + hotelPrice
    val finalTotalPrice = basePrice + totalAdditionalPrice

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                if (formState.isValid()) {
                    onConfirm(formState.copy())
                    onDismiss()
                }
            }) {
                Text("Confirmer la réservation")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuler")
            }
        },
        title = { Text("Réserver ${pack.titre}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Price breakdown
                Surface(
                    color = ColorPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Prix de base: ${basePrice.toInt()} DT",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (pensionPrice > 0) {
                            Text(
                                "Pension (${formState.pensionType}): +${pensionPrice.toInt()} DT",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (transportPrice > 0) {
                            Text(
                                "Transport (${formState.transportType}): +${transportPrice.toInt()} DT",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (hotelPrice > 0) {
                            Text(
                                "Hôtel (${formState.hotelCategory}): +${hotelPrice.toInt()} DT",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            "Total: ${finalTotalPrice.toInt()} DT",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ColorPrimary
                        )
                    }
                }

                OutlinedTextField(
                    value = formState.country,
                    onValueChange = { formState.country = it },
                    label = { Text("Pays") },
                    leadingIcon = { Icon(Icons.Default.LocationCity, null) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = formState.region,
                    onValueChange = { formState.region = it },
                    label = { Text("Région") },
                    leadingIcon = { Icon(Icons.Default.Place, null) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = formState.startDate,
                    onValueChange = { formState.startDate = it },
                    label = { Text("Date début") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = formState.endDate,
                    onValueChange = { formState.endDate = it },
                    label = { Text("Date fin") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                    modifier = Modifier.fillMaxWidth()
                )
                SelectionChips(
                    label = "Type de pension",
                    options = listOf("Tout compris", "Demi-pension", "Petit-déjeuner"),
                    selected = formState.pensionType,
                    onSelected = { formState.pensionType = it },
                    prices = pensionPrices
                )
                SelectionChips(
                    label = "Transport",
                    options = listOf("Transfert inclus", "Location voiture", "Pas de transport"),
                    selected = formState.transportType,
                    onSelected = { formState.transportType = it },
                    prices = transportPrices
                )
                SelectionChips(
                    label = "Catégorie d'hôtel",
                    options = listOf("3*", "4*", "5*"),
                    selected = formState.hotelCategory,
                    onSelected = { formState.hotelCategory = it },
                    prices = hotelPrices
                )
                TagEditor(
                    title = "Activités",
                    placeholder = "Ajouter une activité",
                    tags = formState.activities,
                    onAdd = { formState.activities += it },
                    onRemove = { formState.activities -= it }
                )
                TagEditor(
                    title = "Lieux à visiter",
                    placeholder = "Ajouter un lieu",
                    tags = formState.places,
                    onAdd = { formState.places += it },
                    onRemove = { formState.places -= it }
                )
            }
        }
    )
}

@Composable
private fun SelectionChips(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    prices: Map<String, Double>? = null
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(options) { option ->
                val price = prices?.get(option)
                AssistChip(
                    onClick = { onSelected(option) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(option)
                            if (price != null && price > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "+${price.toInt()} DT",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ColorPrimary
                                )
                            }
                        }
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (selected == option) ColorPrimary.copy(alpha = 0.2f) else Color.Transparent
                    )
                )
            }
        }
    }
}

@Composable
private fun TagEditor(
    title: String,
    placeholder: String,
    tags: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    var current by remember { mutableStateOf("") }
    Column {
        Text(title, style = MaterialTheme.typography.labelLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = current,
                onValueChange = { current = it },
                label = { Text(placeholder) },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                val trimmed = current.trim()
                if (trimmed.isNotEmpty()) {
                    onAdd(trimmed)
                    current = ""
                }
            }) { Text("Ajouter") }
        }
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tags) { tag ->
                AssistChip(
                    onClick = { onRemove(tag) },
                    label = { Text(tag) },
                    leadingIcon = { Icon(Icons.Default.Close, contentDescription = null) }
                )
            }
        }
    }
}

private data class ReservationFormState(
    var country: String = "",
    var region: String = "",
    var startDate: String = "",
    var endDate: String = "",
    var pensionType: String = "",
    var transportType: String = "",
    var hotelCategory: String = "",
    var activities: List<String> = emptyList(),
    var places: List<String> = emptyList()
) {
    fun hydrateFromPack(pack: Pack?) {
        pack ?: return
        if (country.isBlank()) country = pack.country.orEmpty()
        if (region.isBlank()) region = pack.region.orEmpty()
        if (startDate.isBlank()) startDate = pack.dateDebut
        if (endDate.isBlank()) endDate = pack.dateFin
    }

    fun isValid(): Boolean =
        country.isNotBlank() &&
        region.isNotBlank() &&
        startDate.isNotBlank() &&
        endDate.isNotBlank() &&
        pensionType.isNotBlank() &&
        transportType.isNotBlank() &&
        hotelCategory.isNotBlank()

    fun copy(): ReservationFormState =
        ReservationFormState(
            country,
            region,
            startDate,
            endDate,
            pensionType,
            transportType,
            hotelCategory,
            activities.toList(),
            places.toList()
        )
}
