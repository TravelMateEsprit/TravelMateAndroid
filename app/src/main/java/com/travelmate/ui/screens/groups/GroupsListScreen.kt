package com.travelmate.ui.screens.groups

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import android.util.Log

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
    var selectedTab by remember { mutableStateOf(0) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            Log.e("GroupsListScreen", "❌ Error: $it")
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        Log.d("GroupsListScreen", "🔄 Loading groups on screen start")
        viewModel.loadAllGroups()
    }

    LaunchedEffect(allGroups) {
        Log.d("GroupsListScreen", "📊 All groups count: ${allGroups.size}")
        allGroups.forEach { group ->
            Log.d("GroupsListScreen", "  - ${group.name} (${group._id})")
        }
    }

    LaunchedEffect(myGroups) {
        Log.d("GroupsListScreen", "👥 My groups count: ${myGroups.size}")
    }

    LaunchedEffect(filteredGroups) {
        Log.d("GroupsListScreen", "🔍 Filtered groups count: ${filteredGroups.size}")
    }

    // Calcul des groupes filtrés
    val displayedCreatedGroups = filteredGroups.filter { group ->
        myCreatedGroups.any { it._id == group._id }
    }

    val displayedMyGroups = filteredGroups.filter { group ->
        myGroups.any { it._id == group._id }
    }

    val discoverGroups = filteredGroups.filter { group ->
        val isCreatedByUser = myCreatedGroups.any { it._id == group._id }
        val isInMyGroups = myGroups.any { it._id == group._id }
        !isCreatedByUser && !isInMyGroups
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            // FAB avec animation
            AnimatedVisibility(
                visible = selectedTab == 0 || selectedTab == 1,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = ColorPrimary,
                    contentColor = Color.White,
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    text = {
                        Text(
                            "Créer un groupe",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            // En-tête avec gradient
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    ColorPrimary,
                                    ColorPrimary.copy(alpha = 0.8f)
                                )
                            )
                        )
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "${myGroups.size + myCreatedGroups.size} Groupes Actifs",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }

                            IconButton(onClick = { /* Notifications */ }) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = ColorError,
                                            modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                                        ) {
                                            Text("3", fontSize = 10.sp, color = Color.White)
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        // Barre de recherche intégrée
                        OutlinedTextField(
                            value = filterQuery,
                            onValueChange = { viewModel.setFilterQuery(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 12.dp),
                            placeholder = {
                                Text(
                                    "Rechercher un groupe, une destination...",
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            },
                            trailingIcon = {
                                Row {
                                    if (filterQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.setFilterQuery("") }) {
                                            Icon(
                                                Icons.Default.Close,
                                                "Effacer",
                                                tint = Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                    }

                                    IconButton(onClick = { showSortMenu = true }) {
                                        Icon(
                                            Icons.Default.Sort,
                                            "Trier",
                                            tint = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White,
                                focusedContainerColor = Color.White.copy(alpha = 0.15f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.1f)
                            ),
                            singleLine = true
                        )

                        // Menu de tri
                        Box {
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                Text(
                                    "Trier par",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontSize = 12.sp,
                                    color = ColorTextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Divider()

                                SortOption.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                if (sortOption == option) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = ColorPrimary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                } else {
                                                    Spacer(modifier = Modifier.size(20.dp))
                                                }
                                                Text(
                                                    option.label,
                                                    fontWeight = if (sortOption == option) FontWeight.SemiBold else FontWeight.Normal
                                                )
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

            // Onglets
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = ColorPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = ColorPrimary,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Mes créations",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                            if (displayedCreatedGroups.isNotEmpty()) {
                                Text(
                                    "(${displayedCreatedGroups.size})",
                                    fontSize = 11.sp,
                                    color = ColorTextSecondary
                                )
                            }
                        }
                    }
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Mes groupes",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                            if (displayedMyGroups.isNotEmpty()) {
                                Text(
                                    "(${displayedMyGroups.size})",
                                    fontSize = 11.sp,
                                    color = ColorTextSecondary
                                )
                            }
                        }
                    }
                )

                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Découvrir",
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                            )
                            if (discoverGroups.isNotEmpty()) {
                                Text(
                                    "(${discoverGroups.size})",
                                    fontSize = 11.sp,
                                    color = ColorTextSecondary
                                )
                            }
                        }
                    }
                )
            }

            // Contenu des onglets
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> GroupsList(
                        groups = displayedCreatedGroups,
                        isLoading = isLoading && allGroups.isEmpty(),
                        emptyMessage = if (filterQuery.isEmpty()) {
                            "Vous n'avez pas encore créé de groupe"
                        } else {
                            "Aucun groupe créé ne correspond à votre recherche"
                        },
                        emptyIcon = Icons.Default.AddCircleOutline,
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

                    1 -> GroupsList(
                        groups = displayedMyGroups,
                        isLoading = isLoading && allGroups.isEmpty(),
                        emptyMessage = if (filterQuery.isEmpty()) {
                            "Vous n'avez rejoint aucun groupe"
                        } else {
                            "Aucun groupe rejoint ne correspond à votre recherche"
                        },
                        emptyIcon = Icons.Default.GroupAdd,
                        isMyGroup = true,
                        isCreatedByUser = false,
                        onJoin = { groupId -> viewModel.joinGroup(groupId) },
                        onLeave = { groupId -> viewModel.leaveGroup(groupId) },
                        onClick = { groupId -> onNavigateToGroupDetails(groupId) }
                    )

                    2 -> GroupsList(
                        groups = discoverGroups,
                        isLoading = isLoading && allGroups.isEmpty(),
                        emptyMessage = if (filterQuery.isEmpty()) {
                            "Aucun nouveau groupe à découvrir"
                        } else {
                            "Aucun groupe ne correspond à votre recherche"
                        },
                        emptyIcon = Icons.Default.Explore,
                        isMyGroup = false,
                        isCreatedByUser = false,
                        onJoin = { groupId -> viewModel.joinGroup(groupId) },
                        onLeave = { groupId -> viewModel.leaveGroup(groupId) },
                        onClick = { groupId -> onNavigateToGroupDetails(groupId) }
                    )
                }
            }
        }
    }

    // Dialogs
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
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = ColorError,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Supprimer le groupe") },
            text = {
                Text(
                    "Êtes-vous sûr de vouloir supprimer le groupe \"${group?.name ?: ""}\" ?\n\n" +
                            "Cette action est irréversible et supprimera tous les messages et données associés.",
                    lineHeight = 20.sp
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
                    Text("Supprimer définitivement")
                }
            },
            dismissButton = {
                TextButton(onClick = { groupToDelete = null }) {
                    Text("Annuler", color = ColorTextSecondary)
                }
            }
        )
    }
}

