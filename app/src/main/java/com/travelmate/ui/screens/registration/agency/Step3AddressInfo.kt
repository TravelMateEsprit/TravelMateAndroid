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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelmate.ui.components.CustomTextField
import com.travelmate.ui.components.LoadingButton
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.AgencyFormData
import com.travelmate.viewmodel.AgencyRegistrationViewModel
import com.travelmate.viewmodel.AgencyRegistrationUiState

/**
 * Step 3: Address information  
 * Required: address, city, country
 */
@Composable
fun Step3AddressInfo(
    formData: AgencyFormData,
    onFormDataChange: (AgencyFormData) -> Unit,
    onSubmit: () -> Unit,
    onPrevious: () -> Unit,
    viewModel: AgencyRegistrationViewModel,
    uiState: AgencyRegistrationUiState
) {
    val focusManager = LocalFocusManager.current
    
    val isStepValid = remember(formData.address, formData.city, formData.country) {
        formData.address.isNotBlank() &&
        formData.city.isNotBlank() &&
        formData.country.isNotBlank()
    }
    
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        visible = true
    }
    
    // Calculate completion
    val fieldsCompleted = remember(formData.address, formData.city, formData.country) {
        listOf(
            formData.address.isNotBlank(),
            formData.city.isNotBlank(),
            formData.country.isNotBlank()
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
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Adresse de l'agence",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Dernière étape !",
                        fontSize = 14.sp,
                        color = ColorTextSecondary
                    )
                    if (fieldsCompleted > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ColorSuccess.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "$fieldsCompleted/3",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorSuccess,
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
                    containerColor = ColorSuccess.copy(alpha = 0.08f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ColorSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Presque terminé !",
                            fontSize = 13.sp,
                            color = ColorTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Ajoutez l'adresse physique de votre agence pour finaliser l'inscription.",
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
            value = formData.address,
            onValueChange = { onFormDataChange(formData.copy(address = it)) },
            label = "Adresse complète",
            leadingIcon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = null,
                    tint = if (formData.address.isNotBlank()) ColorPrimary else ColorTextSecondary
                )
            },
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CustomTextField(
            value = formData.city,
            onValueChange = { onFormDataChange(formData.copy(city = it)) },
            label = "Ville",
            leadingIcon = {
                Icon(
                    Icons.Default.LocationCity,
                    contentDescription = null,
                    tint = if (formData.city.isNotBlank()) ColorPrimary else ColorTextSecondary
                )
            },
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CustomTextField(
            value = formData.country,
            onValueChange = { onFormDataChange(formData.copy(country = it)) },
            label = "Pays",
            leadingIcon = {
                Icon(
                    Icons.Default.Public,
                    contentDescription = null,
                    tint = if (formData.country.isNotBlank()) ColorPrimary else ColorTextSecondary
                )
            },
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Progress summary
        if (isStepValid) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ColorPrimary.copy(alpha = 0.05f)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(ColorPrimary, ColorSecondary)
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = ColorSuccess,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Tout est prêt !",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorTextPrimary
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = ColorSuccess.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "100%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorSuccess,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Cliquez sur S'inscrire pour créer votre compte agence.",
                            fontSize = 12.sp,
                            color = ColorTextSecondary
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
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
            
            LoadingButton(
                text = "S'inscrire",
                onClick = onSubmit,
                enabled = isStepValid,
                isLoading = uiState is AgencyRegistrationUiState.Loading,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
