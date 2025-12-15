package com.travelmate.ui.screens.agency

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.travelmate.data.models.Pack
import com.travelmate.ui.components.ModernCard
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.AgencyPacksViewModel
import com.travelmate.viewmodel.SortOption
import com.travelmate.viewmodel.FilterStatus
import com.travelmate.viewmodel.PriceRange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacksListScreen(
    onNavigateToCreatePack: () -> Unit,
    onNavigateToPackDetails: (String) -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToReservations: () -> Unit,
    onNavigateBack: () -> Unit = {},
    viewModel: AgencyPacksViewModel = hiltViewModel()
) {
    val packs by viewModel.filteredPacks.collectAsState()
    val selectedPacks by viewModel.selectedPacks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isSelectionMode = selectedPacks.isNotEmpty()
    
    val filterStatus by viewModel.filterStatus.collectAsState()
    val filterDestination by viewModel.filterDestination.collectAsState()
    val filterPriceRange by viewModel.filterPriceRange.collectAsState()
    val filterTypeOffre by viewModel.filterTypeOffre.collectAsState()

    val hasActiveFilters = filterStatus != null ||
        filterDestination != null ||
        filterPriceRange != null ||
        filterTypeOffre != null

    // Load packs on first composition
    LaunchedEffect(Unit) {
        viewModel.loadMyPacks()
    }

    // Reload packs when success message appears (e.g., after creating a pack)
    // This ensures the newly created pack appears in the list
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            // Reload the list to show the newly created pack
            // Add a small delay to ensure backend has processed the new pack
            kotlinx.coroutines.delay(500)
            viewModel.loadMyPacks()
            // Auto-clear after 3 seconds
            kotlinx.coroutines.delay(2500)
            viewModel.clearSuccessMessage()
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmer la suppression") },
            text = {
                Text(
                    "Voulez-vous vraiment supprimer ${selectedPacks.size} pack(s) ?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSelectedPacks()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorError
                    )
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Filter dialog
    if (showFilterDialog) {
        FilterDialog(
            onDismiss = { showFilterDialog = false },
            onClearFilters = {
                viewModel.clearFilters()
            },
            currentStatus = filterStatus,
            onStatusChanged = { viewModel.setFilterStatus(it) },
            viewModel = viewModel
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSelectionMode) {
                        Text("${selectedPacks.size} sélectionné(s)", fontWeight = FontWeight.Bold)
                    } else {
                        Text("Mes Packs", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    }
                },
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, "Annuler sélection")
                        }
                    } else {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, "Retour")
                        }
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    } else {
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filtrer",
                                tint = if (hasActiveFilters) Color(0xFFFFC107) else Color.White
                            )
                        }
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Trier")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF7F7F7)
    ) { paddingValues ->
        // Sort menu dropdown
        Box(modifier = Modifier.fillMaxSize()) {
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                DropdownMenuItem(
                    text = { Text("Prix croissant") },
                    onClick = {
                        viewModel.sortPacks(SortOption.PRICE_ASC)
                        showSortMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Prix décroissant") },
                    onClick = {
                        viewModel.sortPacks(SortOption.PRICE_DESC)
                        showSortMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Date (récent)") },
                    onClick = {
                        viewModel.sortPacks(SortOption.DATE_DESC)
                        showSortMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Titre A-Z") },
                    onClick = {
                        viewModel.sortPacks(SortOption.TITLE)
                        showSortMenu = false
                    }
                )
            }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            ActionButtonColumn(
                onChat = onNavigateToChat,
                onCreatePack = onNavigateToCreatePack,
                onReservations = onNavigateToReservations
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                // Success/Error Messages
                AnimatedVisibility(
                    visible = successMessage != null,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Surface(
                        color = ColorSuccess.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ColorSuccess
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                successMessage ?: "",
                                color = ColorSuccess,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearSuccessMessage() }) {
                                Icon(Icons.Default.Close, null, tint = ColorSuccess)
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = error != null,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Surface(
                        color = ColorError.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = ColorError
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                error ?: "",
                                color = ColorError,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(Icons.Default.Close, null, tint = ColorError)
                            }
                        }
                    }
                }

                // Search Bar
                if (!isSelectionMode) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        placeholder = { Text("Rechercher un pack...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(Icons.Default.Close, "Effacer")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Loading State
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ColorPrimary)
                    }
                }
                // Empty State
                else if (packs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Inventory,
                                contentDescription = null,
                                tint = ColorTextSecondary,
                                modifier = Modifier.size(80.dp)
                            )
                            Text(
                                if (searchQuery.isNotEmpty()) "Aucun résultat"
                                else "Aucun pack",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorTextPrimary
                            )
                            Text(
                                if (searchQuery.isNotEmpty()) "Essayez une autre recherche"
                                else "Créez votre premier pack pour commencer",
                                fontSize = 14.sp,
                                color = ColorTextSecondary
                            )
                        }
                    }
                }
                // Packs List
                else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(packs, key = { it.id }) { pack ->
                            PackListItemCard(
                                pack = pack,
                                isSelected = selectedPacks.contains(pack.id),
                                onPackClick = {
                                    if (isSelectionMode) {
                                        viewModel.togglePackSelection(pack.id)
                                    } else {
                                        onNavigateToPackDetails(pack.id)
                                    }
                                },
                                onPackLongClick = {
                                    viewModel.togglePackSelection(pack.id)
                                }
                            )
                        }
                    }
                }
            }
        }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PackListItemCard(
    pack: Pack,
    isSelected: Boolean,
    onPackClick: () -> Unit,
    onPackLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onPackClick,
                onLongClick = onPackLongClick
            ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp,
            pressedElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ColorPrimary.copy(alpha = 0.05f) else Color.White
        )
    ) {
        Box {
            // Selection border
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(ColorPrimary, ColorSecondary)
                            )
                        )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selection checkbox
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ColorPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                // Pack image or icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    if (pack.images.isNotEmpty()) {
                        AsyncImage(
                            model = pack.images.first(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(
                                            ColorPrimary,
                                            ColorSecondary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Flight,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    
                    // Status badge overlay
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        color = if (pack.actif) ColorSuccess else Color.Gray,
                        shape = CircleShape
                    ) {
                        Box(
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Pack info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        pack.titre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                tint = ColorPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                pack.destination.orEmpty(),
                                fontSize = 14.sp,
                                color = ColorTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = ColorPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "${pack.dateDebut} - ${pack.dateFin}",
                                fontSize = 12.sp,
                                color = ColorTextSecondary
                            )
                        }
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = ColorPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "${pack.prix.toInt()} DT",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorPrimary
                            )
                        }
                        
                        Surface(
                            color = if (pack.actif) ColorSuccess.copy(alpha = 0.15f)
                            else Color.Gray.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (pack.actif) ColorSuccess else Color.Gray
                                        )
                                )
                                Text(
                                    if (pack.actif) "Actif" else "Inactif",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (pack.actif) ColorSuccess else Color.Gray
                                )
                            }
                        }
                    }
                }

                // Arrow icon
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ColorTextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ActionButtonColumn(
    onChat: () -> Unit,
    onCreatePack: () -> Unit,
    onReservations: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionIconButton(
            icon = Icons.Default.Chat,
            label = "Chats",
            onClick = onChat
        )
        ActionIconButton(
            icon = Icons.Default.Add,
            label = "Nouveau",
            onClick = onCreatePack
        )
        ActionIconButton(
            icon = Icons.Default.EventNote,
            label = "Demandes",
            onClick = onReservations
        )
    }
}

@Composable
private fun ActionIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = ColorPrimary)
        Text(label, fontSize = 12.sp, color = ColorTextPrimary)
    }
}

@Composable
private fun FilterDialog(
    onDismiss: () -> Unit,
    onClearFilters: () -> Unit,
    currentStatus: FilterStatus?,
    onStatusChanged: (FilterStatus?) -> Unit,
    viewModel: AgencyPacksViewModel
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtres avancés") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Statut")
                FilterStatus.values().forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (currentStatus == status) ColorPrimary.copy(alpha = 0.1f) else Color.Transparent
                            )
                            .clickable { onStatusChanged(status) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentStatus == status,
                            onClick = { onStatusChanged(status) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            when (status) {
                                FilterStatus.ALL -> "Tous"
                                FilterStatus.ACTIVE -> "Actifs"
                                FilterStatus.INACTIVE -> "Inactifs"
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fermer") }
        },
        dismissButton = {
            TextButton(onClick = onClearFilters) { Text("Réinitialiser") }
        }
    )
}
