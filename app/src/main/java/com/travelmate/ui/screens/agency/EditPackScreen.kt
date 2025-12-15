package com.travelmate.ui.screens.agency

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.travelmate.data.models.Pack
import com.travelmate.data.models.UpdatePackRequest

@Composable
fun EditPackScreen(
    pack: Pack,
    onBack: () -> Unit,
    onUpdatePack: (UpdatePackRequest) -> Unit
) {
    var isSubmitting by remember { mutableStateOf(false) }

    PackFormScaffold(
        title = "Modifier le pack",
        submitButtonLabel = "Enregistrer les modifications",
        initialValues = pack.toFormValues(),
        onNavigateBack = onBack,
        onSubmit = { values ->
            if (!isSubmitting) {
                isSubmitting = true
                onUpdatePack(values.toUpdateRequest())
                isSubmitting = false
            }
        },
        submitInProgress = isSubmitting,
        requireFullDetails = false
    )
}