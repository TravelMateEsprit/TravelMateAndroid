package com.travelmate.ui.screens.agency

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.travelmate.data.models.CreatePackRequest

@Composable
fun CreatePackScreen(
    onNavigateBack: () -> Unit,
    onCreatePack: (CreatePackRequest) -> Unit
) {
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    PackFormScaffold(
        title = "Créer un pack",
        submitButtonLabel = "Créer le pack",
        initialValues = PackFormValues(),
        onNavigateBack = onNavigateBack,
        onSubmit = { values ->
            if (!isSubmitting) {
                isSubmitting = true
                onCreatePack(values.toCreateRequest())
                showSuccess = true
                isSubmitting = false
            }
        },
        submitInProgress = isSubmitting,
        successDialog = SuccessDialogState(
            visible = showSuccess,
            title = "Succès",
            message = "Le pack a été créé avec succès.",
            onConfirm = {
                showSuccess = false
                onNavigateBack()
            },
            onDismiss = { showSuccess = false }
        ),
        requireFullDetails = true
    )
}
