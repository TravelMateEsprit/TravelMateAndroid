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
    val focusManager = LocalFocusManager.current
    
    // Champs du formulaire (selon le backend: name, email, password)
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
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
    
    val isFormValid = fullName.isNotBlank() && 
                     email.isNotBlank() && isEmailValid && 
                     password.isNotBlank() && isPasswordValid && 
                     doPasswordsMatch
    
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
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Votre compte a été créé. Bienvenue sur TravelMate !",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            containerColor = MaterialTheme.colorScheme.surface
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
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    errorMessage,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            containerColor = MaterialTheme.colorScheme.surface
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
                        text = "Créez votre compte",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
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
                    ModernSectionHeader(
                        title = "Informations du compte",
                        subtitle = "Remplissez vos informations"
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    ModernTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = "Nom complet",
                        leadingIcon = Icons.Default.Person,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
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
                            "Le mot de passe doit contenir au moins 6 caractères et un chiffre" else null,
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
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Bouton d'inscription
                    ModernButton(
                        text = "S'inscrire",
                        onClick = {
                            val request = UserRegistrationRequest(
                                name = fullName.trim(),
                                email = email.trim(),
                                password = password
                            )
                            viewModel.registerUser(request)
                        },
                        isLoading = uiState is UserRegistrationUiState.Loading,
                        enabled = isFormValid,
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.Check
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
