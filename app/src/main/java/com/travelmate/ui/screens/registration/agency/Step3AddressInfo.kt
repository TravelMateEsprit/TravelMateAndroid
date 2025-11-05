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
fun Step3AddressInfo(
    formData: AgencyFormData,
    onFormDataChange: (AgencyFormData) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    viewModel: AgencyRegistrationViewModel
) {
    val focusManager = LocalFocusManager.current
    
    val isPostalCodeValid = remember(formData.postalCode) {
        formData.postalCode.isEmpty() || viewModel.validatePostalCode(formData.postalCode)
    }
    
    val isStepValid = remember(formData.address, formData.city, formData.postalCode, formData.country, isPostalCodeValid) {
        formData.address.isNotBlank() &&
        formData.city.isNotBlank() &&
        formData.postalCode.isNotBlank() && isPostalCodeValid &&
        formData.country.isNotBlank()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Adresse de l'agence",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Localisation de votre agence",
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        CustomTextField(
            value = formData.address,
            onValueChange = { onFormDataChange(formData.copy(address = it)) },
            label = "Adresse",
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CustomTextField(
            value = formData.city,
            onValueChange = { onFormDataChange(formData.copy(city = it)) },
            label = "Ville",
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CustomTextField(
            value = formData.postalCode,
            onValueChange = { onFormDataChange(formData.copy(postalCode = it)) },
            label = "Code postal",
            isError = formData.postalCode.isNotEmpty() && !isPostalCodeValid,
            errorMessage = if (formData.postalCode.isNotEmpty() && !isPostalCodeValid) "Code postal invalide (5 chiffres)" else null,
            trailingIcon = {
                if (formData.postalCode.isNotEmpty()) {
                    ValidationIcon(isValid = isPostalCodeValid)
                }
            },
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CustomTextField(
            value = formData.country,
            onValueChange = { onFormDataChange(formData.copy(country = it)) },
            label = "Pays",
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
