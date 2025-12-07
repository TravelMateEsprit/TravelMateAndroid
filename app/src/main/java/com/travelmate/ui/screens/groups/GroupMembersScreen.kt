package com.travelmate.ui.screens.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.travelmate.data.models.GroupMember
import com.travelmate.ui.theme.ColorError
import com.travelmate.ui.theme.ColorPrimary
import com.travelmate.ui.theme.ColorTextPrimary
import com.travelmate.ui.theme.ColorTextSecondary
import com.travelmate.viewmodel.GroupsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMembersScreen(
    groupId: String,
    onBack: () -> Unit,
    viewModel: GroupsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("travelmate_prefs", android.content.Context.MODE_PRIVATE)
    val currentUserId = prefs.getString("user_id", "") ?: ""
    
    val members by viewModel.groupMembers.collectAsState()
    val currentGroup by viewModel.currentGroup.collectAsState()
    val isCreator = currentGroup?.createdBy == currentUserId
    val coroutineScope = rememberCoroutineScope()

    // Load members on screen open
    LaunchedEffect(groupId) {
        viewModel.getGroupMembers(groupId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Membres du groupe (${members.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(members) { member ->
                MemberCard(
                    member = member,
                    isCreator = isCreator,
                    currentUserId = currentUserId,
                    onRemoveMember = { memberId, action ->
                        viewModel.removeMember(groupId, memberId, action)
                        // Rafraîchir les membres après un délai
                        coroutineScope.launch {
                            delay(1000)
                            viewModel.getGroupMembers(groupId)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MemberCard(
    member: GroupMember,
    isCreator: Boolean,
    currentUserId: String,
    onRemoveMember: (String, String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val isCurrentUser = member.userId?.id == currentUserId

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Member info
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ColorPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    if (member.userAvatar.isNotEmpty()) {
                        AsyncImage(
                            model = member.userAvatar,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                    } else {
                        val initial = (member.userNom.firstOrNull() ?: 'U').toString()
                        Text(
                            text = initial,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                // Name and role
                Column {
                    Text(
                        text = member.userName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorTextPrimary
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                member.role == "creator" -> "Créateur"
                                member.role == "admin" -> "Administrateur"
                                else -> "Membre"
                            },
                            fontSize = 14.sp,
                            color = if (member.role == "creator" || member.role == "admin") {
                                ColorPrimary
                            } else {
                                ColorTextSecondary
                            }
                        )
                        if (member.status == "pending") {
                            Text(
                                text = "• En attente",
                                fontSize = 14.sp,
                                color = ColorTextSecondary
                            )
                        }
                    }
                }
            }

            // Actions menu (only for creator and not current user)
            if (isCreator && !isCurrentUser && member.role != "creator") {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Actions",
                            tint = ColorTextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (member.status == "pending") {
                            DropdownMenuItem(
                                text = { Text("Approuver") },
                                onClick = {
                                    onRemoveMember(member.userId?.id ?: member.id, "approve")
                                    showMenu = false
                                }
                            )
                        }

                        if (member.role != "admin") {
                            DropdownMenuItem(
                                text = { Text("Promouvoir admin") },
                                onClick = {
                                    onRemoveMember(member.userId?.id ?: member.id, "promote")
                                    showMenu = false
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Rétrograder") },
                                onClick = {
                                    onRemoveMember(member.userId?.id ?: member.id, "demote")
                                    showMenu = false
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("Bannir", color = ColorError) },
                            onClick = {
                                onRemoveMember(member.userId?.id ?: member.id, "ban")
                                showMenu = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Retirer", color = ColorError) },
                            onClick = {
                                onRemoveMember(member.userId?.id ?: member.id, "remove")
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}
