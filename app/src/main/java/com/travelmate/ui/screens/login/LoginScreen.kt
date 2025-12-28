package com.travelmate.ui.screens.login

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.travelmate.viewmodel.LoginUiState
import com.travelmate.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit,
    onLoginSuccess: (userType: String) -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val isEmailValid = remember(email) {
        email.isEmpty() || viewModel.validateEmail(email)
    }
    
    val isFormValid = remember(email, password, isEmailValid) {
        email.isNotBlank() && isEmailValid && password.isNotBlank()
    }
    
    // Animation d'entrée
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    
    // Gérer les changements d'état
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LoginUiState.Success -> {
                // Get user type from response
                val userType = state.response.user?.userType ?: "user"
                android.util.Log.d("LoginScreen", "Login successful, userType: $userType")
                
                // Check if admin is trying to login
                if (userType.lowercase() == "admin") {
                    errorMessage = "Les comptes administrateurs n'ont pas accès à l'application mobile. Veuillez utiliser l'interface web."
                    showErrorDialog = true
                    viewModel.resetState()
                } else {
                    showSuccessDialog = true
                }
            }
            is LoginUiState.Error -> {
                errorMessage = state.message
                showErrorDialog = true
            }
            else -> {}
        }
    }
    
    // Dialogue de succès modernisé
    if (showSuccessDialog) {
        val userType = (uiState as? LoginUiState.Success)?.response?.user?.userType ?: "user"
        
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ColorSuccess,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Connexion réussie !",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Bienvenue sur TravelMate.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                ModernButton(
                    text = "Continuer",
                    onClick = {
                        showSuccessDialog = false
                        viewModel.resetState()
                        onLoginSuccess(userType)
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
                    "Erreur de connexion",
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
                .fillMaxHeight(0.35f)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(ColorPrimary, ColorSecondary),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar avec bouton retour
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
                
                Text(
                    text = "Connexion",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // --- SECTION LOGO FIXÉE ---
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(animationSpec = spring(Spring.DampingRatioMediumBouncy)) + fadeIn()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(28.dp)), // <-- MODIFICATION ICI : Arrondi des coins
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = com.travelmate.R.drawable.logo_travelmate),
                            contentDescription = "TravelMate logo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Card principal avec formulaire
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
                    Text(
                        text = "Bon retour !",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Connectez-vous pour continuer",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Statut de connexion
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Connection status removed - now using HTTP REST API
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Email
                    ModernTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Adresse email",
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
                    
                    // Mot de passe
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
                                    contentDescription = if (passwordVisible) 
                                        "Masquer" else "Afficher",
                                    tint = ColorPrimary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) 
                            androidx.compose.ui.text.input.VisualTransformation.None 
                        else 
                            PasswordVisualTransformation(),
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (isFormValid) {
                                    viewModel.login(email.trim(), password)
                                }
                            }
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Mot de passe oublié
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onNavigateToForgotPassword) {
                            Text(
                                "Mot de passe oublié ?",
                                color = ColorAccent,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Bouton de connexion
                    ModernButton(
                        text = "Se connecter",
                        onClick = {
                            viewModel.login(email.trim(), password)
                        },
                        isLoading = uiState is LoginUiState.Loading,
                        enabled = isFormValid,
                        icon = Icons.Default.Login
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Connection warning removed - using HTTP REST API
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
