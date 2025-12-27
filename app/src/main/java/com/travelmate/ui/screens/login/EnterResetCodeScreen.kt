package com.travelmate.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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
import com.travelmate.ui.theme.ColorSuccess

@Composable
fun EnterResetCodeScreen(
    navController: NavController,
    viewModel: ResetPasswordCodeViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    fun onValidate() {
        error = ""
        if (email.isBlank() || code.length != 6) {
            error = "Veuillez saisir un email et un code à 6 chiffres."
            return
        }
        viewModel.verifyCode(email.trim(), code.trim())
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ResetPasswordCodeUiState.CodeVerified -> {
                // Stocker pour la prochaine étape
                navController.currentBackStackEntry?.savedStateHandle?.set("resetEmail", email)
                navController.currentBackStackEntry?.savedStateHandle?.set("resetCode", code)
                navController.navigate("new_password")
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
            Text("Vérification du code", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Adresse email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = code,
                onValueChange = { if (it.length <= 6) code = it },
                label = { Text("Code reçu par email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(20.dp))
            ModernButton(
                text = if (uiState is ResetPasswordCodeUiState.Loading) "Vérification..." else "Valider le code",
                onClick = { onValidate() },
                icon = Icons.Default.CheckCircle,
                isLoading = uiState is ResetPasswordCodeUiState.Loading
            )
            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(error, color = ColorError)
            }
        }
    }
}
