package com.travelmate.ui.screens.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.travelmate.ui.components.CreateGroupDialog
import com.travelmate.ui.components.GroupCard
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.GroupsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsListScreen(
    viewModel: GroupsViewModel = hiltViewModel()
) {
    val allGroups by viewModel.allGroups.collectAsState()
    val myGroups by viewModel.myGroups.collectAsState()
    val myCreatedGroups by viewModel.myCreatedGroups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var groupToDelete by remember { mutableStateOf<String?>(null) }

    // Show error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAllGroups()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ColorPrimary,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Groupes",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row {
                        IconButton(onClick = { /* Notifications */ }) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = ColorError) {
                                        Text("3", fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, null, tint = Color.White)
                            }
                        }
                    }
                }
            }

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title + Add Button
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Groupes de voyage",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ColorTextPrimary
                            )
                            FloatingActionButton(
                                onClick = { showCreateDialog = true },
                                containerColor = ColorPrimary,
                                contentColor = Color.White,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Créer un groupe",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // Search Bar
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Rechercher un groupe...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, null, tint = ColorTextSecondary)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ColorPrimary,
                                unfocusedBorderColor = ColorTextSecondary.copy(alpha = 0.3f)
                            )
                        )
                    }

                    // My Created Groups Section
                    if (myCreatedGroups.isNotEmpty()) {
                        item {
                            Text(
                                "Mes groupes créés",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorTextPrimary
                            )
                        }

                        items(myCreatedGroups.filter {
                            it.name.contains(searchQuery, ignoreCase = true) ||
                                    it.description.contains(searchQuery, ignoreCase = true)
                        }) { group ->
                            GroupCard(
                                group = group,
                                isMyGroup = true,
                                isCreatedByUser = true,
                                onJoin = { viewModel.joinGroup(it) },
                                onLeave = { viewModel.leaveGroup(it) },
                                onDelete = { groupToDelete = it }
                            )
                        }
                    }

                    // My Groups Section (groupes où l'utilisateur est membre mais pas créateur)
                    if (myGroups.isNotEmpty()) {
                        item {
                            Text(
                                "Mes groupes",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorTextPrimary,
                                modifier = Modifier.padding(top = if (myCreatedGroups.isNotEmpty()) 8.dp else 0.dp)
                            )
                        }

                        items(myGroups.filter {
                            it.name.contains(searchQuery, ignoreCase = true) ||
                                    it.description.contains(searchQuery, ignoreCase = true)
                        }) { group ->
                            GroupCard(
                                group = group,
                                isMyGroup = true,
                                isCreatedByUser = false,
                                onJoin = { viewModel.joinGroup(it) },
                                onLeave = { viewModel.leaveGroup(it) }
                            )
                        }
                    }

                    // Discover Groups Section
                    item {
                        Text(
                            "Découvrir des groupes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextPrimary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Tous les autres groupes (pas créés par l'utilisateur)
                    // Exclure aussi les groupes où l'utilisateur est déjà membre (qui sont dans myGroups)
                    val discoverGroups = allGroups.filter { group ->
                        val isCreatedByUser = myCreatedGroups.any { it._id == group._id }
                        val isInMyGroups = myGroups.any { it._id == group._id }
                        !isCreatedByUser && !isInMyGroups
                    }.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                                it.description.contains(searchQuery, ignoreCase = true)
                    }

                    if (isLoading && allGroups.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = ColorPrimary)
                            }
                        }
                    } else if (discoverGroups.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Aucun groupe disponible",
                                    color = ColorTextSecondary
                                )
                            }
                        }
                    } else {
                        items(discoverGroups) { group ->
                            GroupCard(
                                group = group,
                                isMyGroup = group.isUserMember,
                                isCreatedByUser = false,
                                onJoin = { viewModel.joinGroup(it) },
                                onLeave = { viewModel.leaveGroup(it) }
                            )
                        }
                    }
                }
            }
        }
        
        // Snackbar for errors - positioned at bottom
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Create Group Dialog
    if (showCreateDialog) {
        CreateGroupDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, destination, description ->
                viewModel.createGroup(name, destination, description)
                showCreateDialog = false
            }
        )
    }

    // Delete Group Confirmation Dialog
    groupToDelete?.let { groupId ->
        val group = allGroups.find { it._id == groupId }
        AlertDialog(
            onDismissRequest = { groupToDelete = null },
            title = { Text("Supprimer le groupe") },
            text = {
                Text(
                    "Êtes-vous sûr de vouloir supprimer le groupe \"${group?.name ?: ""}\" ? " +
                            "Cette action est irréversible."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGroup(groupId)
                        groupToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorError
                    )
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { groupToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}