// Composant réutilisable pour afficher la liste de groupes
@Composable
fun GroupsList(
    groups: List<Group>,
    isLoading: Boolean,
    emptyMessage: String,
    emptyIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isMyGroup: Boolean,
    isCreatedByUser: Boolean,
    onJoin: (String) -> Unit,
    onLeave: (String) -> Unit,
    onDelete: ((String) -> Unit)? = null,
    onEdit: ((String) -> Unit)? = null,
    onClick: (String) -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = ColorPrimary)
                Text(
                    "Chargement des groupes...",
                    color = ColorTextSecondary,
                    fontSize = 14.sp
                )
            }
        }
    } else if (groups.isEmpty()) {
        EmptyGroupsState(
            message = emptyMessage,
            icon = emptyIcon
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(groups, key = { it._id }) { group ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    GroupCard(
                        group = group,
                        isMyGroup = isMyGroup,
                        isCreatedByUser = isCreatedByUser,
                        onJoin = onJoin,
                        onLeave = onLeave,
                        onDelete = onDelete,
                        onEdit = onEdit,
                        onClick = onClick
                    )
                }
            }

            // Espacement pour le FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

// État vide avec icône et message
@Composable
fun EmptyGroupsState(
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = ColorPrimary.copy(alpha = 0.1f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp),
                    tint = ColorPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                message,
                fontSize = 16.sp,
                color = ColorTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Commencez votre aventure",
                fontSize = 14.sp,
                color = ColorTextSecondary.copy(alpha = 0.7f)
            )
        }
    }
}