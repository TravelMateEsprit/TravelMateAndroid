package com.travelmate.ui.screens.login

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.travelmate.ui.components.*
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.ForgotPasswordUiState
import com.travelmate.viewmodel.ForgotPasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsState()
    
    var email by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    
    val isEmailValid = remember(email) {
        email.isEmpty() || viewModel.validateEmail(email)
    }
    
    val isFormValid = remember(email, isEmailValid) {
        email.isNotBlank() && isEmailValid
    }
    
    // Gérer les changements d'état
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ForgotPasswordUiState.Success -> {
                successMessage = state.message
                showSuccessDialog = true
                viewModel.resetState()
            }
            is ForgotPasswordUiState.Error -> {
                errorMessage = state.message
                showErrorDialog = true
                viewModel.resetState()
            }
            else -> {}
        }
    }
    
    // Animation d'entrée
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    
    // Dialogue de succès
    if (showSuccessDialog) {
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
                    "Email envoyé !",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    successMessage.ifEmpty { "Consultez votre boîte email pour réinitialiser votre mot de passe." },
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                ModernButton(
                    text = "OK",
                    onClick = {
                        showSuccessDialog = false
                        onNavigateBack()
                    }
                )
            }
        )
    }
    
    // Dialogue d'erreur
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
                    "Erreur",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    errorMessage,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                ModernButton(
                    text = "OK",
                    onClick = { showErrorDialog = false }
                )
            }
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(ColorPrimary, ColorPrimary.copy(alpha = 0.8f)),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
    ) {
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
                    text = "Mot de passe oublié",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Logo
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )) + fadeIn()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = com.travelmate.R.drawable.ic_launcher_foreground),
                        contentDescription = "TravelMate logo",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(60.dp))
                            .background(Color.White)
                            .padding(12.dp)
                    )
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
                        text = "Réinitialiser le mot de passe",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Entrez votre email pour recevoir un lien de réinitialisation",
                        fontSize = 14.sp,
                        color = ColorTextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Adresse email") },
                        placeholder = { Text("exemple@email.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = "Email")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (isFormValid) {
                                    focusManager.clearFocus()
                                    viewModel.sendResetEmail(email.trim())
                                }
                            }
                        ),
                        isError = !isEmailValid,
                        supportingText = {
                            if (!isEmailValid) {
                                Text("Email invalide")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Bouton d'envoi
                    ModernButton(
                        text = "Envoyer le lien",
                        onClick = {
                            viewModel.sendResetEmail(email.trim())
                        },
                        isLoading = uiState is ForgotPasswordUiState.Loading,
                        enabled = isFormValid,
                        icon = Icons.Default.Send
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Retour à la connexion
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Vous vous souvenez de votre mot de passe ? ",
                            fontSize = 14.sp,
                            color = ColorTextSecondary
                        )
                        TextButton(onClick = onNavigateBack) {
                            Text(
                                "Se connecter",
                                color = ColorAccent,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
