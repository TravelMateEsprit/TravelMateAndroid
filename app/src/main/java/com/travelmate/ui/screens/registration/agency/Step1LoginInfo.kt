package com.travelmate.ui.screens.registration.agency

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelmate.ui.components.CustomTextField
import com.travelmate.ui.components.LoadingButton
import com.travelmate.ui.components.ValidationIcon
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.AgencyFormData
import com.travelmate.viewmodel.AgencyRegistrationViewModel

/**
 * Step 1: Login credentials and contact person info
 * Required: name (contact person), email, password
 */
@Composable
fun Step1LoginInfo(
    formData: AgencyFormData,
    onFormDataChange: (AgencyFormData) -> Unit,
    onNext: () -> Unit,
    viewModel: AgencyRegistrationViewModel,
    isConnected: Boolean
) {
    val focusManager = LocalFocusManager.current
    
    val isEmailValid = remember(formData.email) {
        formData.email.isEmpty() || viewModel.validateEmail(formData.email)
    }
    val isPasswordValid = remember(formData.password) {
        formData.password.isEmpty() || viewModel.validatePassword(formData.password)
    }
    val doPasswordsMatch = remember(formData.password, formData.confirmPassword) {
        formData.confirmPassword.isEmpty() || formData.password == formData.confirmPassword
    }
    
    val isStepValid = remember(formData.name, formData.email, formData.password, formData.confirmPassword, isEmailValid, isPasswordValid, doPasswordsMatch) {
        formData.name.isNotBlank() &&
        formData.email.isNotBlank() && isEmailValid &&
        formData.password.isNotBlank() && isPasswordValid &&
        formData.confirmPassword.isNotBlank() && doPasswordsMatch
    }
    
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        visible = true
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header avec icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(ColorPrimary, ColorSecondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Informations de connexion",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )
                Text(
                    text = "Créez vos identifiants",
                    fontSize = 14.sp,
                    color = ColorTextSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Info Card
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + expandVertically()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ColorPrimary.copy(alpha = 0.08f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = ColorPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Ces informations seront utilisées pour vous connecter à votre compte agence.",
                        fontSize = 13.sp,
                        color = ColorTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        CustomTextField(
            value = formData.name,
            onValueChange = { onFormDataChange(formData.copy(name = it)) },
            label = "Nom complet du responsable",
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CustomTextField(
            value = formData.email,
            onValueChange = { onFormDataChange(formData.copy(email = it)) },
            label = "Email professionnel",
            isError = formData.email.isNotEmpty() && !isEmailValid,
            errorMessage = if (formData.email.isNotEmpty() && !isEmailValid) "Format email invalide" else null,
            trailingIcon = {
                if (formData.email.isNotEmpty()) {
                    ValidationIcon(isValid = isEmailValid)
                }
            },
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CustomTextField(
            value = formData.password,
            onValueChange = { onFormDataChange(formData.copy(password = it)) },
            label = "Mot de passe",
            isError = formData.password.isNotEmpty() && !isPasswordValid,
            errorMessage = if (formData.password.isNotEmpty() && !isPasswordValid) 
                "6 caractères min, 1 chiffre requis" else null,
            visualTransformation = PasswordVisualTransformation(),
            trailingIcon = {
                if (formData.password.isNotEmpty()) {
                    ValidationIcon(isValid = isPasswordValid)
                }
            },
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        
        // Password strength indicator
        if (formData.password.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                when {
                                    formData.password.length < 6 && index == 0 -> ColorError
                                    formData.password.length >= 6 && index <= 0 -> ColorSuccess
                                    formData.password.length >= 8 && index <= 1 -> ColorSuccess
                                    formData.password.length >= 12 && index <= 2 -> ColorSuccess
                                    else -> ColorTextSecondary.copy(alpha = 0.2f)
                                }
                            )
                    )
                }
            }
            Text(
                text = when {
                    formData.password.length < 6 -> "Faible"
                    formData.password.length < 8 -> "Moyen"
                    formData.password.length < 12 -> "Bon"
                    else -> "Excellent"
                },
                fontSize = 12.sp,
                color = when {
                    formData.password.length < 6 -> ColorError
                    formData.password.length < 8 -> ColorWarning
                    else -> ColorSuccess
                },
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CustomTextField(
            value = formData.confirmPassword,
            onValueChange = { onFormDataChange(formData.copy(confirmPassword = it)) },
            label = "Confirmer le mot de passe",
            isError = formData.confirmPassword.isNotEmpty() && !doPasswordsMatch,
            errorMessage = if (formData.confirmPassword.isNotEmpty() && !doPasswordsMatch)
                "Les mots de passe ne correspondent pas" else null,
            visualTransformation = PasswordVisualTransformation(),
            trailingIcon = {
                if (formData.confirmPassword.isNotEmpty()) {
                    ValidationIcon(isValid = doPasswordsMatch)
                }
            },
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )
        
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))
        
        // Connection status
        if (!isConnected) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ColorWarning.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = ColorWarning,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Connexion en cours...",
                        fontSize = 13.sp,
                        color = ColorWarning
                    )
                }
            }
        }
        
        LoadingButton(
            text = "Continuer",
            onClick = onNext,
            enabled = isStepValid && isConnected,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
