package com.travelmate.ui.screens.registration.agency

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelmate.ui.components.CustomTextField
import com.travelmate.ui.components.LoadingButton
import com.travelmate.ui.components.ValidationIcon
import com.travelmate.viewmodel.AgencyFormData
import com.travelmate.viewmodel.AgencyRegistrationViewModel

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
    
    val isStepValid = remember(formData.email, formData.password, formData.confirmPassword, isEmailValid, isPasswordValid, doPasswordsMatch) {
        formData.email.isNotBlank() && isEmailValid &&
        formData.password.isNotBlank() && isPasswordValid &&
        formData.confirmPassword.isNotBlank() && doPasswordsMatch
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Informations de connexion",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Créez vos identifiants de connexion",
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        CustomTextField(
            value = formData.email,
            onValueChange = { onFormDataChange(formData.copy(email = it)) },
            label = "Email",
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
                "8 caractères min, 1 majuscule, 1 chiffre" else null,
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
        
        LoadingButton(
            text = "Suivant",
            onClick = onNext,
            enabled = isStepValid && isConnected,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
