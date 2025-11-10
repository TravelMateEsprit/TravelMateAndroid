package com.travelmate.ui.screens.user

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.travelmate.data.models.Voyage
import com.travelmate.ui.components.CustomTextField
import com.travelmate.ui.theme.*
import com.travelmate.utils.UserPreferences
import com.travelmate.viewmodel.ReservationsViewModel
import com.travelmate.viewmodel.VoyagesViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationScreen(
    voyageId: String,
    onNavigateBack: () -> Unit,
    onReservationSuccess: () -> Unit,
    reservationsViewModel: ReservationsViewModel = hiltViewModel(),
    voyagesViewModel: VoyagesViewModel = hiltViewModel(),
    userPreferences: UserPreferences
) {
    val voyages by voyagesViewModel.voyages.collectAsState()
    val isLoadingVoyage by voyagesViewModel.isLoading.collectAsState()
    val isLoadingReservation by reservationsViewModel.isLoading.collectAsState()
    val error by reservationsViewModel.error.collectAsState()
    val myReservations by reservationsViewModel.myReservations.collectAsState()
    
    val voyage = remember(voyageId, voyages) {
        voyages.find { it.id_voyage == voyageId }
    }
    
    // Check if user already has a reservation for this voyage
    val existingReservation = remember(voyageId, myReservations) {
        myReservations.find { 
            it.getVoyageId() == voyageId && 
            it.statut != "annulee" // Only check active reservations
        }
    }
    
    // Form state
    var nombrePersonnes by remember { mutableStateOf("1") }
    var promoCode by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    val scope = rememberCoroutineScope()
    
    // Load voyage and reservations if not found
    LaunchedEffect(voyageId) {
        if (voyage == null && !isLoadingVoyage) {
            voyagesViewModel.getVoyageById(voyageId) {}
        }
        // Load user's reservations to check for existing reservation
        reservationsViewModel.loadMyReservations()
    }
    
    // Pre-fill user details
    val userName = remember { userPreferences.getUserName() ?: "" }
    val userEmail = remember { userPreferences.getUserEmail() ?: "" }
    val userPhone = remember { userPreferences.getUserPhone() ?: "" }
    
    // Show error if any
    error?.let { errorMsg ->
        LaunchedEffect(errorMsg) {
            // Extract detailed error message if available
            val displayMessage = when {
                errorMsg.contains("déjà", ignoreCase = true) || 
                errorMsg.contains("already", ignoreCase = true) ||
                errorMsg.contains("réservation active", ignoreCase = true) ||
                errorMsg.contains("reservation active", ignoreCase = true) -> {
                    "Vous avez déjà une réservation active pour ce voyage."
                }
                errorMsg.contains("HTTP") -> {
                    "Erreur: ${errorMsg}\nVérifiez les logs pour plus de détails."
                }
                else -> {
                    errorMsg
                }
            }
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = displayMessage,
                    duration = SnackbarDuration.Long
                )
                reservationsViewModel.clearError()
            }
        }
    }
    
    // Handle reservation success
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onReservationSuccess()
            },
            title = {
                Text(
                    text = "Réservation confirmée!",
                    fontWeight = FontWeight.Bold,
                    color = ColorPrimary
                )
            },
            text = {
                Text("Votre réservation a été créée avec succès. Vous recevrez une confirmation par email.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onReservationSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                ) {
                    Text("OK")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Réserver un voyage") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        if (isLoadingVoyage && voyage == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ColorPrimary)
            }
        } else if (voyage == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Voyage introuvable",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Retour")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Voyage Details Section
                VoyageDetailsCard(voyage = voyage)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Show warning if reservation already exists
                existingReservation?.let { reservation ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = ColorAccent.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = ColorAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Réservation existante",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Vous avez déjà une réservation ${reservation.statut} pour ce voyage.",
                                    fontSize = 14.sp,
                                    color = ColorTextSecondary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Reservation Form
                ReservationFormCard(
                    voyage = voyage,
                    userName = userName,
                    userEmail = userEmail,
                    userPhone = userPhone,
                    nombrePersonnes = nombrePersonnes,
                    onNombrePersonnesChange = { nombrePersonnes = it },
                    isLoading = isLoadingReservation,
                    hasExistingReservation = existingReservation != null
                ) {
                    // Don't allow creating if reservation already exists
                    if (existingReservation != null) {
                        return@ReservationFormCard
                    }
                    
                    val nombrePersonnesInt = nombrePersonnes.toIntOrNull() ?: 1
                    
                    // Create the reservation request with required fields only
                    val request = com.travelmate.data.models.CreateReservationRequest(
                        id_voyage = voyage.id_voyage,
                        prix = voyage.prix_estime ?: 0.0,
                        nombre_personnes = nombrePersonnesInt,
                        notes = null
                    )
                    
                    // Show loading state
                    reservationsViewModel.createReservation(request) { result ->
                        result.fold(
                            onSuccess = {
                                showSuccessDialog = true
                            },
                            onFailure = { exception ->
                                val errorMsg = exception.message ?: "Une erreur est survenue lors de la réservation"
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = errorMsg,
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
@SuppressLint("SimpleDateFormat")
fun VoyageDetailsCard(voyage: Voyage) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val dateDisplayFormat = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    var dateDepartText by remember { mutableStateOf(voyage.date_depart) }
    var dateRetourText by remember { mutableStateOf(voyage.date_retour) }
    var timeDepartText by remember { mutableStateOf("") }
    var timeRetourText by remember { mutableStateOf("") }
    
    LaunchedEffect(voyage) {
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
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Image
            if (!voyage.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = voyage.imageUrl,
                    contentDescription = voyage.destination,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(ColorPrimary, ColorSecondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Flight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
            
            // Content
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = voyage.destination,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Départ",
                            style = MaterialTheme.typography.bodySmall,
                            color = ColorTextSecondary
                        )
                        Text(
                            text = dateDepartText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (timeDepartText.isNotEmpty()) {
                            Text(
                                text = timeDepartText,
                                style = MaterialTheme.typography.bodySmall,
                                color = ColorTextSecondary
                            )
                        }
                    }
                    
                    Column {
                        Text(
                            text = "Retour",
                            style = MaterialTheme.typography.bodySmall,
                            color = ColorTextSecondary
                        )
                        Text(
                            text = dateRetourText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (timeRetourText.isNotEmpty()) {
                            Text(
                                text = timeRetourText,
                                style = MaterialTheme.typography.bodySmall,
                                color = ColorTextSecondary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Prix estimé",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ColorTextSecondary
                    )
                    Text(
                        text = "${voyage.prix_estime ?: 0} €",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ColorPrimary
                    )
                }
                
                if (!voyage.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = voyage.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ColorTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun ReservationFormCard(
    voyage: Voyage,
    userName: String,
    userEmail: String,
    userPhone: String,
    nombrePersonnes: String,
    onNombrePersonnesChange: (String) -> Unit,
    isLoading: Boolean,
    hasExistingReservation: Boolean = false,
    onConfirmReservation: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Détails de la réservation",
                style = MaterialTheme.typography.titleMedium,
                color = ColorTextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // User Information
            Text(
                text = "Vos informations",
                style = MaterialTheme.typography.titleSmall,
                color = ColorTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            CustomTextField(
                value = userName,
                onValueChange = {},
                label = "Nom complet",
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            CustomTextField(
                value = userEmail,
                onValueChange = {},
                label = "Email",
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            CustomTextField(
                value = userPhone,
                onValueChange = {},
                label = "Téléphone",
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Number of people
            Text(
                text = "Nombre de personnes",
                style = MaterialTheme.typography.titleSmall,
                color = ColorTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            CustomTextField(
                value = nombrePersonnes,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() } && (newValue.isEmpty() || newValue.toIntOrNull() != null)) {
                        onNombrePersonnesChange(newValue)
                    }
                },
                label = "Nombre de personnes",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Price summary
            Text(
                text = "Récapitulatif",
                style = MaterialTheme.typography.titleSmall,
                color = ColorTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Prix par personne",
                    color = ColorTextSecondary
                )
                Text(
                    text = "${voyage.prix_estime ?: 0} €",
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(voyage.prix_estime ?: 0.0) * (nombrePersonnes.toIntOrNull() ?: 1)} €",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ColorPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Confirm button
            Button(
                onClick = onConfirmReservation,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && !hasExistingReservation,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary,
                    disabledContainerColor = ColorPrimary.copy(alpha = 0.5f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = if (hasExistingReservation) "Réservation existante" else "Confirmer la réservation",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
