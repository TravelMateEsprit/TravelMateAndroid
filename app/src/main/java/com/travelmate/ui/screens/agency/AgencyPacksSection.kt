
package com.travelmate.ui.screens.agency

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelmate.data.models.Pack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyPacksScreen(
    packs: List<Pack>,
    isLoading: Boolean = false,
    error: String? = null,
    onRetry: () -> Unit = {},
    onNavigateBack: () -> Unit,
    onNavigateToCreatePack: () -> Unit,
    onNavigateToConversations: () -> Unit,
    onNavigateToReservations: () -> Unit,
    onOpenPackDetails: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes Packs") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToConversations) {
                        Icon(Icons.Default.Chat, contentDescription = "Conversations")
                    }
                    IconButton(onClick = onNavigateToReservations) {
                        Icon(Icons.Default.Notifications, contentDescription = "Réservations")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreatePack) {
                Icon(Icons.Default.Add, contentDescription = "Créer un pack")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Rechercher un pack") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Rechercher") }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Show loading state
            if (isLoading && packs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // Show error state
            else if (error != null && packs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "Erreur de chargement",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedButton(onClick = onRetry) {
                            Text("Réessayer")
                        }
                    }
                }
            }
            // Show empty state or list
            else {
                android.util.Log.d("AgencyPacksScreen", "Rendering packs: total=${packs.size}, searchQuery='$searchQuery', isLoading=$isLoading, error=$error")
                
                val filteredPacks = packs.filter {
                    searchQuery.isBlank() || 
                    it.titre.contains(searchQuery, ignoreCase = true) || 
                    (it.destination?.contains(searchQuery, ignoreCase = true) ?: false)
                }

                android.util.Log.d("AgencyPacksScreen", "After UI filter: ${filteredPacks.size} packs")

                if (filteredPacks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (searchQuery.isNotEmpty()) "Aucun pack trouvé" else "Aucun pack pour le moment.")
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredPacks) { pack ->
                            PackItem(pack = pack, onClick = { onOpenPackDetails(pack.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PackItem(
    pack: Pack,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = pack.titre, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = pack.destination ?: "Destination non spécifiée", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${pack.priceAdult} DT", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}
