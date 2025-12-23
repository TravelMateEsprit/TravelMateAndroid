package com.travelmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.travelmate.data.models.GroupMember

@Composable
fun GroupMembersDialog(
    members: List<GroupMember>,
    onDismiss: () -> Unit,
    currentUserId: String = "",
    isCreator: Boolean = false,
    onRemoveMember: (memberId: String, action: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Membres du groupe (${members.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                // Members List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(members) { member ->
                        MemberItem(
                            member = member,
                            isCreator = isCreator,
                            currentUserId = currentUserId,
                            onRemove = { action -> onRemoveMember(member.id, action) }
                        )
                    }
                }

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Fermer")
                }
            }
        }
    }
}

@Composable
private fun MemberItem(
    member: GroupMember,
    isCreator: Boolean = false,
    currentUserId: String = "",
    onRemove: (action: String) -> Unit = { _ -> },
    modifier: Modifier = Modifier
) {
    val showRemoveMenu = remember { mutableStateOf(false) }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        AsyncImage(
            model = member.avatar.ifEmpty { "https://via.placeholder.com/40" },
            contentDescription = "${member.nom} ${member.prenom}",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentScale = ContentScale.Crop
        )

        // Member Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // ✅ Try to get name from multiple sources
            val displayName = when {
                member.prenom.isNotBlank() && member.nom.isNotBlank() -> "${member.prenom} ${member.nom}"
                member.user?.nom?.isNotBlank() == true && member.user?.prenom?.isNotBlank() == true -> 
                    "${member.user?.prenom} ${member.user?.nom}"
                member.user?.nom?.isNotBlank() == true -> member.user?.nom ?: "Utilisateur"
                member.nom.isNotBlank() -> member.nom
                else -> "Utilisateur"
            }
            
            val displayEmail = member.email.ifBlank { member.user?.email ?: "" }
            
            Text(
                text = displayName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (displayEmail.isNotBlank()) {
                Text(
                    text = displayEmail,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Creator Badge or Action Menu
        if (member.isCreator) {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Text(
                    text = "👑 Créateur",
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        } else if (isCreator && member.id != currentUserId) {
            // ✅ NEW: Action menu for creator to remove/ban members
            Box {
                IconButton(onClick = { showRemoveMenu.value = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Actions",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                DropdownMenu(
                    expanded = showRemoveMenu.value,
                    onDismissRequest = { showRemoveMenu.value = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Supprimer du groupe") },
                        onClick = {
                            onRemove("remove")
                            showRemoveMenu.value = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Bannir") },
                        onClick = {
                            onRemove("ban")
                            showRemoveMenu.value = false
                        }
                    )
                }
            }
        }
    }
}
