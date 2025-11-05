package com.travelmate.ui.screens.registration.agency

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelmate.ui.components.CustomTextField
import com.travelmate.ui.components.ValidationIcon
import com.travelmate.viewmodel.AgencyFormData
import com.travelmate.viewmodel.AgencyRegistrationViewModel

@Composable
fun Step2AgencyInfo(
    formData: AgencyFormData,
    onFormDataChange: (AgencyFormData) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    viewModel: AgencyRegistrationViewModel
) {
    val focusManager = LocalFocusManager.current
    
    val isSiretValid = remember(formData.siret) {
        formData.siret.isEmpty() || viewModel.validateSiret(formData.siret)
    }
    val isPhoneValid = remember(formData.phone) {
        formData.phone.isEmpty() || viewModel.validatePhone(formData.phone)
    }
    val isUrlValid = remember(formData.websiteUrl) {
        viewModel.validateUrl(formData.websiteUrl)
    }
    
    val isStepValid = remember(formData.agencyName, formData.siret, formData.phone, isSiretValid, isPhoneValid) {
        formData.agencyName.isNotBlank() &&
        formData.siret.isNotBlank() && isSiretValid &&
        formData.phone.isNotBlank() && isPhoneValid
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Informations de l'agence",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Détails de votre agence",
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        CustomTextField(
            value = formData.agencyName,
            onValueChange = { onFormDataChange(formData.copy(agencyName = it)) },
            label = "Nom de l'agence",
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CustomTextField(
            value = formData.siret,
            onValueChange = { onFormDataChange(formData.copy(siret = it)) },
            label = "SIRET (14 chiffres)",
            isError = formData.siret.isNotEmpty() && !isSiretValid,
            errorMessage = if (formData.siret.isNotEmpty() && !isSiretValid) "SIRET invalide (14 chiffres)" else null,
            trailingIcon = {
                if (formData.siret.isNotEmpty()) {
                    ValidationIcon(isValid = isSiretValid)
                }
            },
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CustomTextField(
            value = formData.phone,
            onValueChange = { onFormDataChange(formData.copy(phone = it)) },
            label = "Téléphone",
            isError = formData.phone.isNotEmpty() && !isPhoneValid,
            errorMessage = if (formData.phone.isNotEmpty() && !isPhoneValid) "Numéro invalide" else null,
            trailingIcon = {
                if (formData.phone.isNotEmpty()) {
                    ValidationIcon(isValid = isPhoneValid)
                }
            },
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CustomTextField(
            value = formData.websiteUrl,
            onValueChange = { onFormDataChange(formData.copy(websiteUrl = it)) },
            label = "Site web (optionnel)",
            isError = formData.websiteUrl.isNotEmpty() && !isUrlValid,
            errorMessage = if (formData.websiteUrl.isNotEmpty() && !isUrlValid) "URL invalide" else null,
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CustomTextField(
            value = formData.description,
            onValueChange = { onFormDataChange(formData.copy(description = it)) },
            label = "Description (optionnel)",
            singleLine = false,
            maxLines = 4,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )
        
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onPrevious,
                modifier = Modifier.weight(1f)
            ) {
                Text("Précédent")
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Button(
                onClick = onNext,
                enabled = isStepValid,
                modifier = Modifier.weight(1f)
            ) {
                Text("Suivant")
            }
        }
    }
}
