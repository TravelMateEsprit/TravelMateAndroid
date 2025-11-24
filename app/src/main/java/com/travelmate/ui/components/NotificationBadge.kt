package com.travelmate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelmate.ui.theme.ColorError

@Composable
fun NotificationBadge(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BadgedBox(
        badge = {
            if (count > 0) {
                Badge(
                    containerColor = ColorError,
                    modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                ) {
                    Text(
                        if (count > 9) "9+" else count.toString(),
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }
            }
        },
        modifier = modifier
    ) {
        IconButton(onClick = onClick) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}