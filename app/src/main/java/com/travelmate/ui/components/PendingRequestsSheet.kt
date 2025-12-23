package com.travelmate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelmate.data.models.PendingRequest
import com.travelmate.ui.theme.*

@Composable
fun PendingRequestsSheet(
    requests: List<PendingRequest>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.People,
                contentDescription = null,
                tint = ColorPrimary,
                modifier = Modifier.size(28.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Demandes en attente",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )

                Text(
                    "${requests.size} ${if (requests.size > 1) "demandes" else "demande"}",
                    fontSize = 14.sp,
                    color = ColorTextSecondary
                )
            }
        }

        Divider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = ColorTextSecondary.copy(alpha = 0.1f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Liste des demandes
        if (requests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Aucune demande en attente",
                    fontSize = 14.sp,
                    color = ColorTextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                contentPadding = PaddingValues(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(requests) { request ->
                    PendingRequestCard(
                        request = request,
                        onApprove = { onApprove(request.userId.id) },  // ✅ Utilise .id au lieu de ._id
                        onReject = { onReject(request.userId.id) }
                    )
                }
            }
        }
    }
}