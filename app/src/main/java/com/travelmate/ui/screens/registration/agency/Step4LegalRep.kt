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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelmate.ui.components.CustomTextField
import com.travelmate.viewmodel.AgencyFormData

@Composable
fun Step4LegalRep(
    formData: AgencyFormData,
    onFormDataChange: (AgencyFormData) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    
    val isStepValid = remember(formData.legalRepFirstName, formData.legalRepLastName) {
        formData.legalRepFirstName.isNotBlank() &&
        formData.legalRepLastName.isNotBlank()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Représentant légal",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Informations du représentant légal de l'agence",
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        CustomTextField(
            value = formData.legalRepFirstName,
            onValueChange = { onFormDataChange(formData.copy(legalRepFirstName = it)) },
            label = "Prénom du représentant légal",
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CustomTextField(
            value = formData.legalRepLastName,
            onValueChange = { onFormDataChange(formData.copy(legalRepLastName = it)) },
            label = "Nom du représentant légal",
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
