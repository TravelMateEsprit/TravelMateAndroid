package com.travelmate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelmate.data.models.Review

/**
 * Bottom sheet for creating or editing a review
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReviewBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, comment: String) -> Unit,
    isLoading: Boolean = false,
    existingReview: Review? = null,
    insuranceName: String? = null,
    modifier: Modifier = Modifier
) {
    var rating by remember { mutableStateOf(existingReview?.rating ?: 0) }
    var comment by remember { mutableStateOf(existingReview?.comment ?: "") }
    var commentError by remember { mutableStateOf<String?>(null) }
    
    val isEditMode = existingReview != null
    val title = if (isEditMode) "Modifier votre avis" else "Donner votre avis"
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    insuranceName?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Rating section
            Text(
                text = "Votre note",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            StarRatingInput(
                rating = rating,
                onRatingChanged = { rating = it },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            
            if (rating == 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Veuillez sélectionner une note",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Comment section
            Text(
                text = "Votre commentaire",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = comment,
                onValueChange = { 
                    comment = it
                    // Validate on change
                    commentError = when {
                        it.length < 10 -> "Minimum 10 caractères"
                        it.length > 500 -> "Maximum 500 caractères"
                        else -> null
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                placeholder = { 
                    Text("Partagez votre expérience avec cette assurance...")
                },
                enabled = !isLoading,
                isError = commentError != null,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = commentError ?: "",
                            color = if (commentError != null) MaterialTheme.colorScheme.error 
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${comment.length}/500",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                maxLines = 8,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Submit button
            Button(
                onClick = {
                    if (rating > 0 && comment.length in 10..500) {
                        onSubmit(rating, comment)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && rating > 0 && comment.length in 10..500,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditMode) "Modifier" else "Publier",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
