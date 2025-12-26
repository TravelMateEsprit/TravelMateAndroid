package com.travelmate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelmate.data.models.MemberRole
import com.travelmate.data.models.GroupMember
import com.travelmate.ui.theme.ColorError
import com.travelmate.ui.theme.ColorPrimary
import com.travelmate.ui.theme.ColorTextPrimary
import com.travelmate.ui.theme.ColorTextSecondary

// Use GroupMember from data.models package

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMembersSheet(
    members: List<GroupMember>,
    isUserAdmin: Boolean,
    onPromoteToAdmin: (String) -> Unit = {},
    onDemoteToMember: (String) -> Unit = {},
    onBanMember: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Membres du groupe",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(members.filter { it.userId != null }) { member ->
                val userId = member.userId?.id ?: return@items
                MemberCard(
                    member = member,
                    isUserAdmin = isUserAdmin,
                    onPromoteToAdmin = { onPromoteToAdmin(userId) },
                    onDemoteToMember = { onDemoteToMember(userId) },
                    onBanMember = { onBanMember(userId) }
                )
            }
        }
    }
}

@Composable
private fun MemberCard(
    member: GroupMember,
    isUserAdmin: Boolean,
    onPromoteToAdmin: () -> Unit,
    onDemoteToMember: () -> Unit,
    onBanMember: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = member.userName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (member.isAdmin()) "Administrateur" else "Membre",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isUserAdmin) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Plus d'options")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        when (member.role) {
                            "member" -> {
                                DropdownMenuItem(
                                    text = { Text("Promouvoir administrateur") },
                                    onClick = {
                                        onPromoteToAdmin()
                                        showMenu = false
                                    }
                                )
                            }
                            "admin" -> {
                                DropdownMenuItem(
                                    text = { Text("Rétrograder membre") },
                                    onClick = {
                                        onDemoteToMember()
                                        showMenu = false
                                    }
                                )
                            }
                            else -> { /* Do nothing */ }
                        }
                        DropdownMenuItem(
                            text = { Text("Bannir du groupe", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                onBanMember()
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}