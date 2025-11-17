package com.travelmate.ui.screens.groups

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
import com.travelmate.data.models.Group
import com.travelmate.ui.components.CreateGroupDialog
import com.travelmate.ui.components.EditGroupDialog
import com.travelmate.ui.components.GroupCard
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.GroupsViewModel
import com.travelmate.viewmodel.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsListScreen(
    onNavigateToGroupDetails: (String) -> Unit,
    viewModel: GroupsViewModel = hiltViewModel()
) {
    val allGroups by viewModel.allGroups.collectAsState()
    val myGroups by viewModel.myGroups.collectAsState()
    val myCreatedGroups by viewModel.myCreatedGroups.collectAsState()
    val filteredGroups by viewModel.filteredGroups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val filterQuery by viewModel.filterQuery.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var groupToEdit by remember { mutableStateOf<Group?>(null) }
    var groupToDelete by remember { mutableStateOf<String?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

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
            // ========== HEADER ==========
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

            // ========== CONTENT ==========
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ========== TITRE ET BOUTON CRÉER ==========
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

                // ========== RECHERCHE ET TRI ==========
                item {
                    Column {
                        OutlinedTextField(
                            value = filterQuery,
                            onValueChange = { viewModel.setFilterQuery(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Rechercher un groupe...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, null, tint = ColorTextSecondary)
                            },
                            trailingIcon = {
                                if (filterQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setFilterQuery("") }) {
                                        Icon(Icons.Default.Close, "Effacer")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ColorPrimary,
                                unfocusedBorderColor = ColorTextSecondary.copy(alpha = 0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Trier par: ${sortOption.label}",
                                fontSize = 14.sp,
                                color = ColorTextSecondary
                            )

                            Box {
                                OutlinedButton(
                                    onClick = { showSortMenu = true },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = ColorPrimary
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Sort,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Trier", fontSize = 14.sp)
                                }

                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    SortOption.values().forEach { option ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    if (sortOption == option) {
                                                        Icon(
                                                            Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = ColorPrimary,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    } else {
                                                        Spacer(modifier = Modifier.size(18.dp))
                                                    }
                                                    Text(option.label)
                                                }
                                            },
                                            onClick = {
                                                viewModel.setSortOption(option)
                                                showSortMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ========== MES GROUPES CRÉÉS (FILTRÉS) ==========
                // ✅ CORRECTION : Utiliser filteredGroups au lieu de myCreatedGroups directement
                val displayedCreatedGroups = filteredGroups.filter { group ->
                    myCreatedGroups.any { it._id == group._id }
                }

                if (displayedCreatedGroups.isNotEmpty()) {
                    item {
                        Text(
                            "Mes groupes créés",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextPrimary
                        )
                    }

                    items(displayedCreatedGroups) { group ->
                        GroupCard(
                            group = group,
                            isMyGroup = true,
                            isCreatedByUser = true,
                            onJoin = { groupId -> viewModel.joinGroup(groupId) },
                            onLeave = { groupId -> viewModel.leaveGroup(groupId) },
                            onDelete = { groupId -> groupToDelete = groupId },
                            onEdit = { groupId ->
                                groupToEdit = myCreatedGroups.find { it._id == groupId }
                                showEditDialog = true
                            },
                            onClick = { groupId -> onNavigateToGroupDetails(groupId) }
                        )
                    }
                }

                // ========== MES GROUPES (FILTRÉS) ==========
                // ✅ CORRECTION : Utiliser filteredGroups au lieu de myGroups directement
                val displayedMyGroups = filteredGroups.filter { group ->
                    myGroups.any { it._id == group._id }
                }

                if (displayedMyGroups.isNotEmpty()) {
                    item {
                        Text(
                            "Mes groupes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextPrimary,
                            modifier = Modifier.padding(top = if (displayedCreatedGroups.isNotEmpty()) 8.dp else 0.dp)
                        )
                    }

                    items(displayedMyGroups) { group ->
                        GroupCard(
                            group = group,
                            isMyGroup = true,
                            isCreatedByUser = false,
                            onJoin = { groupId -> viewModel.joinGroup(groupId) },
                            onLeave = { groupId -> viewModel.leaveGroup(groupId) },
                            onClick = { groupId -> onNavigateToGroupDetails(groupId) }
                        )
                    }
                }

                // ========== DÉCOUVRIR DES GROUPES (FILTRÉS) ==========
                item {
                    Text(
                        "Découvrir des groupes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // ✅ CORRECTION : Filtrer correctement
                val discoverGroups = filteredGroups.filter { group ->
                    val isCreatedByUser = myCreatedGroups.any { it._id == group._id }
                    val isInMyGroups = myGroups.any { it._id == group._id }
                    !isCreatedByUser && !isInMyGroups
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = ColorTextSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Aucun groupe trouvé",
                                    color = ColorTextSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(discoverGroups) { group ->
                        GroupCard(
                            group = group,
                            isMyGroup = group.isUserMember,
                            isCreatedByUser = false,
                            onJoin = { groupId -> viewModel.joinGroup(groupId) },
                            onLeave = { groupId -> viewModel.leaveGroup(groupId) },
                            onClick = { groupId -> onNavigateToGroupDetails(groupId) }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // ========== DIALOGS (identiques) ==========
    if (showCreateDialog) {
        CreateGroupDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, destination, description, imageUrl ->
                viewModel.createGroup(name, destination, description, imageUrl)
                showCreateDialog = false
            },
            onUploadImage = { uri, onSuccess, onError ->
                viewModel.uploadGroupImage(uri, onSuccess, onError)
            }
        )
    }

    if (showEditDialog && groupToEdit != null) {
        EditGroupDialog(
            group = groupToEdit!!,
            onDismiss = {
                showEditDialog = false
                groupToEdit = null
            },
            onConfirm = { name, destination, description, imageUrl ->
                viewModel.updateGroup(groupToEdit!!._id, name, destination, description, imageUrl)
                showEditDialog = false
                groupToEdit = null
            },
            onUploadImage = { uri, onSuccess, onError ->
                viewModel.uploadGroupImage(uri, onSuccess, onError)
            }
        )
    }

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