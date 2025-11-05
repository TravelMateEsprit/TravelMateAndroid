package com.travelmate.ui.screens.registration.user

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
import com.travelmate.data.models.UserRegistrationRequest
import com.travelmate.ui.components.*
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.UserRegistrationUiState
import com.travelmate.viewmodel.UserRegistrationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRegistrationScreen(
    onNavigateBack: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    viewModel: UserRegistrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isConnected by viewModel.connectionState.collectAsState()
    val focusManager = LocalFocusManager.current
    
    // Champs du formulaire
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Étapes du formulaire
    var currentStep by remember { mutableStateOf(0) }
    val totalSteps = 3
    
    // Animation d'entrée
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    
    // Validation en temps réel
    val isEmailValid = remember(email) { 
        email.isEmpty() || viewModel.validateEmail(email)
    }
    val isPasswordValid = remember(password) { 
        password.isEmpty() || viewModel.validatePassword(password)
    }
    val doPasswordsMatch = remember(password, confirmPassword) { 
        confirmPassword.isEmpty() || password == confirmPassword
    }
    val isPhoneValid = remember(phone) { 
        phone.isEmpty() || phone.length >= 10
    }
    
    val isStep1Valid = firstName.isNotBlank() && lastName.isNotBlank() && dateOfBirth.isNotBlank()
    val isStep2Valid = email.isNotBlank() && isEmailValid && phone.isNotBlank() && isPhoneValid
    val isStep3Valid = password.isNotBlank() && isPasswordValid && doPasswordsMatch
    
    // Gérer les changements d'état
    LaunchedEffect(uiState) {
        when (uiState) {
            is UserRegistrationUiState.Success -> {
                showSuccessDialog = true
            }
            is UserRegistrationUiState.Error -> {
                errorMessage = (uiState as UserRegistrationUiState.Error).message
                showErrorDialog = true
            }
            else -> {}
        }
    }
    
    // Dialogue de succès modernisé
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
                    "Votre compte a été créé avec succès. Bienvenue sur TravelMate !",
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
    
    // Dialogue d'erreur modernisé
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
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Gradient background
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
            // Top Bar
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
                        text = "Inscription Utilisateur",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Étape ${currentStep + 1} sur $totalSteps",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Indicateur de progression
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
                                    if (step <= currentStep) ColorAccent
                                    else Color.White.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Contenu du formulaire dans une carte
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { 100 },
                    animationSpec = tween(500)
                ) + fadeIn()
            ) {
                ModernCard(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    cornerRadius = 24.dp,
                    elevation = 8.dp
                ) {
                    // Statut de connexion
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ModernConnectionStatus(isConnected = isConnected)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Contenu selon l'étape
                    when (currentStep) {
                        0 -> {
                            // Étape 1: Informations personnelles
                            ModernSectionHeader(
                                title = "Informations personnelles",
                                subtitle = "Commençons par votre identité"
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            ModernTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                label = "Prénom",
                                leadingIcon = Icons.Default.Person,
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next,
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            ModernTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                label = "Nom",
                                leadingIcon = Icons.Default.Person,
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next,
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            ModernTextField(
                                value = dateOfBirth,
                                onValueChange = { dateOfBirth = it },
                                label = "Date de naissance (JJ/MM/AAAA)",
                                leadingIcon = Icons.Default.CalendarToday,
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                )
                            )
                        }
                        
                        1 -> {
                            // Étape 2: Coordonnées
                            ModernSectionHeader(
                                title = "Coordonnées",
                                subtitle = "Comment vous contacter ?"
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            ModernTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "Email",
                                leadingIcon = Icons.Default.Email,
                                isError = email.isNotEmpty() && !isEmailValid,
                                errorMessage = if (email.isNotEmpty() && !isEmailValid) 
                                    "Format email invalide" else null,
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            ModernTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = "Téléphone",
                                leadingIcon = Icons.Default.Phone,
                                isError = phone.isNotEmpty() && !isPhoneValid,
                                errorMessage = if (phone.isNotEmpty() && !isPhoneValid) 
                                    "Format téléphone invalide" else null,
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done,
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                )
                            )
                        }
                        
                        2 -> {
                            // Étape 3: Sécurité
                            ModernSectionHeader(
                                title = "Sécurité",
                                subtitle = "Créez votre mot de passe"
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            ModernTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = "Mot de passe",
                                leadingIcon = Icons.Default.Lock,
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) 
                                                Icons.Default.Visibility 
                                            else 
                                                Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = ColorPrimary
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) 
                                    androidx.compose.ui.text.input.VisualTransformation.None 
                                else 
                                    PasswordVisualTransformation(),
                                isError = password.isNotEmpty() && !isPasswordValid,
                                errorMessage = if (password.isNotEmpty() && !isPasswordValid) 
                                    "Le mot de passe doit contenir au moins 8 caractères" else null,
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next,
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            ModernTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = "Confirmer le mot de passe",
                                leadingIcon = Icons.Default.Lock,
                                trailingIcon = {
                                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (confirmPasswordVisible) 
                                                Icons.Default.Visibility 
                                            else 
                                                Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = ColorPrimary
                                        )
                                    }
                                },
                                visualTransformation = if (confirmPasswordVisible) 
                                    androidx.compose.ui.text.input.VisualTransformation.None 
                                else 
                                    PasswordVisualTransformation(),
                                isError = confirmPassword.isNotEmpty() && !doPasswordsMatch,
                                errorMessage = if (confirmPassword.isNotEmpty() && !doPasswordsMatch) 
                                    "Les mots de passe ne correspondent pas" else null,
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Boutons de navigation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (currentStep > 0) {
                            ModernOutlineButton(
                                text = "Précédent",
                                onClick = { currentStep-- },
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.ArrowBack
                            )
                        }
                        
                        if (currentStep < totalSteps - 1) {
                            val canGoNext = when (currentStep) {
                                0 -> isStep1Valid
                                1 -> isStep2Valid
                                else -> false
                            }
                            
                            ModernButton(
                                text = "Suivant",
                                onClick = { currentStep++ },
                                enabled = canGoNext,
                                modifier = Modifier.weight(if (currentStep > 0) 1f else 1f),
                                icon = Icons.Default.ArrowForward
                            )
                        } else {
                            ModernButton(
                                text = "S'inscrire",
                                onClick = {
                                    val request = UserRegistrationRequest(
                                        email = email.trim(),
                                        password = password,
                                        firstName = firstName.trim(),
                                        lastName = lastName.trim(),
                                        phone = phone.trim(),
                                        dateOfBirth = dateOfBirth.trim()
                                    )
                                    viewModel.registerUser(request)
                                },
                                isLoading = uiState is UserRegistrationUiState.Loading,
                                enabled = isStep3Valid && isConnected,
                                modifier = Modifier.weight(if (currentStep > 0) 1f else 1f),
                                icon = Icons.Default.Check
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
