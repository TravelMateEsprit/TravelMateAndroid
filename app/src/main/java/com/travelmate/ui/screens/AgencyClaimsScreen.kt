    @file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

    package com.travelmate.ui.screens

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
    import androidx.hilt.navigation.compose.hiltViewModel
    import androidx.navigation.NavController
    import com.travelmate.data.models.Claim
    import com.travelmate.ui.components.*
    import com.travelmate.utils.Constants
    import com.travelmate.viewmodel.ClaimViewModel

    @Composable
    fun AgencyClaimsScreen(
        navController: NavController,
        viewModel: ClaimViewModel = hiltViewModel()
    ) {
        val claims by viewModel.agencyClaims.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val error by viewModel.error.collectAsState()
        val unreadCount by viewModel.unreadCount.collectAsState()

        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("Toutes", "En attente", "En cours", "Résolues")

        LaunchedEffect(Unit) {
            viewModel.loadAgencyClaims()
            viewModel.loadUnreadCount()
        }

        val filteredClaims = when (selectedTab) {
            1 -> claims.filter { it.status == "OUVERT" }
            2 -> claims.filter { it.status == "EN_COURS" }
            3 -> claims.filter { it.status == "RESOLU" || it.status == "FERME" }
            else -> claims
        }

        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = { Text("Réclamations clients") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                            }
                        },
                        actions = {
                            if (unreadCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(end = 16.dp)
                                ) {
                                    Text(unreadCount.toString())
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF2196F3),
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White
                        )
                    )

                    // Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFF2196F3),
                        contentColor = Color.White
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    error != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = error ?: "Erreur inconnue",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadAgencyClaims() }) {
                                Text("Réessayer")
                            }
                        }
                    }
                    filteredClaims.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Aucune réclamation",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredClaims) { claim ->
                                AgencyClaimCard(
                                    claim = claim,
                                    onClick = { navController.navigate("${Constants.Routes.AGENCY_CLAIM_DETAIL}/${claim._id}") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun AgencyClaimCard(claim: Claim, onClick: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            onClick = onClick
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = claim.subject,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    if (!claim.isReadByAgency) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error
                        ) {
                            Text("Nouveau", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Info client
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = claim.userId?.name ?: "Client",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = claim.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ClaimStatusChip(status = claim.status)
                        ClaimPriorityChip(priority = claim.priority)
                    }

                    Text(
                        text = formatDate(claim.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
