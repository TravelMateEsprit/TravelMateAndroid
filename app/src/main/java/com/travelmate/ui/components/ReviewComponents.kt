package com.travelmate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelmate.data.models.Review
import java.text.SimpleDateFormat
import java.util.*

/**
 * Card component to display a single review
 */
@Composable
fun ReviewCard(
    review: Review,
    modifier: Modifier = Modifier,
    isUserReview: Boolean = false,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with user info and rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.userName ?: "Utilisateur",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    StarRatingDisplay(
                        rating = review.rating.toDouble(),
                        starSize = 16,
                        showRatingValue = false
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = formatDateForReview(review.createdAt),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Show insurance name if this is from "My Reviews"
                    review.insuranceName?.let { insuranceName ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = insuranceName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Edit/Delete buttons for user's own reviews
                if (isUserReview && (onEditClick != null || onDeleteClick != null)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        onEditClick?.let {
                            IconButton(onClick = it) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Modifier",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        onDeleteClick?.let {
                            IconButton(onClick = it) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Supprimer",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
            
            // Comment
            if (review.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = review.comment,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
            
            // Show edited indicator if updated
            if (review.createdAt != review.updatedAt) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Modifié le ${formatDateForReview(review.updatedAt)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

/**
 * Compact review summary card (for insurance cards)
 */
@Composable
fun ReviewSummaryCard(
    averageRating: Double,
    totalReviews: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StarRatingDisplay(
                    rating = averageRating,
                    starSize = 18,
                    showRatingValue = true
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "($totalReviews)",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "Voir les avis",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Empty state when no reviews exist
 */
@Composable
fun NoReviewsState(
    modifier: Modifier = Modifier,
    message: String = "Aucun avis pour le moment"
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📝",
            fontSize = 48.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Soyez le premier à donner votre avis !",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Helper function to format date for reviews
 */
private fun formatDateForReview(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)
        
        val outputFormat = SimpleDateFormat("d MMM yyyy", Locale.FRENCH)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}
