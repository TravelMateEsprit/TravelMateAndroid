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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelmate.ui.components.CustomTextField
import com.travelmate.ui.components.ValidationIcon
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.AgencyFormData
import com.travelmate.viewmodel.AgencyRegistrationViewModel

/**
 * Step 2: Agency information
 * Required: agencyName, agencyLicense, phone
 * Optional: agencyWebsite, agencyDescription
 */
@Composable
fun Step2AgencyInfo(
    formData: AgencyFormData,
    onFormDataChange: (AgencyFormData) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    viewModel: AgencyRegistrationViewModel
) {
    val focusManager = LocalFocusManager.current
    
    val isPhoneValid = remember(formData.phone) {
        formData.phone.isEmpty() || viewModel.validatePhone(formData.phone)
    }
    val isUrlValid = remember(formData.agencyWebsite) {
        viewModel.validateUrl(formData.agencyWebsite)
    }
    
    val isStepValid = remember(formData.agencyName, formData.agencyLicense, formData.phone, isPhoneValid) {
        formData.agencyName.isNotBlank() &&
        formData.agencyLicense.isNotBlank() &&
        formData.phone.isNotBlank() && isPhoneValid
    }
    
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        visible = true
    }
    
    // Calculate completion
    val fieldsCompleted = remember(formData.agencyName, formData.agencyLicense, formData.phone, formData.agencyWebsite, formData.agencyDescription) {
        listOf(
            formData.agencyName.isNotBlank(),
            formData.agencyLicense.isNotBlank(),
            formData.phone.isNotBlank() && isPhoneValid,
            formData.agencyWebsite.isNotBlank(),
            formData.agencyDescription.isNotBlank()
        ).count { it }
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
                    Icons.Default.Business,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Informations de l'agence",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Détails de votre agence",
                        fontSize = 14.sp,
                        color = ColorTextSecondary
                    )
                    if (fieldsCompleted > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ColorPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "$fieldsCompleted/5",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
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
                    containerColor = ColorSecondary.copy(alpha = 0.08f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = ColorSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Licence professionnelle requise",
                            fontSize = 13.sp,
                            color = ColorTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Assurez-vous d'avoir votre numéro de licence officielle.",
                            fontSize = 12.sp,
                            color = ColorTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        CustomTextField(
            value = formData.agencyName,
            onValueChange = { onFormDataChange(formData.copy(agencyName = it)) },
            label = "Nom de l'agence",
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CustomTextField(
            value = formData.agencyLicense,
            onValueChange = { onFormDataChange(formData.copy(agencyLicense = it)) },
            label = "Numéro de licence officielle",
            leadingIcon = {
                Icon(
                    Icons.Default.Badge,
                    contentDescription = null,
                    tint = if (formData.agencyLicense.isNotBlank()) ColorPrimary else ColorTextSecondary
                )
            },
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CustomTextField(
            value = formData.phone,
            onValueChange = { onFormDataChange(formData.copy(phone = it)) },
            label = "Téléphone professionnel",
            isError = formData.phone.isNotEmpty() && !isPhoneValid,
            errorMessage = if (formData.phone.isNotEmpty() && !isPhoneValid) "Numéro invalide" else null,
            trailingIcon = {
                if (formData.phone.isNotEmpty()) {
                    ValidationIcon(isValid = isPhoneValid)
                }
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    tint = if (formData.phone.isNotBlank() && isPhoneValid) ColorSuccess else ColorTextSecondary
                )
            },
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CustomTextField(
            value = formData.agencyWebsite,
            onValueChange = { onFormDataChange(formData.copy(agencyWebsite = it)) },
            label = "Site web (optionnel)",
            isError = formData.agencyWebsite.isNotEmpty() && !isUrlValid,
            errorMessage = if (formData.agencyWebsite.isNotEmpty() && !isUrlValid) "URL invalide" else null,
            leadingIcon = {
                Icon(
                    Icons.Default.Language,
                    contentDescription = null,
                    tint = if (formData.agencyWebsite.isNotBlank() && isUrlValid) ColorPrimary else ColorTextSecondary
                )
            },
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CustomTextField(
            value = formData.agencyDescription,
            onValueChange = { onFormDataChange(formData.copy(agencyDescription = it)) },
            label = "Description de l'agence (optionnel)",
            singleLine = false,
            maxLines = 4,
            leadingIcon = {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = if (formData.agencyDescription.isNotBlank()) ColorPrimary else ColorTextSecondary
                )
            },
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )
        
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onPrevious,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Précédent")
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Button(
                onClick = onNext,
                enabled = isStepValid,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary
                )
            ) {
                Text("Continuer")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
