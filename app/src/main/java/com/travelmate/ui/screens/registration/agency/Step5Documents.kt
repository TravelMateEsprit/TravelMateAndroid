package com.travelmate.ui.screens.registration.agency

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelmate.ui.components.LoadingButton
import com.travelmate.viewmodel.AgencyFormData

@Composable
fun Step5Documents(
    formData: AgencyFormData,
    onFormDataChange: (AgencyFormData) -> Unit,
    onSubmit: () -> Unit,
    onPrevious: () -> Unit,
    isLoading: Boolean,
    isConnected: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Documents",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Documents justificatifs (optionnels)",
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Text(
            text = "Document KBIS (optionnel)",
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Vous pourrez ajouter votre document KBIS plus tard depuis votre profil.",
            fontSize = 12.sp,
            color = androidx.compose.ui.graphics.Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Note: L'upload de fichier nécessiterait une implémentation plus complexe
        // avec un sélecteur de fichiers et encodage en Base64
        // Pour le moment, nous laissons le champ optionnel vide
        
        Text(
            text = " Fonctionnalité d'upload de document à venir",
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 16.dp)
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
            
            LoadingButton(
                text = "Soumettre",
                onClick = onSubmit,
                isLoading = isLoading,
                enabled = isConnected,
                modifier = Modifier.weight(1f)
            )
        }
        
        if (!isConnected) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
