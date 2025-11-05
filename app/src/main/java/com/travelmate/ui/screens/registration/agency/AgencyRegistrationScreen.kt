package com.travelmate.ui.screens.registration.agency

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.travelmate.ui.components.*
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.AgencyRegistrationUiState
import com.travelmate.viewmodel.AgencyRegistrationViewModel

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
    
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    
    val totalSteps = 5
    
    val isStep1Valid = formData.agencyName.isNotBlank() && formData.description.isNotBlank()
    val isStep2Valid = formData.email.isNotBlank() && formData.phone.isNotBlank() && 
                       formData.address.isNotBlank() && formData.city.isNotBlank() && formData.country.isNotBlank()
    val isStep3Valid = formData.siret.isNotBlank() && formData.legalRepFirstName.isNotBlank() && formData.legalRepLastName.isNotBlank()
    val isStep4Valid = formData.password.isNotBlank() && formData.password == formData.confirmPassword && 
                       formData.password.length >= 8
    
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
                    if (!isConnected) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            ModernConnectionStatus(isConnected = isConnected)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    
                    when (currentStep) {
                        1 -> {
                            ModernSectionHeader(
                                title = "Informations de l'agence",
                                subtitle = "Détails de votre agence"
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            ModernTextField(
                                value = formData.agencyName,
                                onValueChange = { viewModel.updateFormData { copy(agencyName = it) } },
                                label = "Nom de l'agence",
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next,
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ModernTextField(
                                value = formData.description,
                                onValueChange = { viewModel.updateFormData { copy(description = it) } },
                                label = "Description",
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done,
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                            )
                        }
                        2 -> {
                            ModernSectionHeader(
                                title = "Coordonnées",
                                subtitle = "Informations de contact"
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            ModernTextField(
                                value = formData.email,
                                onValueChange = { viewModel.updateFormData { copy(email = it) } },
                                label = "Email",
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ModernTextField(
                                value = formData.phone,
                                onValueChange = { viewModel.updateFormData { copy(phone = it) } },
                                label = "Téléphone",
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next,
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ModernTextField(
                                value = formData.address,
                                onValueChange = { viewModel.updateFormData { copy(address = it) } },
                                label = "Adresse",
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next,
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ModernTextField(
                                value = formData.city,
                                onValueChange = { viewModel.updateFormData { copy(city = it) } },
                                label = "Ville",
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next,
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ModernTextField(
                                value = formData.country,
                                onValueChange = { viewModel.updateFormData { copy(country = it) } },
                                label = "Pays",
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done,
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                            )
                        }
                        3 -> {
                            ModernSectionHeader(
                                title = "Informations légales",
                                subtitle = "Licences et certifications"
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            ModernTextField(
                                value = formData.siret,
                                onValueChange = { viewModel.updateFormData { copy(siret = it) } },
                                label = "Numéro SIRET",
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next,
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ModernTextField(
                                value = formData.legalRepFirstName,
                                onValueChange = { viewModel.updateFormData { copy(legalRepFirstName = it) } },
                                label = "Prénom du représentant légal",
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next,
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ModernTextField(
                                value = formData.legalRepLastName,
                                onValueChange = { viewModel.updateFormData { copy(legalRepLastName = it) } },
                                label = "Nom du représentant légal",
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done,
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                            )
                        }
                        4 -> {
                            ModernSectionHeader(
                                title = "Sécurité",
                                subtitle = "Créez votre mot de passe"
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            ModernTextField(
                                value = formData.password,
                                onValueChange = { viewModel.updateFormData { copy(password = it) } },
                                label = "Mot de passe",
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = ColorPrimary
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                                isError = formData.password.isNotEmpty() && formData.password.length < 8,
                                errorMessage = if (formData.password.isNotEmpty() && formData.password.length < 8) "Le mot de passe doit contenir au moins 8 caractères" else null,
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next,
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ModernTextField(
                                value = formData.confirmPassword,
                                onValueChange = { viewModel.updateFormData { copy(confirmPassword = it) } },
                                label = "Confirmer le mot de passe",
                                trailingIcon = {
                                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = ColorPrimary
                                        )
                                    }
                                },
                                visualTransformation = if (confirmPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                                isError = formData.confirmPassword.isNotEmpty() && formData.password != formData.confirmPassword,
                                errorMessage = if (formData.confirmPassword.isNotEmpty() && formData.password != formData.confirmPassword) "Les mots de passe ne correspondent pas" else null,
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                            )
                        }
                        5 -> {
                            ModernSectionHeader(
                                title = "Documents",
                                subtitle = "Téléchargez vos documents (optionnel)"
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Les documents peuvent être ajoutés plus tard depuis votre profil.",
                                fontSize = 14.sp,
                                color = ColorTextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (currentStep > 1) {
                            ModernOutlineButton(
                                text = "Précédent",
                                onClick = { viewModel.previousStep() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        if (currentStep < totalSteps) {
                            val canGoNext = when (currentStep) {
                                1 -> isStep1Valid
                                2 -> isStep2Valid
                                3 -> isStep3Valid
                                4 -> isStep4Valid
                                else -> true
                            }
                            
                            ModernButton(
                                text = "Suivant",
                                onClick = { viewModel.nextStep() },
                                enabled = canGoNext,
                                modifier = Modifier.weight(if (currentStep > 1) 1f else 1f)
                            )
                        } else {
                            ModernButton(
                                text = "S'inscrire",
                                onClick = { viewModel.registerAgency() },
                                isLoading = uiState is AgencyRegistrationUiState.Loading,
                                enabled = isStep4Valid && isConnected,
                                modifier = Modifier.weight(if (currentStep > 1) 1f else 1f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
