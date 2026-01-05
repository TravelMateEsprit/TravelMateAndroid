package com.travelmate.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Displays a star rating (read-only)
 */
@Composable
fun StarRatingDisplay(
    rating: Double,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    starSize: Int = 20,
    showRatingValue: Boolean = true,
    starColor: Color = Color(0xFFFFC107)
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Display stars
        for (i in 1..maxRating) {
            Icon(
                imageVector = if (i <= rating.toInt()) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (i <= rating.toInt()) starColor else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(starSize.dp)
            )
        }
        
        // Display numeric rating
        if (showRatingValue) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = String.format("%.1f", rating),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Interactive star rating input
 */
@Composable
fun StarRatingInput(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    starSize: Int = 32,
    enabled: Boolean = true,
    starColor: Color = Color(0xFFFFC107)
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxRating) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Note $i étoiles",
                tint = if (i <= rating) starColor else MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(starSize.dp)
                    .clickable(enabled = enabled) {
                        onRatingChanged(i)
                    }
                    .padding(4.dp)
            )
        }
    }
}

/**
 * Displays rating statistics with distribution bars
 */
@Composable
fun RatingStatsDisplay(
    averageRating: Double,
    totalReviews: Int,
    ratingDistribution: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Average rating display
        Text(
            text = String.format("%.1f", averageRating),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        StarRatingDisplay(
            rating = averageRating,
            starSize = 24,
            showRatingValue = false
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "$totalReviews avis",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Rating distribution
        for (star in 5 downTo 1) {
            val count = ratingDistribution[star.toString()] ?: 0
            val percentage = if (totalReviews > 0) (count.toFloat() / totalReviews) else 0f
            
            RatingDistributionBar(
                stars = star,
                count = count,
                percentage = percentage
            )
            
            if (star > 1) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Single bar in rating distribution
 */
@Composable
private fun RatingDistributionBar(
    stars: Int,
    count: Int,
    percentage: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Star label
        Text(
            text = "$stars",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(20.dp)
        )
        
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = Color(0xFFFFC107),
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Progress bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
        ) {
            // Background
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            ) {}
            
            // Filled portion
            Surface(
                modifier = Modifier
                    .fillMaxWidth(percentage)
                    .fillMaxHeight(),
                color = Color(0xFFFFC107),
                shape = MaterialTheme.shapes.small
            ) {}
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Count
        Text(
            text = count.toString(),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(30.dp)
        )
    }
}
