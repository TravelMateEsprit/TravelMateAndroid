package com.travelmate.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.travelmate.viewmodel.ResetPasswordCodeViewModel
import com.travelmate.viewmodel.ResetPasswordCodeUiState
import com.travelmate.ui.components.ModernButton
import com.travelmate.ui.theme.ColorAccent
import com.travelmate.ui.theme.ColorError

@Composable
fun NewPasswordScreen(
    navController: NavController,
    viewModel: ResetPasswordCodeViewModel = hiltViewModel()
) {
    val savedStateHandle = navController.previousBackStackEntry?.savedStateHandle
    val email = savedStateHandle?.get<String>("resetEmail") ?: ""
    val code = savedStateHandle?.get<String>("resetCode") ?: ""

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    fun onChangePassword() {
        error = ""
        if (password.length < 6) {
            error = "Le mot de passe doit contenir au moins 6 caractères."
            return
        }
        if (password != confirmPassword) {
            error = "Les mots de passe ne correspondent pas."
            return
        }
        viewModel.changePassword(email.trim(), code.trim(), password)
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ResetPasswordCodeUiState.PasswordChanged -> {
                // Afficher succès et revenir à la connexion
                navController.navigate("login") {
                    popUpTo("login") { inclusive = true }
                }
                viewModel.resetState()
            }
            is ResetPasswordCodeUiState.Error -> {
                error = (uiState as ResetPasswordCodeUiState.Error).message
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Nouveau mot de passe", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Nouveau mot de passe") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmer le mot de passe") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(20.dp))
            ModernButton(
                text = if (uiState is ResetPasswordCodeUiState.Loading) "Changement..." else "Changer le mot de passe",
                onClick = { onChangePassword() },
                icon = Icons.Default.Lock,
                isLoading = uiState is ResetPasswordCodeUiState.Loading
            )
            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(error, color = ColorError)
            }
            // Le succès est géré par la navigation, donc ce bloc est supprimé
        }
    }
}
