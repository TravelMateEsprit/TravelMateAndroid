package com.travelmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReactionPicker(
    onEmojiSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val emojis = listOf("👍", "❤️", "😂", "😮", "😢", "🙏")

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            emojis.forEach { emoji ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .clickable { onEmojiSelected(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        emoji,
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}