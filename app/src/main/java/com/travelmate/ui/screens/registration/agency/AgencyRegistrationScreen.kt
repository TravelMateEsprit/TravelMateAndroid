package com.travelmate.ui.screens.registration.agency

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.travelmate.ui.components.*
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.AgencyRegistrationUiState
import com.travelmate.viewmodel.AgencyRegistrationViewModel

/**
 * Agency registration screen with 3 steps matching backend requirements:
 * Step 1: Contact person info and login credentials (name, email, password)
 * Step 2: Agency information (agencyName, agencyLicense, phone, agencyWebsite, agencyDescription)
 * Step 3: Address (address, city, country)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyRegistrationScreen(
    onNavigateBack: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    viewModel: AgencyRegistrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val formData by viewModel.formData.collectAsState()
    val isConnected by viewModel.connectionState.collectAsState()
    val focusManager = LocalFocusManager.current
    
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
        viewModel.connectSocket()
    }
    
    val totalSteps = 3
    
    LaunchedEffect(uiState) {
        when (uiState) {
            is AgencyRegistrationUiState.Success -> {
                showSuccessDialog = true
            }
            is AgencyRegistrationUiState.Error -> {
                errorMessage = (uiState as AgencyRegistrationUiState.Error).message
                showErrorDialog = true
            }
            else -> {}
        }
    }
    
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ColorSuccess,
                    modifier = Modifier.size(56.dp)
                )
            },
            title = {
                Text(
                    "Inscription réussie !",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = ColorTextPrimary
                )
            },
            text = {
                Text(
                    "Votre agence a été enregistrée. Bienvenue sur TravelMate !",
                    color = ColorTextSecondary,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                ModernButton(
                    text = "Commencer",
                    onClick = {
                        showSuccessDialog = false
                        viewModel.resetState()
                        onRegistrationSuccess()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
    
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            icon = {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = ColorError,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Erreur d'inscription",
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )
            },
            text = {
                Text(
                    errorMessage,
                    color = ColorTextSecondary,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                ModernButton(
                    text = "Réessayer",
                    onClick = {
                        showErrorDialog = false
                        viewModel.resetState()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = ColorError
                )
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(ColorPrimary, ColorSecondary),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 800f)
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "Inscription Agence",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Étape $currentStep sur $totalSteps",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + expandVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(totalSteps) { step ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (step < currentStep) ColorAccent
                                    else Color.White.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(500)) + fadeIn()
            ) {
                ModernCard(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    cornerRadius = 24.dp,
                    elevation = 8.dp
                ) {
                    when (currentStep) {
                        1 -> {
                            Step1LoginInfo(
                                formData = formData,
                                onFormDataChange = { newData -> viewModel.updateFormData { newData } },
                                onNext = { viewModel.nextStep() },
                                viewModel = viewModel,
                                isConnected = isConnected
                            )
                        }
                        2 -> {
                            Step2AgencyInfo(
                                formData = formData,
                                onFormDataChange = { newData -> viewModel.updateFormData { newData } },
                                onNext = { viewModel.nextStep() },
                                onPrevious = { viewModel.previousStep() },
                                viewModel = viewModel
                            )
                        }
                        3 -> {
                            Step3AddressInfo(
                                formData = formData,
                                onFormDataChange = { newData -> viewModel.updateFormData { newData } },
                                onSubmit = { viewModel.registerAgency() },
                                onPrevious = { viewModel.previousStep() },
                                viewModel = viewModel,
                                uiState = uiState
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
