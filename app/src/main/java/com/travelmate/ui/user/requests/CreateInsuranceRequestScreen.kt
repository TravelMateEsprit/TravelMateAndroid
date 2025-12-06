package com.travelmate.ui.user.requests

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.travelmate.ui.components.DatePickerField
import com.travelmate.ui.components.ModernButton
import com.travelmate.ui.components.TravelPurposeSelector
import com.travelmate.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CreateInsuranceRequestScreen(
    navController: NavController,
    insuranceId: String,
    viewModel: CreateInsuranceRequestViewModel = hiltViewModel()
) {
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 3
    
    // Form state
    var travelerName by remember { mutableStateOf("") }
    var travelerEmail by remember { mutableStateOf("") }
    var travelerPhone by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var passportNumber by remember { mutableStateOf("") }
    var nationality by remember { mutableStateOf("Tunisienne") }
    var destination by remember { mutableStateOf("") }
    var departureDate by remember { mutableStateOf("") }
    var returnDate by remember { mutableStateOf("") }
    var travelPurpose by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    
    val state by viewModel.state.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    
    // Auto-remplir les informations utilisateur
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            if (travelerName.isEmpty()) {
                travelerName = user.name ?: ""
            }
            if (travelerEmail.isEmpty()) {
                travelerEmail = user.email
            }
            if (travelerPhone.isEmpty()) {
                travelerPhone = user.phone.orEmpty()
            }
        }
    }
    
    LaunchedEffect(state) {
        if (state is CreateRequestState.Success) {
            navController.popBackStack()
        }
    }
    
    // Validation des étapes
    val isStep1Valid = travelerName.isNotBlank() && 
                      travelerEmail.isNotBlank() && 
                      travelerPhone.isNotBlank() &&
                      dateOfBirth.isNotBlank()
    
    val isStep2Valid = passportNumber.isNotBlank() &&
                      nationality.isNotBlank() &&
                      destination.isNotBlank()
    
    val isStep3Valid = departureDate.isNotBlank() && 
                      returnDate.isNotBlank()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Demande d'inscription") },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (currentStep > 1) {
                            currentStep--
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ColorBackground)
        ) {
            // Indicateur de progression
            StepProgressIndicator(
                currentStep = currentStep,
                totalSteps = totalSteps,
                modifier = Modifier.padding(16.dp)
            )
            
            // Contenu des étapes
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { width -> width } + fadeIn() with
                                    slideOutHorizontally { width -> -width } + fadeOut()
                        } else {
                            slideInHorizontally { width -> -width } + fadeIn() with
                                    slideOutHorizontally { width -> width } + fadeOut()
                        }.using(SizeTransform(clip = false))
                    },
                    label = "step_transition"
                ) { step ->
                    when (step) {
                        1 -> Step1PersonalInfo(
                            travelerName = travelerName,
                            onTravelerNameChange = { travelerName = it },
                            travelerEmail = travelerEmail,
                            onTravelerEmailChange = { travelerEmail = it },
                            travelerPhone = travelerPhone,
                            onTravelerPhoneChange = { travelerPhone = it },
                            dateOfBirth = dateOfBirth,
                            onDateOfBirthChange = { dateOfBirth = it }
                        )
                        2 -> Step2DocumentsInfo(
                            passportNumber = passportNumber,
                            onPassportNumberChange = { passportNumber = it },
                            nationality = nationality,
                            onNationalityChange = { nationality = it },
                            destination = destination,
                            onDestinationChange = { destination = it }
                        )
                        3 -> Step3TravelDetails(
                            departureDate = departureDate,
                            onDepartureDateChange = { departureDate = it },
                            returnDate = returnDate,
                            onReturnDateChange = { returnDate = it },
                            travelPurpose = travelPurpose,
                            onTravelPurposeChange = { travelPurpose = it },
                            message = message,
                            onMessageChange = { message = it }
                        )
                    }
                }
            }
            
            // Erreur
            if (state is CreateRequestState.Error) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ColorError.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = ColorError,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            (state as CreateRequestState.Error).message,
                            color = ColorError,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            // Boutons de navigation
            NavigationButtons(
                currentStep = currentStep,
                totalSteps = totalSteps,
                onPrevious = { currentStep-- },
                onNext = { currentStep++ },
                onSubmit = {
                    viewModel.createRequest(
                        insuranceId = insuranceId,
                        travelerName = travelerName,
                        travelerEmail = travelerEmail,
                        travelerPhone = travelerPhone,
                        dateOfBirth = dateOfBirth,
                        passportNumber = passportNumber,
                        nationality = nationality,
                        destination = destination,
                        departureDate = departureDate,
                        returnDate = returnDate,
                        travelPurpose = travelPurpose.ifBlank { null },
                        message = message.ifBlank { null }
                    )
                },
                isNextEnabled = when (currentStep) {
                    1 -> isStep1Valid
                    2 -> isStep2Valid
                    3 -> isStep3Valid
                    else -> false
                },
                isLoading = state is CreateRequestState.Loading,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// ==================== COMPOSANTS D'ÉTAPES ====================

@Composable
private fun StepProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (step in 1..totalSteps) {
                StepCircle(
                    stepNumber = step,
                    isCompleted = step < currentStep,
                    isActive = step == currentStep,
                    modifier = Modifier.weight(1f)
                )
                if (step < totalSteps) {
                    StepConnector(
                        isCompleted = step < currentStep,
                        modifier = Modifier.weight(0.5f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = when (currentStep) {
                1 -> "Informations personnelles"
                2 -> "Documents & Destination"
                3 -> "Détails du voyage"
                else -> ""
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = ColorTextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Étape $currentStep sur $totalSteps",
            style = MaterialTheme.typography.bodyMedium,
            color = ColorTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StepCircle(
    stepNumber: Int,
    isCompleted: Boolean,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isCompleted -> ColorSuccess
            isActive -> ColorPrimary
            else -> ColorTextSecondary.copy(alpha = 0.3f)
        },
        animationSpec = tween(300),
        label = "step_bg_color"
    )
    
    val contentColor = if (isCompleted || isActive) Color.White else ColorTextSecondary
    
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = stepNumber.toString(),
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun StepConnector(
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = if (isCompleted) ColorSuccess else ColorTextSecondary.copy(alpha = 0.3f),
        animationSpec = tween(300),
        label = "connector_color"
    )
    
    Divider(
        modifier = modifier.padding(horizontal = 8.dp),
        thickness = 2.dp,
        color = color
    )
}

@Composable
private fun Step1PersonalInfo(
    travelerName: String,
    onTravelerNameChange: (String) -> Unit,
    travelerEmail: String,
    onTravelerEmailChange: (String) -> Unit,
    travelerPhone: String,
    onTravelerPhoneChange: (String) -> Unit,
    dateOfBirth: String,
    onDateOfBirthChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = ColorPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Vos informations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                }
                
                Divider()
                
                // Les champs nom et email sont auto-remplis
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ColorPrimary.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = ColorPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Ces informations sont pré-remplies depuis votre profil",
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorTextSecondary,
                        fontSize = 12.sp
                    )
                }
                
                OutlinedTextField(
                    value = travelerName,
                    onValueChange = onTravelerNameChange,
                    label = { Text("Nom complet *") },
                    leadingIcon = { Icon(Icons.Default.Badge, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
                OutlinedTextField(
                    value = travelerEmail,
                    onValueChange = onTravelerEmailChange,
                    label = { Text("Email *") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
                OutlinedTextField(
                    value = travelerPhone,
                    onValueChange = onTravelerPhoneChange,
                    label = { Text("Téléphone *") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("+216 XX XXX XXX") }
                )
                
                DatePickerField(
                    label = "Date de naissance *",
                    value = dateOfBirth,
                    onDateSelected = onDateOfBirthChange,
                    icon = Icons.Default.Cake,
                    placeholder = "Sélectionner votre date de naissance",
                    maxDate = Calendar.getInstance().apply { 
                        add(Calendar.YEAR, -18) 
                    }.timeInMillis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun Step2DocumentsInfo(
    passportNumber: String,
    onPassportNumberChange: (String) -> Unit,
    nationality: String,
    onNationalityChange: (String) -> Unit,
    destination: String,
    onDestinationChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = ColorPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Documents",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                }
                
                Divider()
                
                OutlinedTextField(
                    value = passportNumber,
                    onValueChange = onPassportNumberChange,
                    label = { Text("Numéro de passeport *") },
                    leadingIcon = { Icon(Icons.Default.CreditCard, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Ex: A12345678") }
                )
                
                OutlinedTextField(
                    value = nationality,
                    onValueChange = onNationalityChange,
                    label = { Text("Nationalité *") },
                    leadingIcon = { Icon(Icons.Default.Flag, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Public,
                        contentDescription = null,
                        tint = ColorPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Destination",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                }
                
                Divider()
                
                OutlinedTextField(
                    value = destination,
                    onValueChange = onDestinationChange,
                    label = { Text("Pays de destination *") },
                    leadingIcon = { Icon(Icons.Default.FlightTakeoff, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Ex: France, Italie...") }
                )
            }
        }
    }
}

@Composable
private fun Step3TravelDetails(
    departureDate: String,
    onDepartureDateChange: (String) -> Unit,
    returnDate: String,
    onReturnDateChange: (String) -> Unit,
    travelPurpose: String,
    onTravelPurposeChange: (String) -> Unit,
    message: String,
    onMessageChange: (String) -> Unit
) {
    val today = Calendar.getInstance().timeInMillis
    val departureDateMillis = remember(departureDate) {
        if (departureDate.isNotBlank()) {
            try {
                java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .parse(departureDate)?.time
            } catch (e: Exception) {
                null
            }
        } else null
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = ColorPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Dates de voyage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                }
                
                Divider()
                
                DatePickerField(
                    label = "Date de départ *",
                    value = departureDate,
                    onDateSelected = onDepartureDateChange,
                    icon = Icons.Default.FlightTakeoff,
                    placeholder = "Sélectionner la date de départ",
                    minDate = today,
                    modifier = Modifier.fillMaxWidth()
                )
                
                DatePickerField(
                    label = "Date de retour *",
                    value = returnDate,
                    onDateSelected = onReturnDateChange,
                    icon = Icons.Default.FlightLand,
                    placeholder = "Sélectionner la date de retour",
                    minDate = departureDateMillis ?: today,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TravelPurposeSelector(
                    selectedPurpose = travelPurpose,
                    onPurposeSelected = onTravelPurposeChange
                )
                
                Divider()
                
                OutlinedTextField(
                    value = message,
                    onValueChange = onMessageChange,
                    label = { Text("Message supplémentaire (optionnel)") },
                    leadingIcon = { 
                        Icon(Icons.Default.Message, null) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Des informations additionnelles...") }
                )
            }
        }
    }
}

@Composable
private fun NavigationButtons(
    currentStep: Int,
    totalSteps: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    isNextEnabled: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (currentStep > 1) {
            OutlinedButton(
                onClick = onPrevious,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Précédent", fontSize = 16.sp)
            }
        }
        
        if (currentStep < totalSteps) {
            ModernButton(
                text = "Suivant",
                onClick = onNext,
                enabled = isNextEnabled && !isLoading,
                modifier = Modifier
                    .weight(if (currentStep > 1) 1f else 1f)
                    .height(56.dp),
                icon = Icons.Default.ArrowForward
            )
        } else {
            ModernButton(
                text = if (isLoading) "Envoi..." else "Soumettre",
                onClick = onSubmit,
                enabled = isNextEnabled && !isLoading,
                isLoading = isLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                icon = Icons.Default.Send
            )
        }
    }
}
