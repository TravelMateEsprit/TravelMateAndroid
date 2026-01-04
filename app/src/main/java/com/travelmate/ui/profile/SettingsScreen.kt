package com.travelmate.ui.profile

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.travelmate.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Screen moderne pour les paramètres avec changement de mot de passe sécurisé
 * Compatible Dark Mode et Light Mode
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var verificationCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    // Observer les changements d'état
    LaunchedEffect(uiState) {
        when (uiState) {
            is SettingsViewModel.UiState.CodeSent -> {
                snackbarHostState.showSnackbar(
                    "Code de vérification envoyé à votre email",
                    duration = SnackbarDuration.Short
                )
            }
            is SettingsViewModel.UiState.PasswordChanged -> {
                snackbarHostState.showSnackbar(
                    "Mot de passe changé avec succès",
                    duration = SnackbarDuration.Short
                )
                delay(1000)
                onNavigateBack()
            }
            is SettingsViewModel.UiState.Error -> {
                snackbarHostState.showSnackbar(
                    (uiState as SettingsViewModel.UiState.Error).message,
                    duration = SnackbarDuration.Long
                )
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Paramètres",
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface,
                    titleContentColor = colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Section Sécurité
            SecuritySection(
                colorScheme = colorScheme,
                uiState = uiState,
                verificationCode = verificationCode,
                onVerificationCodeChange = { verificationCode = it },
                newPassword = newPassword,
                onNewPasswordChange = { newPassword = it },
                confirmPassword = confirmPassword,
                onConfirmPasswordChange = { confirmPassword = it },
                showNewPassword = showNewPassword,
                onToggleNewPassword = { showNewPassword = !showNewPassword },
                showConfirmPassword = showConfirmPassword,
                onToggleConfirmPassword = { showConfirmPassword = !showConfirmPassword },
                onRequestCode = { viewModel.requestPasswordChangeCode() },
                onChangePassword = {
                    if (newPassword != confirmPassword) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Les mots de passe ne correspondent pas")
                        }
                    } else if (newPassword.length < 8) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Le mot de passe doit contenir au moins 8 caractères")
                        }
                    } else {
                        viewModel.changePasswordWithCode(verificationCode, newPassword)
                    }
                },
                focusManager = focusManager
            )
        }
    }
}

@Composable
private fun SecuritySection(
    colorScheme: ColorScheme,
    uiState: SettingsViewModel.UiState,
    verificationCode: String,
    onVerificationCodeChange: (String) -> Unit,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    showNewPassword: Boolean,
    onToggleNewPassword: () -> Unit,
    showConfirmPassword: Boolean,
    onToggleConfirmPassword: () -> Unit,
    onRequestCode: () -> Unit,
    onChangePassword: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // En-tête de section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                Icons.Outlined.Security,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Sécurité",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )
        }

        // Card moderne pour le changement de mot de passe
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    "Changer mon mot de passe",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Pour votre sécurité, nous vous enverrons un code de vérification par email",
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Étape 1: Demander le code
                AnimatedVisibility(
                    visible = uiState !is SettingsViewModel.UiState.CodeSent && 
                              uiState !is SettingsViewModel.UiState.PasswordChanged
                ) {
                    Button(
                        onClick = onRequestCode,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is SettingsViewModel.UiState.Loading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary
                        )
                    ) {
                        if (uiState is SettingsViewModel.UiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Outlined.Email, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            if (uiState is SettingsViewModel.UiState.Loading) 
                                "Envoi en cours..." 
                            else 
                                "Recevoir le code par email",
                            fontSize = 16.sp
                        )
                    }
                }

                // Étape 2: Saisir le code et le nouveau mot de passe
                AnimatedVisibility(
                    visible = uiState is SettingsViewModel.UiState.CodeSent
                ) {
                    Column {
                        // Champ code de vérification
                        OutlinedTextField(
                            value = verificationCode,
                            onValueChange = onVerificationCodeChange,
                            label = { Text("Code de vérification") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Key, null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorScheme.primary,
                                focusedLabelColor = colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Nouveau mot de passe
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = onNewPasswordChange,
                            label = { Text("Nouveau mot de passe") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Lock, null)
                            },
                            trailingIcon = {
                                IconButton(onClick = onToggleNewPassword) {
                                    Icon(
                                        if (showNewPassword) Icons.Outlined.VisibilityOff 
                                        else Icons.Outlined.Visibility,
                                        "Afficher/Masquer"
                                    )
                                }
                            },
                            visualTransformation = if (showNewPassword) 
                                VisualTransformation.None 
                            else 
                                PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorScheme.primary,
                                focusedLabelColor = colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Confirmer mot de passe
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = onConfirmPasswordChange,
                            label = { Text("Confirmer le mot de passe") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Lock, null)
                            },
                            trailingIcon = {
                                IconButton(onClick = onToggleConfirmPassword) {
                                    Icon(
                                        if (showConfirmPassword) Icons.Outlined.VisibilityOff 
                                        else Icons.Outlined.Visibility,
                                        "Afficher/Masquer"
                                    )
                                }
                            },
                            visualTransformation = if (showConfirmPassword) 
                                VisualTransformation.None 
                            else 
                                PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { 
                                    focusManager.clearFocus()
                                    onChangePassword()
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorScheme.primary,
                                focusedLabelColor = colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Bouton de validation
                        Button(
                            onClick = onChangePassword,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState !is SettingsViewModel.UiState.Loading &&
                                     verificationCode.isNotBlank() &&
                                     newPassword.isNotBlank() &&
                                     confirmPassword.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.primary
                            )
                        ) {
                            if (uiState is SettingsViewModel.UiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                if (uiState is SettingsViewModel.UiState.Loading) 
                                    "Changement en cours..." 
                                else 
                                    "Changer le mot de passe",
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Informations de sécurité
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Conseils de sécurité",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "• Utilisez au moins 8 caractères\n" +
                        "• Mélangez majuscules et minuscules\n" +
                        "• Ajoutez des chiffres et symboles\n" +
                        "• Ne réutilisez pas d'anciens mots de passe",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
