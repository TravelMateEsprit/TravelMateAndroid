package com.travelmate.ui.screens.user

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
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.travelmate.data.models.Voyage
import com.travelmate.ui.components.CustomTextField
import com.travelmate.ui.theme.*
import com.travelmate.utils.Constants
import com.travelmate.utils.UserPreferences
import com.travelmate.viewmodel.ReservationsViewModel
import com.travelmate.viewmodel.VoyagesViewModel
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
    var paymentMethod by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCVC by remember { mutableStateOf("") }
    var specialRequests by remember { mutableStateOf("") }
    var promoCode by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
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
            snackbarHostState.showSnackbar(
                message = displayMessage,
                duration = SnackbarDuration.Long
            )
            reservationsViewModel.clearError()
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
                    paymentMethod = paymentMethod,
                    onPaymentMethodChange = { paymentMethod = it },
                    cardNumber = cardNumber,
                    onCardNumberChange = { cardNumber = it },
                    cardExpiry = cardExpiry,
                    onCardExpiryChange = { cardExpiry = it },
                    cardCVC = cardCVC,
                    onCardCVCChange = { cardCVC = it },
                    specialRequests = specialRequests,
                    onSpecialRequestsChange = { specialRequests = it },
                    promoCode = promoCode,
                    onPromoCodeChange = { promoCode = it },
                    isLoading = isLoadingReservation,
                    snackbarHostState = snackbarHostState,
                    hasExistingReservation = existingReservation != null,
                    onConfirmReservation = {
                        // Don't allow creating if reservation already exists
                        if (existingReservation != null) {
                            return@ReservationFormCard
                        }
                        
                        val prix = voyage.prix_estime ?: 0.0
                        val nombrePersonnesInt = nombrePersonnes.toIntOrNull() ?: 1
                        val totalPrix = prix * nombrePersonnesInt
                        
                        val request = com.travelmate.data.models.CreateReservationRequest(
                            id_voyage = voyage.id_voyage,
                            prix = totalPrix
                        )
                        reservationsViewModel.createReservation(request) { result ->
                            result.fold(
                                onSuccess = {
                                    showSuccessDialog = true
                                },
                                onFailure = {
                                    // Error already shown via snackbar
                                }
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun VoyageDetailsCard(voyage: Voyage) {
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
            
            Column(modifier = Modifier.padding(20.dp)) {
                // Destination
                Text(
                    text = voyage.destination,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Dates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Départ",
                            fontSize = 12.sp,
                            color = ColorTextSecondary
                        )
                        Text(
                            text = "$dateDepartText à $timeDepartText",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ColorTextPrimary
                        )
                    }
                    
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = ColorTextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Retour",
                            fontSize = 12.sp,
                            color = ColorTextSecondary
                        )
                        Text(
                            text = "$dateRetourText à $timeRetourText",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ColorTextPrimary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Type and Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = ColorAccent.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = voyage.type,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ColorAccent,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Prix",
                            fontSize = 12.sp,
                            color = ColorTextSecondary
                        )
                        Text(
                            text = "${(voyage.prix_estime ?: 0.0).toInt()} €",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorPrimary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationFormCard(
    voyage: Voyage,
    userName: String,
    userEmail: String,
    userPhone: String,
    nombrePersonnes: String,
    onNombrePersonnesChange: (String) -> Unit,
    paymentMethod: String,
    onPaymentMethodChange: (String) -> Unit,
    cardNumber: String,
    onCardNumberChange: (String) -> Unit,
    cardExpiry: String,
    onCardExpiryChange: (String) -> Unit,
    cardCVC: String,
    onCardCVCChange: (String) -> Unit,
    specialRequests: String,
    onSpecialRequestsChange: (String) -> Unit,
    promoCode: String,
    onPromoCodeChange: (String) -> Unit,
    isLoading: Boolean,
    snackbarHostState: SnackbarHostState,
    hasExistingReservation: Boolean = false,
    onConfirmReservation: () -> Unit
) {
    val scope = rememberCoroutineScope()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Informations de réservation",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextPrimary
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // User Details (Pre-filled, read-only)
            Text(
                text = "Vos informations",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
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
                value = userPhone ?: "",
                onValueChange = {},
                label = "Téléphone",
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Number of people
            Text(
                text = "Nombre de personnes",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
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
                leadingIcon = Icons.Default.Person,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Payment Information
            Text(
                text = "Informations de paiement",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ColorTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Payment method dropdown
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = paymentMethod.ifEmpty { "Sélectionner un mode de paiement" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Mode de paiement") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorPrimary,
                        unfocusedBorderColor = ColorTextSecondary.copy(alpha = 0.5f)
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf("Carte bancaire", "PayPal", "Espèces", "Virement").forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method) },
                            onClick = {
                                onPaymentMethodChange(method)
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Card details (only show if card payment selected)
            if (paymentMethod == "Carte bancaire") {
                CustomTextField(
                    value = cardNumber,
                    onValueChange = { newValue ->
                        // Format as XXXX XXXX XXXX XXXX
                        val formatted = newValue.filter { it.isDigit() }
                            .chunked(4)
                            .joinToString(" ")
                            .take(19)
                        onCardNumberChange(formatted)
                    },
                    label = "Numéro de carte",
                    leadingIcon = Icons.Default.CreditCard,
                    placeholder = "1234 5678 9012 3456",
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CustomTextField(
                        value = cardExpiry,
                        onValueChange = { newValue ->
                            // Format as MM/YY
                            val formatted = newValue.filter { it.isDigit() }
                                .chunked(2)
                                .joinToString("/")
                                .take(5)
                            onCardExpiryChange(formatted)
                        },
                        label = "Expiration",
                        placeholder = "MM/YY",
                        modifier = Modifier.weight(1f)
                    )
                    
                    CustomTextField(
                        value = cardCVC,
                        onValueChange = { newValue ->
                            // Limit to 3 digits
                            if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                                onCardCVCChange(newValue)
                            }
                        },
                        label = "CVC",
                        placeholder = "123",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Special requests
            Text(
                text = "Demandes spéciales (optionnel)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ColorTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = specialRequests,
                onValueChange = onSpecialRequestsChange,
                label = { Text("Notes ou demandes spéciales") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ColorPrimary,
                    unfocusedBorderColor = ColorTextSecondary.copy(alpha = 0.5f)
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Promo code
            CustomTextField(
                value = promoCode,
                onValueChange = onPromoCodeChange,
                label = "Code promo (optionnel)",
                leadingIcon = Icons.Default.LocalOffer,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Total price calculation
            val totalPrice = (voyage.prix_estime ?: 0.0) * (nombrePersonnes.toIntOrNull() ?: 1)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )
                Text(
                    text = "${totalPrice.toInt()} €",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Confirm button
            Button(
                onClick = {
                    if (hasExistingReservation) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Vous avez déjà une réservation active pour ce voyage.",
                                duration = SnackbarDuration.Long
                            )
                        }
                    } else {
                        onConfirmReservation()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && !hasExistingReservation && nombrePersonnes.toIntOrNull()?.let { it > 0 } == true && paymentMethod.isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Confirmer la réservation",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

