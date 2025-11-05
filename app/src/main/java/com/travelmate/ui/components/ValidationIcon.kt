package com.travelmate.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.travelmate.ui.theme.ColorError
import com.travelmate.ui.theme.ColorSuccess

@Composable
fun ValidationIcon(
    isValid: Boolean,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = if (isValid) Icons.Default.Check else Icons.Default.Close,
        contentDescription = if (isValid) "Valid" else "Invalid",
        tint = if (isValid) ColorSuccess else ColorError,
        modifier = modifier.size(24.dp)
    )
}
